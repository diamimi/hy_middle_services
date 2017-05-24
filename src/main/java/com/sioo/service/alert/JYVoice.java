package com.sioo.service.alert;

import java.util.Calendar;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import com.sioo.util.SmsCache;

/**
 * 
 * @author CQL 331737188@qq.com
 * @date : 2016年7月6日 下午1:34:24
 *
 */
public class JYVoice {
	private static final Logger logger = Logger.getLogger(JYVoice.class);

	public static void sendVoiceAlert(String content) {
		try {
			Calendar c = Calendar.getInstance();
			int time = c.get(Calendar.HOUR_OF_DAY);
			String result = null;
			if (time >= 22 || time < 7) {// 夜间
				if (SmsCache.alertMsgNightMobiles != null && !SmsCache.alertMsgNightMobiles.isEmpty()) {
					String mobile = "";
					for (String mobiles : SmsCache.alertMsgNightMobiles) {
						mobile += mobiles + ",";
					}
					if (!mobile.isEmpty()) {
						mobile = mobile.substring(0, mobile.length() - 1);
					}
					String url = "http://i.huixun35.com/sdk/SMS?cmd=sendvoice&uid=4814002&psw=b168bc069e0dfa7e71506f310f73f3f2&mobiles=" + mobile + "&msg="
							+ java.net.URLEncoder.encode(content, "gbk");
					result = UrlConnection.doGetRequest(url);
				}
			} else {// 日间
				if (SmsCache.alertMsgNightMobiles != null && !SmsCache.alertMsgDayMobiles.isEmpty()) {
					String mobile = "";
					for (String mobiles : SmsCache.alertMsgDayMobiles) {
						mobile += mobiles + ",";
					}
					if (!mobile.isEmpty()) {
						mobile = mobile.substring(0, mobile.length() - 1);
					}
					String url = "http://i.huixun35.com/sdk/SMS?cmd=sendvoice&uid=4814002&psw=b168bc069e0dfa7e71506f310f73f3f2&mobiles=" + mobile + "&msg="
							+ java.net.URLEncoder.encode(content, "gbk");
					result = UrlConnection.doGetRequest(url);
				}
			}
			if (result != null) {
				// rspCode=0&rspDesc=提交成功&msgId=859d6991dd3f4c55a409d0c41ac85521
				logger.info("收到结果,result=" + result);
			}
		} catch (Exception e) {
			logger.error("[JYVoice.sendVoiceAlert(" + content + ") Exception]", e);
		}
	}

	public static String sendGetRequest(String url) throws Exception {
		String resp = null;
		HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());
		HttpMethod httpmethod = new GetMethod(url);
		try {
			httpmethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
			int statusCode = client.executeMethod(httpmethod);
			if (statusCode == HttpStatus.SC_OK) {
				resp = httpmethod.getResponseBodyAsString();
			}
		} catch (Exception e) {
			logger.error("[JYVoice.sendGetRequest(" + url + ") Exception]", e);
		} finally {
			httpmethod.releaseConnection();
		}
		return resp;
	}
}
