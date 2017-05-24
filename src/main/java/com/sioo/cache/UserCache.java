package com.sioo.cache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sioo.util.Md5Util;
import org.apache.log4j.Logger;

import com.sioo.dao.SysCacheDao;
import com.sioo.log.LogInfo;
import com.sioo.servlet.HttpSubmitServer;
import com.sioo.util.SmsCache;
import com.sioo.util.UpdateCacheConstant.METHOD;

/****
 * 用户缓存操作类
 * 
 * @author OYJM
 * @date 2016年9月28日
 *
 */
public class UserCache {
	private static Logger log = Logger.getLogger(UserCache.class);
	private static UserCache userCache = null;

	public static UserCache getInstance() {
		if (userCache != null) {
			return userCache;
		}
		synchronized (UserCache.class) {
			if (userCache == null) {
				userCache = new UserCache();
			}
		}
		return userCache;
	}

	/****
	 * 加载用户
	 * 
	 */
	public void loadUser() {
		try {
			List<Map<String, Object>> listUid = SysCacheDao.getInstance().findSmsUser();
			for (Map<String, Object> user : listUid) {
				if (user.get("id") == null) {
					continue;
				}
				int uid = (Integer) user.get("id");
				String dpwd = user.get("dpwd") == null ? "" : user.get("dpwd").toString();
				String username = user.get("username") == null ? "" : user.get("username").toString();
				user.put("dpwd", Md5Util.getMD5(username + dpwd).toLowerCase());
				user.put("pwd", dpwd);

				SmsCache.USER.put(uid, user);
				SmsCache.USER_SMS.put(uid, user.get("sms") == null ? 0 : (Integer) user.get("sms"));
				SmsCache.USER_SMS_YUKOU.put(uid, user.get("sms") == null ? 0 : (Integer) user.get("sms"));
			}
			log.info("用户信息加载【" + listUid.size() + "】个");
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("加载用户信息缓存异常", "[UserCache.loadUser() ]" + LogInfo.getTrace(e));
		}
	}

	/****
	 * 重新加载用户
	 * 
	 */
	public void reloadUser() {
		try {
			List<Map<String, Object>> userList = SysCacheDao.getInstance().findSmsUser();
			if (userList != null) {
				Map<Integer, Map<String, Object>> currentUserMap = new ConcurrentHashMap<Integer, Map<String, Object>>();
				for (Map<String, Object> user : userList) {
					int uid = (Integer) user.get("id");
					String dpwd = (String) user.get("dpwd");
					String username = (String) user.get("username");
					user.put("dpwd", Md5Util.getMD5(username + dpwd).toLowerCase());
					user.put("pwd", dpwd);

					currentUserMap.put(uid, user);
				}
				SmsCache.USER = currentUserMap;
				log.info("用户信息重新加载【" + userList.size() + "】个");
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("重新加载用户信息缓存异常", "[UserCache.reloadUser() ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 根据UID在缓存中获取用户信息
	 * 
	 * @param uid
	 * @return
	 */
	public Map<String, Object> getUser(Integer uid) {
		try {
			return SmsCache.USER.get(uid);
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("获取用户信息缓存异常", "[UserCache.getUser(" + uid + ") ]" + LogInfo.getTrace(e));
		}
		return null;
	}


	/****
	 * 添加或修改用户缓存
	 * 
	 * @param uid
	 */
	public void addOrUpdateUser(Integer uid) {
		try {
			Map<String, Object> user = SysCacheDao.getInstance().findSmsUserByUid(uid);
			if (user != null) {
				String dpwd = (String) user.get("dpwd");
				String username = (String) user.get("username");
				user.put("dpwd", Md5Util.getMD5(username + dpwd).toLowerCase());
				user.put("pwd", dpwd);
				Map<String, Object> u = SmsCache.USER.get(uid);
				if (u != null) {
					SmsCache.USER.remove(uid);
				}
				SmsCache.USER.put(uid, user);
				log.info("加载用户信息; uid:" + uid);
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("添加或修改用户信息缓存异常", "[UserCache.addOrUpdateUser(" + uid + ") ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 根据UID删除对应得用户缓存
	 * 
	 * @param uid
	 */
	public void deleteUser(Integer uid) {
		try {
			if (SmsCache.USER.containsKey(uid)) {
				SmsCache.USER.remove(uid);
				log.info("删除用户信息; uid:" + uid);
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("删除用户信息缓存异常", "[UserCache.deleteUser(" + uid + ") ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 接口修改用户入口
	 * 
	 * @param method
	 * @param uid
	 */
	public String excute(Integer method, Integer uid) {
		String result = HttpSubmitServer.FAIL;
		try {
			switch (method) {
			case METHOD.ADD:
				addOrUpdateUser(uid);
				result = HttpSubmitServer.SUCC;
				break;
			case METHOD.UPDATE:
				addOrUpdateUser(uid);
				result = HttpSubmitServer.SUCC;
				break;
			case METHOD.DELETE:
				deleteUser(uid);
				result = HttpSubmitServer.SUCC;
				break;
			case METHOD.RELOAD:
				reloadUser();
				result = HttpSubmitServer.SUCC;
				break;
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("修改用户信息缓存异常", "[UserCache.excute(" + method + "," + uid + ") ]" + LogInfo.getTrace(e));
			result = HttpSubmitServer.FAIL;
		}

		return result;
	}
}
