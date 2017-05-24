package com.sioo.sms.handle.taskloop;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.sioo.cache.ReleaseTemplateCache;
import com.sioo.log.LogInfo;
import com.sioo.util.SmsCache;

/****
 * 签名缓存
 * 
 * @author OYJM
 * @date 2016年9月7日
 *
 */
public class SyncReleaseTemplate {
	private static Logger log = Logger.getLogger(SyncReleaseTemplate.class);

	/**
	 * 同步缓存，一小时同步一次
	 * 
	 * */
	public static void sysnCache() {
		log.info("SyncReleaseTemplate ScheduledThreadPoolExecutor start...");
		// 构造一个ScheduledThreadPoolExecutor对象，并且设置它的容量为1
		ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
		// 延迟执行定时任务，同步缓存
		exec.scheduleAtFixedRate(new Runnable() {
			public void run() {
				try {
					// 判断是否系统暂停了
					if (!SmsCache.CONTROL) {
						return;
					}
					ReleaseTemplateCache.getInstance().loadReleaseTemplate(0);
				} catch (Exception e) {
					LogInfo.getLog().errorAlert("同步审核模板缓存信息异常", "[SyncReleaseTemplate.sysnCache() Exception]" + LogInfo.getTrace(e));
				}
			}
		}, 600, 600, TimeUnit.SECONDS); // 10分钟后执行，每10分钟执行一次
	}

}