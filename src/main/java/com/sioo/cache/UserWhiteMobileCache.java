package com.sioo.cache;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sioo.dao.SysCacheDao;
import com.sioo.log.LogInfo;
import com.sioo.util.ConstantSys;
import com.sioo.util.SmsCache;
import com.sioo.util.UpdateCacheConstant.METHOD;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * 用户白名单缓存操作类
 * 
 * @author OYJM
 * @date 2016年9月27日
 *
 */
public class UserWhiteMobileCache {
	private static Logger log = Logger.getLogger(UserWhiteMobileCache.class);
	private static UserWhiteMobileCache userWhiteMobileCache = null;

	public static UserWhiteMobileCache getInstance() {
		if (userWhiteMobileCache != null) {
			return userWhiteMobileCache;
		}
		synchronized (UserWhiteMobileCache.class) {
			if (userWhiteMobileCache == null) {
				userWhiteMobileCache = new UserWhiteMobileCache();
			}
		}
		return userWhiteMobileCache;
	}

	/***
	 * 加载用户白名单
	 * 
	 * @param uid
	 */
	public void loadUserWhiteMobile(Integer uid) {
		try {
			List<Map<String, Object>> mobileList = SysCacheDao.getInstance().findUserWhiteMobile(uid);
			Map<String, List<Long>> whiteMobileMap = new HashMap<String, List<Long>>();
			if (null != mobileList) {
				List<Long> array = null;
				for (Map<String, Object> map : mobileList) {
					String md = map.get("md").toString();
					Long mobile = Long.parseLong(md);
					String tempKey = map.get("uid").toString() + md.substring(0, ConstantSys.MOBILE_POSITION);
					array = whiteMobileMap.get(tempKey);
					if (array == null) {
						array = new ArrayList<Long>();
					}
					if (!array.contains(mobile)) {
						array.add(mobile);
						whiteMobileMap.put(tempKey, array);
					}
				}
				SmsCache.USER_WHITE_MOBILE = whiteMobileMap;
				log.info("用户白名单加载【" + mobileList.size() + "】个");
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("加载用户白名单缓存异常", "[UserWhiteMobileCache.loadUserWhiteMobile(" + uid + ") ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 重新加载用户白名单
	 * 
	 */
	public void reloadUserWhiteMobile() {
		this.loadUserWhiteMobile(0);
	}



	/***
	 * 添加用户白名单
	 * 
	 * @param array
	 */
	public void addUserWhiteMobile(JSONArray array) {
		try {
			for (Object obj : array.toArray()) {
				JSONObject json = (JSONObject) obj;
				Integer uid = json.getInteger("uid");
				Long mobile = json.getLong("mobile");
				String tempKey = uid + mobile.toString().substring(0, ConstantSys.MOBILE_POSITION);
				if (SmsCache.USER_WHITE_MOBILE.containsKey(tempKey)) {
					if (!SmsCache.USER_WHITE_MOBILE.get(tempKey).contains(mobile)) {
						SmsCache.USER_WHITE_MOBILE.get(tempKey).add(mobile);
						log.info("添加用户白名单; uid: " + uid + ", mobile:" + mobile);
					}
				} else {
					List<Long> list = new ArrayList<Long>();
					list.add(mobile);
					SmsCache.USER_WHITE_MOBILE.put(tempKey, list);
					log.info("添加用户白名单; uid: " + uid + ", mobile:" + mobile);
				}
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("添加用户白名单缓存异常", "[UserWhiteMobileCache.addUserWhiteMobile(" + array.toJSONString() + ") ]" + LogInfo.getTrace(e));
		}
	}

	/****
	 * 删除用户白名单
	 * 
	 * @param array
	 */
	public void deleteUserWhiteMobile(JSONArray array) {
		try {
			for (Object obj : array.toArray()) {
				JSONObject json = (JSONObject) obj;
				Integer uid = json.getInteger("uid");
				Long mobile = json.getLong("mobile");
				String tempKey = uid + mobile.toString().substring(0, ConstantSys.MOBILE_POSITION);
				if (SmsCache.USER_WHITE_MOBILE.containsKey(tempKey) && SmsCache.USER_WHITE_MOBILE.get(tempKey).contains(mobile)) {
					SmsCache.USER_WHITE_MOBILE.get(tempKey).remove(mobile);
					log.info("删除用户白名单; uid: " + uid + ", mobile:" + mobile);
				}
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("删除用户白名单缓存异常", "[UserWhiteMobileCache.deleteUserWhiteMobile(" + array.toJSONString() + ") ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 验证手机号是否为用户白名单
	 * 
	 * @param uid
	 * @param mobile
	 * @return true为白名单
	 */
	public boolean isUserWhiteMobile(Integer uid, Long mobile) {
		try {
			List<Long> mobileList = SmsCache.USER_WHITE_MOBILE.get(uid + mobile.toString().substring(0, ConstantSys.MOBILE_POSITION));
			if (null != mobileList && mobileList.size() > 0) {
				if (mobileList.contains(mobile)) {
					log.info("触发用户白名单; uid: " + uid + ", mobile:" + mobile);
					return true;
				}
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("校验用户白名单缓存异常", "[UserWhiteMobileCache.isUserWhiteMobile(" + uid + "," + mobile + ") ]" + LogInfo.getTrace(e));
		}
		return false;
	}

	/***
	 * 接口修改用户白名单入口
	 * 
	 * @param method
	 * @param array
	 */
	public void excute(Integer method, JSONArray array) {
		try {
			if (method == null || array.isEmpty()) {
				return;
			}
			if (method == METHOD.ADD || method == METHOD.UPDATE) {
				addUserWhiteMobile(array);
			} else if (method == METHOD.DELETE) {
				deleteUserWhiteMobile(array);
			} else if (method == METHOD.RELOAD) {
				loadUserWhiteMobile(0);
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("修改用户白名单缓存异常", "[UserWhiteMobileCache.excute(" + method + "," + array.toJSONString() + ") ]" + LogInfo.getTrace(e));
		}
	}
}
