package com.sioo.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.sioo.dao.SysCacheDao;
import com.sioo.log.LogInfo;
import com.sioo.util.MyUtils;
import com.sioo.util.SmsCache;
import com.sioo.util.UpdateCacheConstant.METHOD;

/***
 * 通道黑签名缓存操作类
 * 
 * @author OYJM
 * @date 2016年9月27日
 *
 */
public class ChannelBlackSignCache {
	private static Logger log = Logger.getLogger(ChannelBlackSignCache.class);
	private static ChannelBlackSignCache channelBlackSignCache = null;

	public static ChannelBlackSignCache getInstance() {
		if (channelBlackSignCache != null) {
			return channelBlackSignCache;
		}
		synchronized (ChannelBlackSignCache.class) {
			if (channelBlackSignCache == null) {
				channelBlackSignCache = new ChannelBlackSignCache();
			}
		}
		return channelBlackSignCache;
	}

	/***
	 * 加载通道黑签名
	 * 
	 * @param uid
	 * @param store
	 */
	public void loadChannelBlackSign(Integer uid, String store) {
		try {
			List<Map<String, Object>> userSignList = SysCacheDao.getInstance().findSmsSignChannelBlack(uid, store);
			if (userSignList != null) {
				List<Map<String, Object>> array = null;
				String key = null;
				for (Map<String, Object> map : userSignList) {
					// key是uid+签名的第一个字utf-8编码后，在16位md5,例如签名【希奥股份】 计算前2位
					key = MyUtils.getHashKey((Integer) map.get("uid"), String.valueOf(map.get("store")));
					array = SmsCache.CHANNEL_BLACK_SIGN.get(key);
					if (array == null) {
						array = new ArrayList<Map<String, Object>>();
					}
					array.add(map);
					SmsCache.CHANNEL_BLACK_SIGN.put(key, array);
				}
				log.info("通道黑签名加载【" + userSignList.size() + "】个");
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("加载通道黑签名异常", "[ChannelBlackSignCache.loadChannelBlackSign(" + uid + "," + store + ") ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 重新加载通道黑签名
	 * 
	 */
	public void reloadChannelBlackSign() {
		try {
			List<Map<String, Object>> userSignList = SysCacheDao.getInstance().findSmsSignChannelBlack(0, null);
			if (userSignList != null) {
				Map<String, List<Map<String, Object>>> currentMap = new ConcurrentHashMap<String, List<Map<String, Object>>>();
				List<Map<String, Object>> array = null;
				String key = null;
				for (Map<String, Object> map : userSignList) {
					// key是uid+签名的第一个字utf-8编码后，在16位md5,例如签名【希奥股份】 计算前2位
					key = MyUtils.getHashKey((Integer) map.get("uid"), String.valueOf(map.get("store")));
					array = currentMap.get(key);
					if (array == null) {
						array = new ArrayList<Map<String, Object>>();
					}
					array.add(map);
					currentMap.put(key, array);
				}
				SmsCache.CHANNEL_BLACK_SIGN = currentMap;
				log.info("通道黑签名重新加载【" + userSignList.size() + "】个");
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("重新加载通道黑签名异常", "[ChannelBlackSignCache.loadChannelBlackSign() ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 校验通道黑签名
	 * 
	 * @param uid
	 * @param store
	 * @return
	 */
	public boolean isChannelBlackSign(Integer uid, String store) {
		try {
			String key = MyUtils.getHashKey(uid, store);
			List<Map<String, Object>> mapList = SmsCache.CHANNEL_BLACK_SIGN.get(key);
			if (null != mapList) {
				for (Map<String, Object> map : mapList) {
					if (map.get("store").equals(store) && Integer.parseInt(String.valueOf(map.get("uid"))) == uid) {
						log.info("触发通道黑签名; uid:" + uid + ",store:" + store);
						return true;
					}
				}
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("校验通道黑签名异常", "[ChannelBlackSignCache.isChannelBlackSign(" + uid + "," + store + ") ]" + LogInfo.getTrace(e));
		}
		return false;
	}

	/***
	 * 删除通道黑签名
	 * 
	 * @param uid
	 * @param store
	 */
	public void deleteChannelBlackSign(Integer uid, String store) {
		try {
			String key = MyUtils.getHashKey(uid, store);
			if (SmsCache.CHANNEL_BLACK_SIGN.containsKey(key)) {
				SmsCache.CHANNEL_BLACK_SIGN.remove(key);
				log.info("删除通道黑签名; uid:" + uid + ",store:" + store);
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("删除通道黑签名异常", "[ChannelBlackSignCache.deleteChannelBlackSign(" + uid + "," + store + ") ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 接口修改通道黑签名入口
	 * 
	 * @param method
	 * @param uid
	 * @param mobile
	 */
	public void excute(Integer method, Integer uid, String store) {
		try {
			switch (method) {
			case METHOD.ADD:
				loadChannelBlackSign(uid, store);
				break;
			case METHOD.UPDATE:
				loadChannelBlackSign(uid, store);
				break;
			case METHOD.DELETE:
				deleteChannelBlackSign(uid, store);
				break;
			case METHOD.RELOAD:
				loadChannelBlackSign(0, null);
				break;
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("修改通道黑签名缓存异常", "[ChannelBlackSignCache.excute(" + method + "," + uid + "," + store + ") ]" + LogInfo.getTrace(e));
		}
	}
}
