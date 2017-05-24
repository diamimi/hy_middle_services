package com.sioo.serviceclient;

import com.sioo.util.AESTool;
import com.sioo.util.SignatureUtil;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

@Component
public class ServiceClient {
	private static Logger log = Logger.getLogger(ServiceClient.class);
	private static HttpClient client = null;


	private static MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

	static {
		client = new HttpClient(connectionManager);
		client.getHttpConnectionManager().getParams().setDefaultMaxConnectionsPerHost(20);
		client.getHttpConnectionManager().getParams().setMaxTotalConnections(48);
		client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
		client.getHttpConnectionManager().getParams().setSoTimeout(30000);
	}

	@Test
	public void test(){

	}

	public String excuteClient(int type, int method, String data) {
		Map<String, Object> paraMap = new HashMap<String, Object>();
		paraMap.put("type", type);
		paraMap.put("method", method);
		paraMap.put("data", data);
		//log.info(postString(URL_20, paraMap));
		return postString("http://101.227.68.21:8090/hy_middle_services/updateCache", paraMap);
	}
	

	


	/**
	 * 根据传入的uri和参数map拼接成实际uri
	 * 
	 * @param uri
	 * @param paraMap
	 * @return
	 */
	public String buildUri(String uri, Map<String, String> paraMap) {
		StringBuilder sb = new StringBuilder();
		uri = StringUtils.trim(uri);
		uri = StringUtils.removeEnd(uri, "/");
		uri = StringUtils.removeEnd(uri, "?");
		sb.append(uri);
		if (paraMap != null && !paraMap.isEmpty()) {
			sb.append("?");
			Iterator<Entry<String, String>> iterator = paraMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, String> pair = iterator.next();
				try {
					String keyString = pair.getKey();
					String valueString = pair.getValue();
					sb.append(keyString);
					sb.append("=");
					sb.append(valueString);
					sb.append("&");
				} catch (Exception e) {
					log.error(e, e);
				}
			}
		}
		return StringUtils.removeEnd(sb.toString(), "&");
	}

	/**
	 *
	 * 
	 * @param uri
	 * @param paraMap
	 * @return
	 */
	public String postString(String uri, Map<String, Object> paraMap) {
		HttpMethod httpMethod = null;
		String result = null;
		try {

			httpMethod = new PostMethod(StringUtils.isNotBlank(uri) ? uri : "http://101.227.68.21:8090/hy_middle_services/updateCache");
			HttpMethodParams params = new HttpMethodParams();
			params.setContentCharset("UTF-8");

			AESTool aes = new AESTool();
			SignatureUtil signatureUtil = new SignatureUtil();
			String appid = "sioo";
			String token = signatureUtil.findTokenById(appid);
			String key = aes.findKeyById(appid);
			long millis = System.currentTimeMillis();
			StringBuffer buffer = new StringBuffer("{");
			for (String mapkey : paraMap.keySet()) {
				buffer.append("\"").append(mapkey).append("\":").append(paraMap.get(mapkey)).append(",");
			}
			String json = buffer.toString();
			if (json != null && json.length() > 1) {
				json = json.substring(0, json.length() - 1);
			}
			json = json + "}";
			json = aes.encrypt(json, key);
			String lol = signatureUtil.digest(json, "MD5");
			String signature = signatureUtil.generateSignature(appid, token, lol, millis);
			NameValuePair[] param = { new NameValuePair("s", signature), new NameValuePair("a", appid), new NameValuePair("t", String.valueOf(millis)),
					new NameValuePair("l", lol), new NameValuePair("data", json) };
			httpMethod.setQueryString(param);

			int statusCode = client.executeMethod(httpMethod);
			// 执行getMethod
			if (statusCode != HttpStatus.SC_OK) {
				log.error("method failed" + httpMethod.getStatusLine());
				return result;
			}
			// 读取内容
			result = httpMethod.getResponseBodyAsString();
			log.info("response result:" + result);
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// 释放连接
			httpMethod.releaseConnection();
		}
		return result;
	}

	public static void main(String[] args) throws Exception {

		ServiceClient serviceClient = new ServiceClient();
		serviceClient.excuteClient(28, 2,
				"{\"screenType\":" + "0" + ",\"groupType\":" + 1 + ",\"groupId\":" + 6+ "," +
						"\"content\":\""+ URLEncoder.encode("/ADP [ADP]","UTF-8")+"\"}");


		/*String uri = "http://101.227.68.21:8090/hy_middle_services/updateCache";
		Map<String, Object> paraMap = new HashMap<String, Object>();
		paraMap.put("type", 1);
		paraMap.put("method", 2);
		paraMap.put("data", "{\\\"screenType\\\":\" + old.getScreenType() + \",\\\"groupType\\\":\" + delete + \",\\\"groupId\\\":\" + old.getGroupId()+ \",\\\"content\\\":\\\"\"+URLEncoder.encode(old.getBase(),\"UTF-8\")+\"\\\"}");

		System.out.println(serviceClient.postString(uri, paraMap));*/
	}

}
