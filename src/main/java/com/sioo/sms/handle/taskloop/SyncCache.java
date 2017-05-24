package com.sioo.sms.handle.taskloop;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.sioo.cache.ChannelBlackSignCache;
import com.sioo.cache.ChannelCache;
import com.sioo.cache.MobileAreaCache;
import com.sioo.cache.StrategyGroupCache;
import com.sioo.cache.UserBlackLocationCache;
import com.sioo.cache.UserBlackMobileCache;
import com.sioo.cache.UserBlackWordsCache;
import com.sioo.cache.UserCache;
import com.sioo.cache.UserMsgTemplateCache;
import com.sioo.cache.UserRouteCache;
import com.sioo.cache.UserSignCache;
import com.sioo.cache.UserWhiteMobileCache;
import com.sioo.cache.UserWhiteSignCache;
import com.sioo.log.LogInfo;
import com.sioo.util.SmsCache;

/****
 * 用户缓存
 * 
 * @author OYJM
 * @date 2016年9月7日
 *
 */
public class SyncCache {
	private static Logger log = Logger.getLogger(SyncCache.class);

	/**
	 * 同步缓存，一小时同步一次
	 * 
	 * */
	public static void sysnCache() {
		log.info("sysnCache ScheduledThreadPoolExecutor start...");
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
					log.info("[reload cache] start...");
					// 通道相关信息
					ChannelCache.getInstance().reloadChannel();
					ChannelCache.getInstance().loadGroupChannel(null);
//					ChannelBlackSignCache.getInstance().reloadChannelBlackSign();
					// 用户相关信息
					UserCache.getInstance().reloadUser();
					UserRouteCache.getInstance().reloadUserRoute();
					UserSignCache.getInstance().reloadUserSign();
					// 校验相关信息
					UserWhiteMobileCache.getInstance().reloadUserWhiteMobile();
					UserWhiteSignCache.getInstance().reloadUserWhiteSign();
					UserBlackMobileCache.getInstance().reloadUserBlackMobile();
					UserBlackWordsCache.getInstance().reloadUserBlackWords();
					UserMsgTemplateCache.getInstance().reloadUserMsgTemplate();
					StrategyGroupCache.getInstance().reloadUserStrategyGroup();
					StrategyGroupCache.getInstance().reloadStrategyGroup();
					MobileAreaCache.getInstance().reloadMobileArea();
					UserBlackLocationCache.getInstance().reloadBlackLocation();
					log.info("[reload cache] success...");
				} catch (Exception e) {
					LogInfo.getLog().errorAlert("同步缓存信息异常", "[SyncCache.sysnCache() Exception]" + LogInfo.getTrace(e));
				}
			}
		}, 60 * 60, 60 * 60, TimeUnit.SECONDS); // 一小时后执行，一小时执行一次
	}
}