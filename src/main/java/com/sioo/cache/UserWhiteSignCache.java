package com.sioo.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sioo.dao.SysCacheDao;
import com.sioo.log.LogInfo;
import com.sioo.util.SmsCache;
import com.sioo.util.UpdateCacheConstant.METHOD;

/***
 * 用户白签名缓存操作类
 * 
 * @author OYJM
 * @date 2016年9月27日
 *
 */
public class UserWhiteSignCache {
	private static Logger log = Logger.getLogger(UserWhiteSignCache.class);
	private static UserWhiteSignCache userWhiteSignCache = null;

	public static UserWhiteSignCache getInstance() {
		if (userWhiteSignCache != null) {
			return userWhiteSignCache;
		}
		synchronized (UserWhiteSignCache.class) {
			if (userWhiteSignCache == null) {
				userWhiteSignCache = new UserWhiteSignCache();
			}
		}
		return userWhiteSignCache;
	}

	/***
	 * 加载用户白签名
	 * 
	 * @param uid
	 */
	public void loadUserWhiteSign(Integer uid) {
		try {
			List<Map<String, Object>> userSignList = SysCacheDao.getInstance().findUserWhiteSign(uid);
			Map<Integer, List<String>> userSignMap = new HashMap<Integer, List<String>>();
			if (null != userSignList) {
				List<String> array = new ArrayList<String>();
				for (Map<String, Object> map : userSignList) {
					int key = (Integer) map.get("uid");
					String sign = map.get("sign").toString();
					array = userSignMap.get(key);
					if (array == null) {
						array = new ArrayList<String>();
					}
					if (!array.contains(sign)) {
						array.add(sign);
						userSignMap.put(key, array);
					}
				}
				SmsCache.USER_WHITE_SIGN = userSignMap;
				log.info("用户白签名加载【" + userSignList.size() + "】个");
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("加载用户白签名缓存异常", "[UserWhiteSignCache.loadUserWhiteSign(" + uid + ") ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 重新加载用户白签名
	 * 
	 */
	public void reloadUserWhiteSign() {
		this.loadUserWhiteSign(0);
	}

	/***
	 * 添加用户白签名
	 * 
	 * @param array
	 */
	public void addUserWhiteSign(JSONArray array) {
		try {
			for (Object obj : array.toArray()) {
				JSONObject json = (JSONObject) obj;
				Integer uid = json.getInteger("uid");
				String sign = json.getString("sign");
				List<String> list = null;
				if (SmsCache.USER_WHITE_SIGN.containsKey(uid)) {
					if (!SmsCache.USER_WHITE_SIGN.get(uid).contains(sign)) {
						list = SmsCache.USER_WHITE_SIGN.get(uid);
						list.add(sign);
					}
				} else {
					list = new ArrayList<String>();
					list.add(sign);
				}
				if (list != null) {
					SmsCache.USER_WHITE_SIGN.put(uid, list);
					log.info("添加用户白签名; uid: " + uid + ", sign:" + sign);
				}
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("添加用户白签名缓存异常", "[UserWhiteSignCache.addUserWhiteSign(" + array.toJSONString() + ") ]" + LogInfo.getTrace(e));
		}
	}

	/****
	 * 删除用户白签名
	 * 
	 * @param array
	 */
	public void deleteUserWhiteSign(JSONArray array) {
		try {
			for (Object obj : array.toArray()) {
				JSONObject json = (JSONObject) obj;
				Integer uid = json.getInteger("uid");
				String sign = json.getString("sign");
				if (SmsCache.USER_WHITE_SIGN.containsKey(uid) && SmsCache.USER_WHITE_SIGN.get(uid).contains(sign)) {
					List<String> list = SmsCache.USER_WHITE_SIGN.get(uid);
					list.remove(sign);
					SmsCache.USER_WHITE_SIGN.put(uid, list);
					log.info("删除用户白签名; uid: " + uid + ", sign:" + sign);
				}
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("删除用户白签名缓存异常", "[UserWhiteSignCache.deleteUserWhiteSign(" + array.toJSONString() + ") ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 验证签名是否为用户白签名
	 * 
	 * @param uid
	 * @param sign
	 * @return true为白签名
	 */
	public boolean isUserWhiteSign(Integer uid, String sign) {
		try {
			List<String> signList = SmsCache.USER_WHITE_SIGN.get(uid);
			if (null != signList && signList.size() > 0) {
				if (signList.contains(sign)) {
					log.info("触发用户白签名; uid: " + uid + ", sign:" + sign);
					return true;
				}
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("校验用户白签名异常", "[UserWhiteSignCache.isUserWhiteSign(" + uid + "," + sign + ") ]" + LogInfo.getTrace(e));
		}
		return false;
	}

	/***
	 * 接口修改用户白签名入口
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
				addUserWhiteSign(array);
			} else if (method == METHOD.DELETE) {
				deleteUserWhiteSign(array);
			} else if (method == METHOD.RELOAD) {
				loadUserWhiteSign(0);
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("修改用户白签名缓存异常", "[UserWhiteSignCache.excute(" + method + "," + array.toJSONString() + ") ]" + LogInfo.getTrace(e));
		}
	}
}
