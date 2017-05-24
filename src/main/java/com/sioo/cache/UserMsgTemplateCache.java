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
import com.sioo.util.MyUtils;
import com.sioo.util.SmsCache;
import com.sioo.util.UpdateCacheConstant.METHOD;

/***
 * 用户模板缓存操作类
 * 
 * @author OYJM
 * @date 2016年9月27日
 *
 */
public class UserMsgTemplateCache {
	private static Logger log = Logger.getLogger(UserMsgTemplateCache.class);
	private static UserMsgTemplateCache userMsgTemplateCache = null;

	public static UserMsgTemplateCache getInstance() {
		if (userMsgTemplateCache != null) {
			return userMsgTemplateCache;
		}
		synchronized (UserMsgTemplateCache.class) {
			if (userMsgTemplateCache == null) {
				userMsgTemplateCache = new UserMsgTemplateCache();
			}
		}
		return userMsgTemplateCache;
	}

	/***
	 * 加载用户模板
	 * 
	 * @param uid
	 */
	public void loadUserMsgTemplate(Integer uid) {
		try {
			List<Map<String, Object>> userTempList = SysCacheDao.getInstance().findSmsUserSendModel(uid);
			Map<Integer, List<String>> userTempMap = new HashMap<Integer, List<String>>();
			if (null != userTempList) {
				List<String> array = null;
				for (Map<String, Object> map : userTempList) {
					int tempKey = (Integer) map.get("uid");
					String temp = map.get("templet").toString();
					array = userTempMap.get(tempKey);
					if (array == null) {
						array = new ArrayList<String>();
					}
					if (!array.contains(temp)) {
						array.add(temp);
						userTempMap.put(tempKey, array);
					}
				}
				SmsCache.USER_SMS_TEMPLATE = userTempMap;
				log.info("用户短信模板加载【" + userTempList.size() + "】个");
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("加载用户短信模板缓存异常", "[UserMsgTemplateCache.loadUserMsgTemplate(" + uid + ") ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 重新加载用户模板
	 */
	public void reloadUserMsgTemplate() {
		this.loadUserMsgTemplate(0);
	}

	/***
	 * 添加用户模板
	 * 
	 * @param array
	 */
	public void addUserMsgTemplate(JSONArray array) {
		try {
			for (Object obj : array.toArray()) {
				JSONObject json = (JSONObject) obj;
				Integer uid = json.getInteger("uid");
				String content = json.getString("content");
				if (SmsCache.USER_SMS_TEMPLATE.containsKey(uid)) {
					if (!SmsCache.USER_SMS_TEMPLATE.get(uid).contains(content)) {
						List<String> list = SmsCache.USER_SMS_TEMPLATE.get(uid);
						list.add(content);
						SmsCache.USER_SMS_TEMPLATE.put(uid, list);
						log.info("添加用户：" + uid + "模板【" + content + "】");
					}
				} else {
					List<String> list = new ArrayList<String>();
					array.add(content);
					SmsCache.USER_SMS_TEMPLATE.put(uid, list);
					log.info("添加用户：" + uid + "模板【" + content + "】");
				}
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("添加用户短信模板缓存异常", "[UserMsgTemplateCache.addUserMsgTemplate(" + array.toJSONString() + ") ]" + LogInfo.getTrace(e));
		}
	}

	/****
	 * 删除用户模板
	 * 
	 * @param array
	 */
	public void deleteUserMsgTemplate(JSONArray array) {
		try {
			for (Object obj : array.toArray()) {
				JSONObject json = (JSONObject) obj;
				Integer uid = json.getInteger("uid");
				String content = json.getString("content");
				if (SmsCache.USER_SMS_TEMPLATE.containsKey(uid) && SmsCache.USER_SMS_TEMPLATE.get(uid).contains(content)) {
					List<String> list = SmsCache.USER_SMS_TEMPLATE.get(uid);
					list.remove(content);
					SmsCache.USER_SMS_TEMPLATE.put(uid, list);
					log.info("删除用户：" + uid + "模板【" + content + "】");
				}
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("删除用户短信模板缓存异常", "[UserMsgTemplateCache.deleteUserMsgTemplate(" + array.toJSONString() + ") ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 验证用户模板
	 * 
	 * @param uid
	 * @param content
	 * @return true为校验通过
	 */
	public boolean isUserMsgTemplate(Integer uid, String content) {
		try {
			List<String> temList = SmsCache.USER_SMS_TEMPLATE.get(uid);
			if (null != temList && temList.size() > 0) {
				content = content.replace("^", "").replace("(", "（").replace(")", "）");
				for (String temp : temList) {
					// 短信模板匹配
					if (MyUtils.matchTemplet(content, temp.replace("(", "（").replace(")", "）"))) {
						log.info("匹配用户模板; uid:" + uid + ", content:" + content + ", temp:" + temp);
						return true;
					}
				}
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("校验用户短信模板异常", "[UserMsgTemplateCache.isUserMsgTemplate(" + uid + "," + content + ") ]" + LogInfo.getTrace(e));
		}
		return false;
	}

	/***
	 * 接口修改用户模板入口
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
				addUserMsgTemplate(array);
			} else if (method == METHOD.DELETE) {
				deleteUserMsgTemplate(array);
			} else if (method == METHOD.RELOAD) {
				loadUserMsgTemplate(0);
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("修改用户短信模板缓存异常", "[UserMsgTemplateCache.deleteUserMsgTemplate(" + method + "," + array.toJSONString() + ") ]" + LogInfo.getTrace(e));
		}
	}
}
