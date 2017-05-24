package com.sioo.thread;

import com.sioo.cache.RepeatMobileCache;
import com.sioo.cache.RptRatioConfigCache;
import com.sioo.log.LogInfo;
import org.apache.log4j.Logger;

import java.util.TimerTask;

public class ClearRepeatMobileCache extends TimerTask {
	private static Logger log = Logger.getLogger(ClearRepeatMobileCache.class);

	@Override
	public void run() {
		try {
			log.info("[ClearRepeatMobileCache] delete all repeat mobile cache, start...");
			RepeatMobileCache.getInstance().deleteRepeatMobile();
			RptRatioConfigCache.getInstance().deleteAllRptRatioUserSend();
			log.info("[ClearRepeatMobileCache] delete all repeat mobile cache, end...");
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("清除重号过滤缓存异常", "[ClearRepeatMobileCache.run() Exception]" + LogInfo.getTrace(e));
		}
	}
}
