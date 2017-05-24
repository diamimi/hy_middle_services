package com.sioo.service.alert;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

public class UrlConnection {
	private static Logger log = Logger.getLogger(UrlConnection.class);

	public static String doGetRequest(String url) {
		try {
			HttpClient httpClient = new HttpClient();
			HttpMethod getMethod = new GetMethod(url);
			int statusCode = httpClient.executeMethod(getMethod);
			if (statusCode == HttpStatus.SC_OK) {
				return getMethod.getResponseBodyAsString();
			}
		} catch (Exception e) {
			log.error("[UrlConnection.doGetRequest(" + url + ") Exception]", e);
		}
		return null;
	}
}