package com.sioo.thread;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.sioo.log.LogInfo;
import com.sioo.util.SmsCache;

/***
 * 短信提醒
 * 
 * @author OYJM
 * @date 2016年11月15日
 *
 */
public class SmsAlertThread implements Runnable {
	private Logger log = LogManager.getLogger(SmsAlertThread.class);

	public void run() {
		List<String> list = null;
		int size = 0;
		while (true) {
			try {
				// 如果为空，休眠2秒；否则休眠100毫秒
				if (SmsCache.QUEUE_ALERT.isEmpty()) {
					Thread.sleep(2000);
					continue;
				} else {
					Thread.sleep(100);
				}

				list = new ArrayList<String>();
				SmsCache.QUEUE_CHECK_ALERT_FLG = false;
				size = SmsCache.QUEUE_ALERT.drainTo(list, 500);
				if (size > 0) {
					for (int i = 0; i < list.size(); i++) {
						log.info("余额提醒[" + list.get(i) + "],Response:" + this.sendPostRequest(list.get(i)) + "}");
					}
				}
			} catch (Exception e) {
				LogInfo.getLog().errorAlert("添加短信余额提醒信息异常", "[SmsAlertThread.run() Exception]" + LogInfo.getTrace(e));
				if (list != null && !list.isEmpty()) {
					for (int i = 0; i < list.size(); i++) {
						LogInfo.getLog().errorData(list.get(i));
					}
				}
			} finally {
				SmsCache.QUEUE_CHECK_ALERT_FLG = true;
				// 释放内存
				if (list != null) {
					list.clear();
					list = null;
				}
			}
		}

	}

	/**
	 * 2017.4.21启用post方法
	 * @param json
	 * @return
	 * @throws Exception
	 */
	public String sendPostRequest(String json) throws Exception{
		JSONObject jsonObject=JSONObject.parseObject(json);
		String url=jsonObject.getString("url");
		String msg=jsonObject.getString("msg");
		String auth=jsonObject.getString("auth");
		String mobile=jsonObject.getString("mobile");
		String expid=jsonObject.getString("expid");
		String encode=jsonObject.getString("encode");
		String uid=jsonObject.getString("uid");
		String res = null;
		HttpMethod httpmethod = null;
		try {
			HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());
			httpmethod = new GetMethod(url);
			httpmethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
			NameValuePair[] data = {
					new NameValuePair("msg", msg),
					new NameValuePair("auth", auth),
					new NameValuePair("mobile", mobile),
					new NameValuePair("expid", expid),
					new NameValuePair("encode", encode),
					new NameValuePair("uid", uid)
			};

			httpmethod.setQueryString(data);
			int statusCode = client.executeMethod(httpmethod);
			if (statusCode == HttpStatus.SC_OK) {
				res = httpmethod.getResponseBodyAsString();
			}
		} catch (Exception e) {
			log.error("[sms alert 余额提醒消息发送异常]:" + e.getMessage(), e);
		} finally {
			if (httpmethod != null)
				httpmethod.releaseConnection();
		}
		return res;
	}

	/**
	 * get方法暂时不用
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public String sendGetRequest(String url) throws Exception {
		String res = null;
		HttpMethod httpmethod = null;
		try {
			HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());
			httpmethod = new GetMethod(url);
			httpmethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());

			int statusCode = client.executeMethod(httpmethod);
			if (statusCode == HttpStatus.SC_OK) {
				res = httpmethod.getResponseBodyAsString();
			}
		} catch (Exception e) {
			log.error("[sms alert 余额提醒消息发送异常]:" + e.getMessage(), e);
			throw e;
		} finally {
			if (httpmethod != null)
				httpmethod.releaseConnection();
		}
		return res;
	}

}
