package com.sioo.cache;

import com.sioo.log.LogInfo;
import com.sioo.servlet.HttpSubmitServer;
import com.sioo.util.FileUtils;
import com.sioo.util.SmsCache;
import com.sioo.util.UpdateCacheConstant.METHOD;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/***
 * 用户余额缓存
 * 
 * @author OYJM
 * @date 2016年9月27日
 *
 */
public class UserSmsCache {
	private static Logger log = Logger.getLogger(UserSmsCache.class);
	private static UserSmsCache userSmsCache = null;

	public static UserSmsCache getInstance() {
		if (userSmsCache != null) {
			return userSmsCache;
		}
		synchronized (UserSmsCache.class) {
			if (userSmsCache == null) {
				userSmsCache = new UserSmsCache();
			}
		}
		return userSmsCache;
	}

	public String excute(Integer method, Integer uid, Integer sms) {
		String result = HttpSubmitServer.FAIL;
		if(uid == null || uid == 0){
			return result;
		}
		try {
			if(method == METHOD.ADD || method == METHOD.UPDATE){
				log.info("update "+uid+" sms, sms:"+getUserSms(uid)+", yukousms:"+getUserSmsYukou(uid)+", addsms:"+sms);
				putUserSms(uid,getUserSms(uid)+sms);
				putUserSmsYukou(uid,getUserSmsYukouBase(uid)+sms);
				result = HttpSubmitServer.SUCC;
			}else if(method == METHOD.DELETE){
				if (SmsCache.USER_SMS.containsKey(uid)) {
					SmsCache.USER_SMS.remove(uid);
				}
				if (SmsCache.USER_SMS_YUKOU.containsKey(uid)) {
					SmsCache.USER_SMS_YUKOU.remove(uid);
				}
				result = HttpSubmitServer.SUCC;
				log.info("delete "+uid+" sms, sms:"+getUserSms(uid)+", yukousms:"+getUserSmsYukou(uid));
			}else if(method == METHOD.GET){
				log.info("get "+uid+" sms, sms:"+getUserSms(uid)+", yukousms:"+getUserSmsYukou(uid));
				result = getUserSmsYukou(uid).toString();
			}else if(method==METHOD.RELOAD){
				/**
				 * 切换线路时,新线路同步旧线路的余额
				 */
				SmsCache.USER_LINE.remove(uid);
				SmsCache.USER_LINE_SYNC_STATE.remove(uid);
				putUserSms(uid,sms);
				putUserSmsYukou(uid,sms);
				result = HttpSubmitServer.SUCC;

			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("修改用户信息缓存异常", "[UserCache.excute(" + method + "," + uid + ") ]" + LogInfo.getTrace(e));
			result = HttpSubmitServer.FAIL;
		}

		return result;
	}
	
	/***
	 * 获取用户短信余额
	 * 
	 * @param uid
	 * @return
	 */
	public Integer getUserSms(Integer uid) {
		try {
			if (SmsCache.USER_SMS.containsKey(uid)) {
				return SmsCache.USER_SMS.get(uid);
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("获取用户余额缓存异常", "[UserSmsCache.getUserSms(" + uid + ") ]" + LogInfo.getTrace(e));
		}
		return 0;
	}

	/***
	 * 设置用户短信余额
	 * 
	 * @param uid
	 * @param sms
	 */
	public void putUserSms(Integer uid, Integer sms) {
		try {
			SmsCache.USER_SMS.put(uid, sms);
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("设置用户余额缓存异常", "[UserSmsCache.putUserSms(" + uid + "," + sms + ") ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 获取预扣用户短信余额
	 * 
	 * @param uid
	 * @return
	 */
	public Integer getUserSmsYukou(Integer uid) {
		try {
			if (SmsCache.USER_SMS_YUKOU.containsKey(uid)) {
				int sms = SmsCache.USER_SMS.get(uid);
				int smsYukou = SmsCache.USER_SMS_YUKOU.get(uid);
				if(smsYukou>sms){
					log.error("用户"+uid+",预扣条数("+smsYukou+")大于短信条数("+sms+"),修改预扣条数为短信条数。");
					SmsCache.USER_SMS_YUKOU.put(uid, sms);
					return sms;
				}else{
					return smsYukou;
				}
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("获取用户预扣短信条数缓存异常", "[UserSmsCache.getUserSmsYukou(" + uid + ") ]" + LogInfo.getTrace(e));
		}
		return 0;
	}

	public Integer getUserSmsYukouBase(Integer uid) {
		try {
			if (SmsCache.USER_SMS_YUKOU.containsKey(uid)) {
				return SmsCache.USER_SMS_YUKOU.get(uid);
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("获取用户预扣短信条数缓存异常", "[UserSmsCache.getUserSmsYukou(" + uid + ") ]" + LogInfo.getTrace(e));
		}
		return 0;
	}
	
	/***
	 * 设置预扣短信余额
	 * 
	 * @param uid
	 * @param sms
	 */
	public void putUserSmsYukou(Integer uid, Integer sms) {
		try {
			SmsCache.USER_SMS_YUKOU.put(uid, sms);
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("设置用户预扣短信条数缓存异常", "[UserSmsCache.putUserSmsYukou(" + uid + "," + sms + ") ]" + LogInfo.getTrace(e));
		}
	}

	/****
	 * 自增长余额标记
	 * 
	 * @return
	 */
	public synchronized Integer incrementSmsFlag() {
		try {
			SmsCache.SMS_FLAG++;
			if (SmsCache.SMS_FLAG > 10000) {
				SmsCache.SMS_FLAG = 1;
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("用户余额记录标记自增长缓存异常", "[UserSmsCache.incrementSmsFlag() ]" + LogInfo.getTrace(e));
		}
		return SmsCache.SMS_FLAG;
	}

	public Integer getSmsFlag() {
		return SmsCache.SMS_FLAG;
	}

	/***
	 * 获取某一批的扣费短信
	 * 
	 * @param pid
	 * @return
	 */
	public ConcurrentHashMap<String, Integer> getSmsKouMap(Long pid) {
		try {
			return SmsCache.USER_SMS_KOU.get(pid);
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("获取用户扣费信息列表异常", "[UserSmsCache.getSmsKouMap(" + pid + ") ]" + LogInfo.getTrace(e));
		}
		return null;
	}

	/***
	 * 获取所有的key
	 * 
	 * @return
	 */
	public Set<Long> getAllKey() {
		try {
			return SmsCache.USER_SMS_KOU.keySet();
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("获取用户扣费信息所有键异常", "[UserSmsCache.getAllKey() ]" + LogInfo.getTrace(e));
		}
		return null;
	}

	/***
	 * 是否为空
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		try {
			if (SmsCache.USER_SMS_KOU == null || SmsCache.USER_SMS_KOU.isEmpty()) {
				return true;
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("判断用户扣费信息是否为空异常", "[UserSmsCache.isEmpty() ]" + LogInfo.getTrace(e));
		}
		return false;
	}

	/***
	 * 移除某一批的扣费短信
	 * 
	 * @param pid
	 * @return
	 */
	public synchronized void removeSmsKouMap(Long pid) {
		try {
			SmsCache.USER_SMS_KOU.remove(pid);
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("移除用户扣款信息异常", "[UserSmsCache.removeSmsKouMap(" + pid + ") ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 设置扣费信息
	 * 
	 * @param uid
	 * @param kouSms
	 */
	public void setSmsKou(Integer uid, Integer kouSms) {
		try {
			long increment = SmsCache.SUB_ATOMIC.incrementAndGet();

			String key = increment + "_" + uid;

			Long pid = SmsCache.ATOMIC.get();
			ConcurrentHashMap<String, Integer> map =SmsCache.USER_SMS_KOU.get(pid);
			if (map == null) {
				map = new ConcurrentHashMap<>();
			}
			map.put(key, kouSms);
			SmsCache.USER_SMS_KOU.put(pid, map);
			// 预扣款
			SmsCache.USER_SMS_YUKOU.put(uid, SmsCache.USER_SMS_YUKOU.get(uid) - kouSms);
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("设置用户扣费信息异常", "[UserSmsCache.setSmsKou(" + uid + "," + kouSms + ") ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 加载保存用户发送记录条数的txt文件
	 */
	public void loadSmsCacheByTxt() {
		try {
			Map<Integer, Integer> map = FileUtils.readSmsCancheTxtFile();
			if (map == null) {
				return;
			}
			for (Integer key : map.keySet()) {
				if (key.intValue() != 0 && map.get(key).intValue() != 0) {
					log.info("加载未计算余额; uid:" + key + ", sms" + map.get(key));
					//设置扣款
					setSmsKou(key, map.get(key));
				}
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("加载未计算扣费信息异常", "[UserSmsCache.loadSmsCacheByTxt() ]" + LogInfo.getTrace(e));
		}
	}
	
}
