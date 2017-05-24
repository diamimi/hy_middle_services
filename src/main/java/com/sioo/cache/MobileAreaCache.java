package com.sioo.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sioo.util.EhcacheUtil;
import org.apache.log4j.Logger;

import com.sioo.dao.SysCacheDao;
import com.sioo.log.LogInfo;
import com.sioo.util.ConstantSys;
import com.sioo.util.SmsCache;
import com.sioo.util.UpdateCacheConstant.METHOD;

/****
 * 归属地缓存操作类
 * 
 * @author OYJM
 * @date 2016年9月28日
 *
 */
public class MobileAreaCache {
	private static Logger log = Logger.getLogger(MobileAreaCache.class);
	private static MobileAreaCache mobileAreaCache = null;

	public static MobileAreaCache getInstance() {
		if (mobileAreaCache != null) {
			return mobileAreaCache;
		}
		synchronized (MobileAreaCache.class) {
			if (mobileAreaCache == null) {
				mobileAreaCache = new MobileAreaCache();
			}
		}
		return mobileAreaCache;
	}

	/***
	 * 加载归属地
	 * 
	 * @param provincecode
	 */
	public void loadMobileArea(Integer provincecode,Integer citycode) {
		try {
			Map<Integer, Map<Integer, Map<String, Object>>> currentMap=null;
			if(SmsCache.MOBILE_AREA_INIT){
				currentMap = (Map<Integer, Map<Integer, Map<String, Object>>>) EhcacheUtil.getInstance().get("sms", "MOBILE_AREA");
				SmsCache.MOBILE_AREA_INIT=false;
			}
			if (currentMap == null || currentMap.size() == 0) {
				currentMap = new ConcurrentHashMap<>();
				List<Map<String, Object>> mobileAreaList = SysCacheDao.getInstance().findSmsMobileArea(provincecode, citycode);
				if (null != mobileAreaList) {
					Map<Integer, Map<String, Object>> map = null;
					for (Map<String, Object> smsMobileAreaMap : mobileAreaList) {
						Integer number = (Integer) smsMobileAreaMap.get("number");
						Integer key = Integer.valueOf(number.toString().substring(0, ConstantSys.MOBILE_LOCATION_SUB_POSITION));
						map = currentMap.get(key);
						if (map == null) {
							map = new HashMap<Integer, Map<String, Object>>();
						}
						if (!map.containsKey(number)) {
							map.put(number, smsMobileAreaMap);
							currentMap.put(key, map);
						}
					}
					SmsCache.MOBILE_AREA = currentMap;
					log.info("归属地加载【" + mobileAreaList.size() + "】个");
				}
			} else {
				SmsCache.MOBILE_AREA = currentMap;
				log.info("归属地从缓存加载【" + currentMap.size() + "】个");
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("加载归属地缓存异常", "[MobileAreaCache.loadMobileArea(" + provincecode + ") ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 重新加载归属地
	 * 
	 */
	public void reloadMobileArea() {
		this.loadMobileArea(0,null);
	}

	/***
	 * 获取归属地信息
	 * 
	 * @param mobile
	 * @return
	 */
	public Map<String, Object> getMobileArea(Long mobile) {
		try {
			if (mobile == null) {
				return null;
			}
			Integer mobileLocationKey = Integer.valueOf(mobile.toString().substring(0, ConstantSys.MOBILE_LOCATION_SUB_POSITION));
			Integer mobileLocation = Integer.valueOf(mobile.toString().substring(0, ConstantSys.MOBILE_LOCATION_POSITION));
			if (SmsCache.MOBILE_AREA.containsKey(mobileLocationKey)) {
				if (SmsCache.MOBILE_AREA.get(mobileLocationKey).containsKey(mobileLocation)) {
					return SmsCache.MOBILE_AREA.get(mobileLocationKey).get(mobileLocation);
				}
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("获取归属地缓存异常", "[MobileAreaCache.getMobileArea(" + mobile + ") ]" + LogInfo.getTrace(e));
		}
		return null;
	}

	/***
	 * 接口修改归属地入口
	 * 
	 * @param method
	 * @param mobile
	 * @param id
	 */
	public void excute(Integer method, Integer mobile, Integer id) {
		try {
			Integer key = Integer.valueOf(mobile.toString().substring(0, ConstantSys.MOBILE_LOCATION_SUB_POSITION));
			if (method.equals(METHOD.ADD) || method.equals(METHOD.UPDATE) || method.equals(METHOD.RELOAD)) {
				Map<String, Object> smsMobileAreaMap = SysCacheDao.getInstance().findSmsMobileAreaById(id);
				if (smsMobileAreaMap != null) {
					Integer number = (Integer) smsMobileAreaMap.get("number");
					Map<Integer, Map<String, Object>> map = null;
					if (SmsCache.MOBILE_AREA.containsKey(key)) {
						if (!SmsCache.MOBILE_AREA.get(key).containsKey(number)) {
							map = SmsCache.MOBILE_AREA.get(key);
							map.put(number, smsMobileAreaMap);
							SmsCache.MOBILE_AREA.put(key, map);
						}
					} else {
						map = new HashMap<Integer, Map<String, Object>>();
						map.put(number, smsMobileAreaMap);
						SmsCache.MOBILE_AREA.put(key, map);
					}
				}
			} else if (method.equals(METHOD.DELETE)) {
				Map<Integer, Map<String, Object>> map = SmsCache.MOBILE_AREA.get(key);
				map.remove(mobile.toString());
				SmsCache.MOBILE_AREA.put(key, map);
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("修改归属地缓存异常", "[MobileAreaCache.excute(" + method + "," + mobile + "," + id + ") ]" + LogInfo.getTrace(e));
		}
	}
}
