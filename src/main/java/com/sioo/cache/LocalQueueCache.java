package com.sioo.cache;

import com.alibaba.fastjson.JSON;
import com.sioo.hy.cmpp.vo.ResultVo;
import com.sioo.hy.cmpp.vo.SendingVo;
import com.sioo.log.LogInfo;
import com.sioo.util.SmsCache;

/***
 * 本地缓存队列操作类
 * 
 * @author OYJM
 * @date 2016年12月3日
 *
 */
public class LocalQueueCache {
	private static LocalQueueCache localQueueCache = null;

	public static LocalQueueCache getInstance() {
		if (localQueueCache != null) {
			return localQueueCache;
		}
		synchronized (LocalQueueCache.class) {
			if (localQueueCache == null) {
				localQueueCache = new LocalQueueCache();
			}
		}
		return localQueueCache;
	}

	/***
	 * 添加发送结果
	 * 
	 * @param resultVo
	 */
	public void putResult(ResultVo resultVo) {
		try {
			SmsCache.QUEUE_RESULT.put(resultVo);
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("往本地队列添加发送结果异常", "[LocalQueueCache.putResult() ]; data: " + JSON.toJSONString(resultVo) + LogInfo.getTrace(e));
		}
	}

	/***
	 * 添加历史记录
	 * 
	 * @param resultVo
	 */
	public void putSendingHistory(SendingVo sendingVo) {
		try {
			SmsCache.QUEUE_SENDING_HISTORY.put(sendingVo);
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("往本地队列添加历史记录异常", "[LocalQueueCache.putSendingHistory() ]; data: " + JSON.toJSONString(sendingVo) + LogInfo.getTrace(e));
		}
	}

	/***
	 * 添加审核消息记录
	 * 
	 * @param resultVo
	 */
	public void putSendingRelease(SendingVo sendingVo) {
		try {
			SmsCache.QUEUE_SENDING_RELEASE.put(sendingVo);
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("往本地队列添加审核消息异常", "[LocalQueueCache.putSendingRelease() ]; data: " + JSON.toJSONString(sendingVo) + LogInfo.getTrace(e));
		}
	}

	/***
	 * 添加队列消息记录
	 * 
	 * @param sendingVo
	 */
	public void putSending(SendingVo sendingVo) {
		try {
			SmsCache.QUEUE_SENDING.put(sendingVo);
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("往本地队列添加队列消息异常", "[LocalQueueCache.putSending() ]; data: " + JSON.toJSONString(sendingVo) + LogInfo.getTrace(e));
		}
	}

	/***
	 * 添加报表消息记录
	 * 
	 * @param sendingVo
	 */
	public void putReport(SendingVo sendingVo) {
		try {
			SmsCache.QUEUE_REPORT.put(sendingVo);
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("往本地队列添加用户日报消息异常", "[LocalQueueCache.putReport() ]; data: " + JSON.toJSONString(sendingVo) + LogInfo.getTrace(e));
		}
	}

	/**
	 * 添加消息提醒记录
	 * 
	 * @param content
	 */
	public void putAlert(String content) {
		try {
			SmsCache.QUEUE_ALERT.put(content);
		} catch (InterruptedException e) {
			LogInfo.getLog().errorAlert("往本地队列添加消息提醒异常", "[LocalQueueCache.putAlert() ]; data: " + content + LogInfo.getTrace(e));
		}
	}
	
	public void putUnkouSms(SendingVo sendingVo){
		try {
			SmsCache.QUEUE_UNKOU_SMS.put(sendingVo);
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("往本地队列添加未扣费消息异常", "[LocalQueueCache.putUnkouSms() ]; data: " + JSON.toJSONString(sendingVo) + LogInfo.getTrace(e));
		}
	}
}
