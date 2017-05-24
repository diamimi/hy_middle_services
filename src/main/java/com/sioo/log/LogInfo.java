package com.sioo.log;

import com.sioo.service.alert.JYVoice;
import com.sioo.util.SmsCache;
import com.yxtsms.util.EnsmsSendNoInitException;
import com.yxtsms.util.HttpRequestProxy;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;

/****
 * 日志操作类
 * 
 * @author OYJM
 * @date 2016年9月18日
 *
 */
public class LogInfo {

	/***
	 * 私有化构造函数
	 */
	private LogInfo() {
	}

	private static LogInfo logInfo = null;

	/***
	 * 获取单例模式的当前类
	 * 
	 * @return
	 */
	public static LogInfo getLog() {
		if (null == logInfo) {
			logInfo = new LogInfo();
		}
		return logInfo;
	}

	private Logger log_error = Logger.getLogger(LogInfo.class);
	private Logger log_errorData = Logger.getLogger("errorData");

	public void errorAlert(String smsAlert, String log) {
		try {
			if (log != null && !log.isEmpty() && log.contains("Error")) {
				if (System.currentTimeMillis() - SmsCache.LOG_TIME_SIGN < 60000) {
					SmsCache.LOG_TIME_SIGN = System.currentTimeMillis();
					if (SmsCache.LOG_TIME_COUNT > 3) {
						log_error.error(log);
						return;
					} else {
						SmsCache.LOG_TIME_COUNT++;
					}
				} else {
					SmsCache.LOG_TIME_SIGN = System.currentTimeMillis();
					SmsCache.LOG_TIME_COUNT = 1;
				}
				if (SmsCache.alertMsgFlag) {
					log_error.info("发送告警：" + smsAlert);
					JYVoice.sendVoiceAlert(smsAlert);
					Calendar c = Calendar.getInstance();
					int time = c.get(Calendar.HOUR_OF_DAY);
					try {
						HttpRequestProxy.initSend("XiAoCmpp", "12345678", "http://221.178.190.171:8044/Yxtsms-Interface/SendMsg");
						HttpRequestProxy.initBalance("XiAoCmpp", "12345678", "http://221.178.190.171:8044/Yxtsms-Interface/GetMoney");
						if (time >= 22 || time < 7) {
							String result = HttpRequestProxy.sendSmsToEnsms(smsAlert + "【希奥】", "30032", SmsCache.alertMsgNightMobiles);// 夜间报警提醒
							log_error.info(" -info- " + result);
						} else {
							String result = HttpRequestProxy.sendSmsToEnsms(smsAlert + "【希奥】", "30032", SmsCache.alertMsgDayMobiles);// 日间报警提醒
							log_error.info(" -info- " + result);
						}
					} catch (EnsmsSendNoInitException e) {
						log_error.error("[LogInfo.errorAlert() EnsmsSendNoInitException]; data: " + log, e);
					} catch (IOException e) {
						log_error.error("[LogInfo.errorAlert() IOException]; data: " + log, e);
					}
				}
			}
			log_error.error(log);
		} catch (Exception ex) {
			log_error.error("[LogInfo.errorAlert() Exception]; data: " + log, ex);
		}
	}

	public void errorData(String data) {
		// this.errorAlert(log, null);
		if (data != null) {
			log_errorData.error(data);
		}
	}

	public static String getTrace(Throwable t) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		t.printStackTrace(writer);
		StringBuffer buffer = stringWriter.getBuffer();
		return buffer.toString();
	}


}
