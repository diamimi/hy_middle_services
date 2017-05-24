package com.sioo.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sioo.util.EhcacheUtil;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sioo.dao.SysCacheDao;
import com.sioo.log.LogInfo;
import com.sioo.util.ConstantSys;
import com.sioo.util.SmsCache;
import com.sioo.util.UpdateCacheConstant.METHOD;

/***
 * 用户黑名单缓存操作类
 * 
 * @author OYJM
 * @date 2016年9月27日
 *
 */
public class UserBlackMobileCache {
	private static Logger log = Logger.getLogger(UserBlackMobileCache.class);
	private static UserBlackMobileCache userBlackMobileCache = null;

	public static UserBlackMobileCache getInstance() {
		if (userBlackMobileCache != null) {
			return userBlackMobileCache;
		}
		synchronized (UserBlackMobileCache.class) {
			if (userBlackMobileCache == null) {
				userBlackMobileCache = new UserBlackMobileCache();
			}
		}
		return userBlackMobileCache;
	}

	/***
	 * 加载用户黑名单
	 * 
	 * @param uid
	 */
	public void loadUserBlackMobile(Integer uid) {
		try {
			Map<String, List<Long>> blackMobileMap =null;
			if(SmsCache.USER_BLACK_MOBILE_INIT){
				blackMobileMap = (Map<String, List<Long>>) EhcacheUtil.getInstance().get("sms", "USER_BLACK_MOBILE");
				SmsCache.USER_BLACK_MOBILE_INIT=false;
			}
			if (blackMobileMap == null || blackMobileMap.size() == 0) {
				blackMobileMap = new ConcurrentHashMap<>();
				List<Map<String, Object>> mobileList = SysCacheDao.getInstance().findUserBlackMobile(uid);
				if (null != mobileList) {
					List<Long> array = new ArrayList<Long>();
					for (Map<String, Object> map : mobileList) {
						String md = map.get("md").toString().trim();
						Long mobile = Long.parseLong(md);
						String tempKey = map.get("uid").toString() + md.substring(0, ConstantSys.MOBILE_POSITION);
						array = blackMobileMap.get(tempKey);
						if (array == null) {
							array = new ArrayList<Long>();
						}
						if (!array.contains(mobile)) {
							array.add(mobile);
							blackMobileMap.put(tempKey, array);
						}
					}
					SmsCache.USER_BLACK_MOBILE = blackMobileMap;
					log.info("用户黑名单加载【" + mobileList.size() + "】个");
				}
			} else {
				SmsCache.USER_BLACK_MOBILE = blackMobileMap;
				log.info("用户黑名单从缓存加载【" + blackMobileMap.size() + "】个");

			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("加载用户黑名单缓存异常", "[UserBlackMobileCache.loadUserBlackMobile(" + uid + ") ]" + LogInfo.getTrace(e));
		}
	}

	/****
	 * 重新加载用户黑名单
	 */
	public void reloadUserBlackMobile() {
		this.loadUserBlackMobile(0);
	}

	/***
	 * 添加用户黑名单
	 * 
	 * @param array
	 */
	public void addUserBlackMobile(JSONArray array) {
		try {
			for (Object obj : array.toArray()) {
				JSONObject json = (JSONObject) obj;
				Integer uid = json.getInteger("uid");
				Long mobile = json.getLong("mobile");
				String tempKey = uid + mobile.toString().substring(0, ConstantSys.MOBILE_POSITION);

				if (SmsCache.USER_BLACK_MOBILE.containsKey(tempKey)) {
					if (!SmsCache.USER_BLACK_MOBILE.get(tempKey).contains(mobile)) {
						List<Long> list = SmsCache.USER_BLACK_MOBILE.get(tempKey);
						list.add(mobile);
						SmsCache.USER_BLACK_MOBILE.put(tempKey, list);
						log.info("添加用户黑名单; uid:" + uid + ", mobile:" + mobile);
					}
				} else {
					List<Long> list = new ArrayList<>();
					list.add(mobile);
					SmsCache.USER_BLACK_MOBILE.put(tempKey, list);
					log.info("添加用户黑名单,新建list; uid:" + uid + ", mobile:" + mobile);
				}
				
				addAllUserBlackMobile(mobile);
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("添加用户黑名单缓存异常", "[UserBlackMobileCache.addUserBlackMobile(" + array.toJSONString() + ") ]" + LogInfo.getTrace(e));
		}
	}

	/****
	 * 删除用户黑名单
	 * 
	 */
	public void deleteUserBlackMobile(JSONArray array) {
		try {
			for (Object obj : array.toArray()) {
				JSONObject json = (JSONObject) obj;
				Integer uid = json.getInteger("uid");
				Long mobile = json.getLong("mobile");
				String tempKey = uid + mobile.toString().substring(0, ConstantSys.MOBILE_POSITION);
				if (SmsCache.USER_BLACK_MOBILE.containsKey(tempKey) && SmsCache.USER_BLACK_MOBILE.get(tempKey).contains(mobile)) {
					List<Long> list = SmsCache.USER_BLACK_MOBILE.get(tempKey);
					list.remove(mobile);
					SmsCache.USER_BLACK_MOBILE.put(tempKey, list);
					log.info("删除用户黑名单; uid:" + uid + ", mobile:" + mobile);
				}
				
				//删除全量用户黑名单
				deleteAllUserBlackMobile(mobile);
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("删除用户黑名单缓存异常", "[UserBlackMobileCache.deleteUserBlackMobile(" + array.toJSONString() + ") ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 验证手机号是否为用户黑名单
	 * 
	 * @param uid
	 * @param mobile
	 * @return true为黑名单
	 */
	public boolean isUserBlackMobile(Integer uid, Long mobile) {
		try {
			List<Long> mobileList = SmsCache.USER_BLACK_MOBILE.get(uid + mobile.toString().substring(0, ConstantSys.MOBILE_POSITION));
			if (null != mobileList && mobileList.size() > 0) {
				if (mobileList.contains(mobile)) {
					log.info("触发用户黑名单; uid:" + uid + ", mobile:" + mobile);
					return true;
				}
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("校验用户黑名单缓存异常", "[UserBlackMobileCache.isUserBlackMobile(" + uid + "," + mobile + ") ]" + LogInfo.getTrace(e));
		}
		return false;
	}

	/***
	 * 接口修改用户黑名单入口
	 * 
	 * @param method
	 * @param array
	 */
	public void excute(Integer method, JSONArray array) {
		try {
			if (method == null || (method != METHOD.RELOAD && array.isEmpty())) {
				return;
			}
			if (method == METHOD.ADD || method == METHOD.UPDATE) {
				addUserBlackMobile(array);
			} else if (method == METHOD.DELETE) {
				deleteUserBlackMobile(array);
			} else if (method == METHOD.RELOAD) {
				loadUserBlackMobile(0);
				loadAllUserBlackMobile();
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("修改用户黑名单缓存异常", "[UserBlackMobileCache.excute(" + method + "," + array.toJSONString() + ") ]" + LogInfo.getTrace(e));
		}
	}
	
	
	/***
	 * 加载全量用户黑名单
	 * 
	 * @param uid
	 */
	public void loadAllUserBlackMobile() {
		try {
			Map<String, List<Long>> blackMobileMap=null;
			if(SmsCache.ALL_BLACK_USER_MOBILE_INIT){
				blackMobileMap = (Map<String, List<Long>>) EhcacheUtil.getInstance().get("sms", "ALL_BLACK_USER_MOBILE");
				SmsCache.ALL_BLACK_USER_MOBILE_INIT=false;
			}
			if (blackMobileMap == null || blackMobileMap.size() == 0) {
				blackMobileMap=new ConcurrentHashMap<>();
				List<Map<String, Object>> mobileList = SysCacheDao.getInstance().findAllUserBlackMobile();
				if (null != mobileList) {
					List<Long> array = new ArrayList<Long>();
					for (Map<String, Object> map : mobileList) {
						String md = map.get("md").toString().trim();
						Long mobile = Long.parseLong(md);
						String tempKey = md.substring(0, ConstantSys.MOBILE_POSITION);
						array = blackMobileMap.get(tempKey);
						if (array == null) {
							array = new ArrayList<Long>();
						}
						if (!array.contains(mobile)) {
							array.add(mobile);
							blackMobileMap.put(tempKey, array);
						}
					}
					SmsCache.ALL_BLACK_USER_MOBILE = blackMobileMap;
					log.info("全量用户黑名单加载【" + mobileList.size() + "】个");
				}
			}else {
				SmsCache.ALL_BLACK_USER_MOBILE = blackMobileMap;
				log.info("从缓存加载全量用户黑名单加载【" + blackMobileMap.size() + "】个");
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("加载全量用户黑名单缓存异常", "[UserBlackMobileCache.loadALLUserBlackMobile() ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 增加全量黑名单
	 * @param mobile
	 */
	public void addAllUserBlackMobile(Long mobile){
		String tempKey = mobile.toString().substring(0, ConstantSys.MOBILE_POSITION);
		List<Long> list = SmsCache.ALL_BLACK_USER_MOBILE.get(tempKey);
		if(list == null || list.size()==0){
			list = new ArrayList<Long>();
			list.add(mobile);
			SmsCache.ALL_BLACK_USER_MOBILE.put(tempKey,list);
		}else if(!list.contains(mobile)){
			list.add(mobile); 
			SmsCache.ALL_BLACK_USER_MOBILE.put(tempKey,list);
		}
	}
	
	/***
	 * 删除全量黑名单
	 * @param mobile
	 */
	public void deleteAllUserBlackMobile(Long mobile){
		String tempKey = mobile.toString().substring(0, ConstantSys.MOBILE_POSITION);
		List<Long> list = SmsCache.ALL_BLACK_USER_MOBILE.get(tempKey);
		if(list!=null && list.size()>0 && list.contains(mobile)){
			list.remove(mobile);
			SmsCache.ALL_BLACK_USER_MOBILE.put(tempKey,list);
		}
	}
	
	/****
	 * 是否为全量黑名单
	 * @param mobile
	 * @return
	 */
	public boolean isAllUserBlackMobile(Long mobile) {
		try {
			List<Long> mobileList = SmsCache.ALL_BLACK_USER_MOBILE.get(mobile.toString().substring(0, ConstantSys.MOBILE_POSITION));
			if (null != mobileList && mobileList.size() > 0) {
				if (mobileList.contains(mobile)) {
					log.info("触发全量用户黑名单; mobile:" + mobile);
					return true;
				}
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("校验用户黑名单缓存异常", "[UserBlackMobileCache.isAllUserBlackMobile(" + mobile + ") ]" + LogInfo.getTrace(e));
		}
		return false;
	}
}
