package com.sioo.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.sioo.dao.SysCacheDao;
import com.sioo.log.LogInfo;
import com.sioo.util.SmsCache;
import com.sioo.util.UpdateCacheConstant.METHOD;
import org.junit.Test;

/***
 * 屏蔽地区缓存操作类
 * 
 * @author OYJM
 * @date 2016年10月9日
 *
 */
public class UserBlackLocationCache {
	private static Logger log = Logger.getLogger(UserBlackLocationCache.class);
	private static UserBlackLocationCache userBlackLocationCache = null;

	public static UserBlackLocationCache getInstance() {
		if (userBlackLocationCache != null) {
			return userBlackLocationCache;
		}
		synchronized (UserBlackLocationCache.class) {
			if (userBlackLocationCache == null) {
				userBlackLocationCache = new UserBlackLocationCache();
			}
		}
		return userBlackLocationCache;
	}

	/***
	 * 加载用户屏蔽地区
	 * 
	 * @param uid
	 */
	public void loadUserBlackLocation(Integer uid) {
		try {
			List<Map<String, Object>> blackLocationList = SysCacheDao.getInstance().findSmsBlackArea(uid);
			Map<Integer, List<String>> currentMap = new ConcurrentHashMap<Integer, List<String>>();
			if (null != blackLocationList&&blackLocationList.size()>0) {
				List<String> list = null;
				for (Map<String, Object> map : blackLocationList) {
					String provinceAndCity = map.get("provincecode").toString()+"&"+map.get("citycode").toString();
					Integer key = (Integer) map.get("uid");
					list = currentMap.get(key);
					if (list == null) {
						list = new ArrayList<String>();
					}

					if (!list.contains(provinceAndCity)) {
						list.add(provinceAndCity);
					}
					currentMap.put(key, list);
				}
				if(uid!=null && uid.intValue()!=0){
					SmsCache.USER_BLACK_LOCATION.put(uid, currentMap.get(uid));
				}else{
					SmsCache.USER_BLACK_LOCATION = currentMap;
				}
				
				log.info("用户"+uid+"屏蔽地区加载【" + blackLocationList.size() + "】个");
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("加载用户屏蔽地区缓存异常", "[UserBlackLocationCache.loadUserBlackLocation(" + uid + ") ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 加载通道屏蔽地区
	 * 
	 * @param
	 */
	public void loadChannelBlackLocation(Integer channelId) {
		try {
			List<Map<String, Object>> channelBlackLocationList = SysCacheDao.getInstance().findSmsBlackAreaByChannel(channelId);
			Map<Integer, List<String>> channelCurrentMap = new ConcurrentHashMap<Integer, List<String>>();
			Map<String, Integer> routeMap = new ConcurrentHashMap<String, Integer>();
			if (null != channelBlackLocationList&&channelBlackLocationList.size()>0) {
				List<String> list = null;
				for (Map<String, Object> map : channelBlackLocationList) {
					String provinceAndCity = map.get("provincecode").toString()+"&"+map.get("citycode").toString();
					Integer key = (Integer) map.get("channel_id");
					list = channelCurrentMap.get(key);
					if (list == null) {
						list = new ArrayList<String>();
					}

					if (!list.contains(provinceAndCity)) {
						list.add(provinceAndCity);
					}
					channelCurrentMap.put(key, list);
					if(map.get("route_channel")!=null && (Integer) map.get("route_channel")!=0){
						routeMap.put(map.get("channel_id")+"&"+map.get("provincecode").toString()+"&"+map.get("citycode").toString(), (Integer) map.get("route_channel"));
					}
				}
				
				if(channelId!=null && channelId.intValue()!=0){
					SmsCache.CHANNEL_BLACK_LOCATION.put(channelId, channelCurrentMap.get(channelId));
					for(String key:SmsCache.BLACK_LOCATION_ROUTE.keySet()){
						if(key.startsWith(channelId+"&")){
							SmsCache.BLACK_LOCATION_ROUTE.remove(key);
						}
					}
					SmsCache.BLACK_LOCATION_ROUTE.putAll(routeMap);
				}else{
					SmsCache.CHANNEL_BLACK_LOCATION = channelCurrentMap;
					SmsCache.BLACK_LOCATION_ROUTE = routeMap;
				}
				log.info("通道"+channelId+"屏蔽地区加载【" + channelBlackLocationList.size() + "】个");
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("加载通道屏蔽地区缓存异常", "[UserBlackLocationCache.loadChannelBlackLocation(" + channelId + ") ]" + LogInfo.getTrace(e));
		}
	}

	public void reloadBlackLocation() {
		try {
			this.loadUserBlackLocation(0);
			this.loadChannelBlackLocation(0);
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("重新加载屏蔽地区缓存异常", "[UserBlackLocationCache.reloadBlackLocation() ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 添加屏蔽地区
	 * 
	 * @param
	 */
	public void addUserBlackLocation(Integer uid) {
		try {
			deleteUserBlackLocation(uid);
			loadUserBlackLocation(uid);
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("添加用户屏蔽地区缓存异常", "[UserBlackLocationCache.addUserBlackLocation(" + uid + ") ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 添加屏蔽地区
	 * 
	 * @param
	 */
	public void addChannelBlackLocation(Integer channelId) {
		try {
			deleteChannelBlackLocation(channelId);
			loadChannelBlackLocation(channelId);
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("添加通道屏蔽地区缓存异常", "[UserBlackLocationCache.addChannelBlackLocation(" + channelId + ") ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 删除用户屏蔽地区
	 * 
	 * @param uid
	 */
	public void deleteUserBlackLocation(Integer uid) {
		try {
			SmsCache.USER_BLACK_LOCATION.remove(uid);
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("删除用户屏蔽地区缓存异常", "[UserBlackLocationCache.deleteUserBlackLocation(" + uid + ") ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 删除通道屏蔽地区
	 * 
	 * @param channelId
	 */
	public void deleteChannelBlackLocation(Integer channelId) {
		try {
			SmsCache.CHANNEL_BLACK_LOCATION.remove(channelId);
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("删除通道屏蔽地区缓存异常", "[UserBlackLocationCache.deleteChannelBlackLocation(" + channelId + ") ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 是否为通道屏蔽地区
	 * 
	 * @param channelId
	 * @param
	 * @return
	 */
	public boolean isChannelBlackLocation(Integer channelId, String provinceAndCity) {
		try {
			List<String> provincecodeList = SmsCache.CHANNEL_BLACK_LOCATION.get(channelId);
			String all = provinceAndCity.split("&")[0]+"&"+0;
			if (null != provincecodeList && provincecodeList.size() > 0 && (provincecodeList.contains(provinceAndCity) || provincecodeList.contains(all))) {
				log.info("触发通道屏蔽地区; channelId:" + channelId + ", province and city code:" + provinceAndCity);
				return true;
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("修改通道屏蔽地区缓存异常", "[UserBlackLocationCache.isChannelBlackLocation(" + channelId + "," + provinceAndCity + ") ]" + LogInfo.getTrace(e));
		}
		return false;
	}

	public Integer getChannelBlackLocationRoute(Integer channelId, String provinceAndCity) {
		try {
			//如果精确到市
			String key1 = channelId+"&"+provinceAndCity;
			if(SmsCache.BLACK_LOCATION_ROUTE.containsKey(key1)){
				return SmsCache.BLACK_LOCATION_ROUTE.get(key1);
			}
			//如果只精确到省
			String key2 = channelId+"&"+provinceAndCity.split("&")[0]+"&"+0;
			if(SmsCache.BLACK_LOCATION_ROUTE.containsKey(key2)){
				return SmsCache.BLACK_LOCATION_ROUTE.get(key2);
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("修改通道屏蔽地区缓存异常", "[UserBlackLocationCache.isChannelBlackLocation(" + channelId + "," + provinceAndCity + ") ]" + LogInfo.getTrace(e));
		}
		return null;
	}
	
	/***
	 * 是否为屏蔽地区
	 * 
	 * @param
	 */
	public boolean isBlackLocation(Integer uid, String provinceAndCity) {
		try {
			List<String> provincecodeList = SmsCache.USER_BLACK_LOCATION.get(uid);
			String all = provinceAndCity.split("&")[0]+"&"+0;
			if (null != provincecodeList && provincecodeList.size() > 0 && (provincecodeList.contains(provinceAndCity) || provincecodeList.contains(all))) {
				log.info("触发用户屏蔽地区; uid:" + uid + ", province and city code:" + provinceAndCity);
				return true;
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("校验用户屏蔽地区缓存异常", "[UserBlackLocationCache.isBlackLocation(" + uid + "," + provinceAndCity + ") ]" + LogInfo.getTrace(e));
		}
		return false;
	}

	public void excute(Integer method, Integer uid, Integer channelId) {
		try {
			if (method == null || uid == null) {
				return;
			}
			if (uid != null && uid != 0) {
				if (method == METHOD.ADD || method == METHOD.UPDATE) {
					addUserBlackLocation(uid);
				} else if (method == METHOD.DELETE) {
					deleteUserBlackLocation(uid);
				} 
			} else if (channelId != null && channelId != 0) {
				if (method == METHOD.ADD || method == METHOD.UPDATE) {
					addChannelBlackLocation(channelId);
				} else if (method == METHOD.DELETE) {
					deleteChannelBlackLocation(channelId);
				}
			}else{
				if (method == METHOD.RELOAD) {
					loadUserBlackLocation(0);
					loadChannelBlackLocation(0);
				}
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("修改屏蔽地区缓存异常", "[UserBlackLocationCache.excute(" + method + "," + uid + "," + channelId + ") ]" + LogInfo.getTrace(e));
		}
	}
}
