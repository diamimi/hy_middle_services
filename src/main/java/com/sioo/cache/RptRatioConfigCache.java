package com.sioo.cache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.sioo.dao.SysCacheDao;
import com.sioo.log.LogInfo;
import com.sioo.util.SmsCache;

/***
 * 扣量信息缓存
 * @author Administrator
 *
 */
public class RptRatioConfigCache {
	private static Logger log = Logger.getLogger(RptRatioConfigCache.class);
	private static RptRatioConfigCache rptRatioConfigCache = null;
	public static RptRatioConfigCache getInstance() {
		if (rptRatioConfigCache != null) {
			return rptRatioConfigCache;
		}
		synchronized (RptRatioConfigCache.class) {
			if (rptRatioConfigCache == null) {
				rptRatioConfigCache = new RptRatioConfigCache();
			}
		}
		return rptRatioConfigCache;
	}
	
	/***
	 * 加载扣量信息
	 * @param uid
	 */
	public void loadRptRatioConfig(Integer uid) {
		try {
			List<Map<String, Object>> rptRatioConfigList = SysCacheDao.getInstance().findSmsRptRatioConfig(uid);
			if (null != rptRatioConfigList) {
				for (Map<String, Object> map : rptRatioConfigList) {
					SmsCache.RPT_RATIO_CONFIG.put((Integer) map.get("uid"), map);
				}
				log.info("扣量用户加载【" + rptRatioConfigList.size() + "】个");
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("加载扣量用户信息异常", "[ChannelCache.loadRptRatioConfig("+uid+") ]" + LogInfo.getTrace(e));
		}
	}
	
	/***
	 * 重新加载扣量信息
	 */
	public void reloadRptRatioConfig() {
		try {
			List<Map<String, Object>> rptRatioConfigList = SysCacheDao.getInstance().findSmsRptRatioConfig(0);
			if (null != rptRatioConfigList) {
				Map<Integer, Map<String, Object>> newMap = new ConcurrentHashMap<Integer, Map<String, Object>>();
				for (Map<String, Object> map : rptRatioConfigList) {
					newMap.put((Integer) map.get("uid"), map);
				}
				SmsCache.RPT_RATIO_CONFIG = newMap;
				log.info("扣量用户重新加载【" + rptRatioConfigList.size() + "】个");
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("重新加载扣量用户信息异常", "[ChannelCache.reloadRptRatioConfig() ]" + LogInfo.getTrace(e));
		}
	}
	
	
	/***
	 * 获取用户扣量信息
	 * @param uid
	 * @return
	 */
	public Map<String, Object> getRptRatioConfig(Integer uid) {
		try {
			if(SmsCache.RPT_RATIO_CONFIG.containsKey(uid)){
				return SmsCache.RPT_RATIO_CONFIG.get(uid);
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("获取用户扣量信息异常", "[ChannelCache.getRptRatioConfig("+uid+") ]" + LogInfo.getTrace(e));
		}
		return null;
	}
	
	
	/***
	 * 清空扣量用户发送条数
	 */
	public void deleteAllRptRatioUserSend(){
//		if(!SmsCache.RPT_RATIO_USER_SEND.isEmpty()){
//			for (Integer key : SmsCache.RPT_RATIO_USER_SEND.keySet()) {
//				SmsCache.RPT_RATIO_USER_SEND.put(key, new ConcurrentHashMap<String, AtomicLong>());
//			}
//		}
		try {
			for (Map.Entry<Integer, Map<String, AtomicLong>> entry : SmsCache.RPT_RATIO_USER_SEND.entrySet()) {
				SmsCache.RPT_RATIO_USER_SEND.remove(entry.getKey());
				log.info("清除扣量用户:"+entry.getKey());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage(),e);
		}
		log.info("清除扣量用户发送条数成功！");
	}
}
