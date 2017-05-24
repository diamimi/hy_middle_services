package com.sioo.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sioo.util.EhcacheUtil;
import org.apache.log4j.Logger;

import com.sioo.dao.SysCacheDao;
import com.sioo.log.LogInfo;
import com.sioo.util.SmsCache;
import com.sioo.util.UpdateCacheConstant.METHOD;

/***
 * 短信提醒信息
 * 
 * @author OYJM
 * @date 2016年12月3日
 *
 */
public class UserSmsAlertCache {
	private static Logger log = Logger.getLogger(UserSmsAlertCache.class);
	private static UserSmsAlertCache userSmsAlertCache = null;

	public static UserSmsAlertCache getInstance() {
		if (userSmsAlertCache != null) {
			return userSmsAlertCache;
		}
		synchronized (UserSmsAlertCache.class) {
			if (userSmsAlertCache == null) {
				userSmsAlertCache = new UserSmsAlertCache();
			}
		}
		return userSmsAlertCache;
	}

	/** 加载用户短信提醒信息 */
	public void loadUserSmsAlert(int uid) {
		try {
			Map<Integer, Map<String, Object>> USER_ALERT=null;
			if(SmsCache.USER_ALERT_INIT){
				USER_ALERT=(Map<Integer, Map<String, Object>>) EhcacheUtil.getInstance().get("sms","USER_ALERT");
				SmsCache.USER_ALERT_INIT=false;
			}
			if(USER_ALERT==null||USER_ALERT.size()==0){
				List<Map<String, Object>> alertList = SysCacheDao.getInstance().findSmsUserAlert(uid);
				for (Map<String, Object> alert : alertList) {
					SmsCache.USER_ALERT.put((Integer) alert.get("uid"), alert);
				}
				log.info("加载用户"+uid+"短信提醒【" + alertList.size() + "】个");
			}else {
				SmsCache.USER_ALERT=USER_ALERT;
				log.info("从缓存加载用户"+uid+"短信提醒【" + USER_ALERT.size() + "】个");
			}

		} catch (Exception e) {
			LogInfo.getLog().errorAlert("加载用户短信提醒信息异常", "[UserSmsCache.loadUserSmsAlert(" + uid + ") ]" + LogInfo.getTrace(e));
		}
	}

	/** 获取用户短信提醒 key:mobile,num,uid */
	public Map<String, Object> getSmsUserAlert(int uid) {
		try {
			return SmsCache.USER_ALERT.get(uid);
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("获取用户短信提醒信息异常", "[UserSmsCache.getSmsUserAlert(" + uid + ") ]" + LogInfo.getTrace(e));
		}
		return null;
	}

	public void setSmsUserAlert(int uid,Map<String, Object> map) {
		try {
			SmsCache.USER_ALERT.put(uid,map);
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("设置用户短信提醒信息异常", "[UserSmsCache.setSmsUserAlert(" + uid + ") ]" + LogInfo.getTrace(e));
		}
	}
	
	/** 删除用户短信提醒 */
	public void delSmsUserAlert(int uid) {
		try {
			if(SmsCache.USER_ALERT.containsKey(uid)){
				SmsCache.USER_ALERT.remove(uid);
			}
			log.info("删除用户"+uid+"短信提醒; uid:" + uid);
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("删除用户短信提醒信息异常", "[UserSmsCache.delSmsUserAlert(" + uid + ") ]" + LogInfo.getTrace(e));
		}
	}

	/** 添加或更新短信提醒 */
	public void addOrUpdSmsUserAlert(int uid, String mobile, int num) {
		try {
			Map<String, Object> alert = SmsCache.USER_ALERT.get(uid);
			if (null == alert) {
				alert = new HashMap<String, Object>();
			}
			alert.put("uid", uid);
			alert.put("mobile", mobile);
			alert.put("num", num);
			// RedisConfig.getInstance().getOpsForHash().put(ConstantSys.USER_SMS_ALERT,
			// uid, alert);
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("更新用户短信提醒信息异常", "[UserSmsAlertCache.addOrUpdSmsUserAlert(" + uid + "," + mobile + "," + num + ") ]" + LogInfo.getTrace(e));
		}
	}
	
	public void excute(Integer method, Integer uid) {
		try {
			if(method == METHOD.ADD || method == METHOD.UPDATE){
				loadUserSmsAlert(uid);
			}else if(method == METHOD.DELETE){
				delSmsUserAlert(uid);
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("修改用户短信提醒信息异常", "[UserSmsAlertCache.excute(" + method + "," + uid + ") ]" + LogInfo.getTrace(e));
		}
	}
}
