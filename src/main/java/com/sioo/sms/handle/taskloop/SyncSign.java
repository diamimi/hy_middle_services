package com.sioo.sms.handle.taskloop;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.sioo.cache.UserSignCache;
import com.sioo.log.LogInfo;
import com.sioo.util.SmsCache;

/****
 * 签名缓存
 * 
 * @author OYJM
 * @date 2016年9月7日
 *
 */
public class SyncSign {
	private static Logger log = Logger.getLogger(SyncSign.class);

	/**
	 * 同步缓存，一小时同步一次
	 * 
	 * */
	public static void sysnCache() {
		log.info("SyncSign ScheduledThreadPoolExecutor start...");
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
					log.info("[reload sign] start...");
					UserSignCache.getInstance().reloadNewSign();
					log.info("[reload sign] success...");
				} catch (Exception e) {
					LogInfo.getLog().errorAlert("同步签名缓存信息异常", "[SyncSign.sysnCache() Exception]" + LogInfo.getTrace(e));
				}
			}
		}, 3600, 3600, TimeUnit.SECONDS); // 1小时后执行，每1小时执行一次
	}

	public static void main(String[] args) {
		UserSignCache.getInstance().loadUserSign(0, null);
		sysnCache();
	}
}