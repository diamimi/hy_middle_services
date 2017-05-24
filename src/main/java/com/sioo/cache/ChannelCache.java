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

/***
 * 通道缓存操作类
 * 
 * @author OYJM
 * @date 2016年9月27日
 *
 */
public class ChannelCache {
	private static Logger log = Logger.getLogger(ChannelCache.class);
	private static ChannelCache channelCache = null;

	public static ChannelCache getInstance() {
		if (channelCache != null) {
			return channelCache;
		}
		synchronized (ChannelCache.class) {
			if (channelCache == null) {
				channelCache = new ChannelCache();
			}
		}
		return channelCache;
	}

	/***
	 * 加载通道
	 * 
	 */
	public void loadChannel() {
		try {
			List<Map<String, Object>> channelList = SysCacheDao.getInstance().findSmsChannel();
			if (null != channelList) {
				for (Map<String, Object> map : channelList) {
					SmsCache.CHANNEL.put((Integer) map.get("id"), map);
				}
				log.info("通道加载【" + channelList.size() + "】个");
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("加载通道信息异常", "[ChannelCache.loadChannel() ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 重新加载通道
	 * 
	 */
	public void reloadChannel() {
		try {
			List<Map<String, Object>> channelList = SysCacheDao.getInstance().findSmsChannel();
			if (null != channelList) {
				Map<Integer, Map<String, Object>> currentMap = new ConcurrentHashMap<Integer, Map<String, Object>>();
				for (Map<String, Object> map : channelList) {
					currentMap.put((Integer) map.get("id"), map);
				}
				SmsCache.CHANNEL = currentMap;
				log.info("通道重新加载【" + channelList.size() + "】个");
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("重新加载通道信息异常", "[ChannelCache.reloadChannel() ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 根据ID获取通道信息
	 * 
	 * @param channelId
	 * @return
	 */
	public Map<String, Object> getChannel(Integer channelId) {
		try {
			Map<String, Object> channelMap = SmsCache.CHANNEL.get(channelId);
			if (channelMap != null && !channelMap.isEmpty()) {
				return channelMap;
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("获取通道信息异常", "[ChannelCache.getChannel(" + channelId + ") ]" + LogInfo.getTrace(e));
		}
		log.info("获取通道信息为空; channelId:" + channelId);
		return null;
	}

	/***
	 * 删除通道信息
	 * 
	 * @param channelId
	 */
	public void deleteChannel(Integer channelId) {
		try {
			SmsCache.CHANNEL.remove(channelId);
			log.info("删除通道信息; channelId:" + channelId);
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("删除通道信息异常", "[ChannelCache.deleteChannel(" + channelId + ") ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 添加或修改通道信息
	 * 
	 * @param channelId
	 */
	public void addOrUpdateChannel(Integer channelId) {
		try {
			Map<String, Object> map = SysCacheDao.getInstance().findSmsChannelById(channelId);
			SmsCache.CHANNEL.put((Integer) map.get("id"), map);
			log.info("重新加载通道信息; channelId:" + channelId);
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("更新通道信息异常", "[ChannelCache.addOrUpdateChannel(" + channelId + ") ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 修改通道状态
	 * 
	 * @param channelId
	 * @param stat
	 */
	public void updateChannelStat(Integer channelId, Integer stat) {
		try {
			// 先从缓存中获取通道信息
			Map<String, Object> map = SmsCache.CHANNEL.get(channelId);
			// 缓存中没有，从数据库找
			if (map == null || map.isEmpty()) {
				map = SysCacheDao.getInstance().findSmsChannelById(channelId);
			}
			if (map != null && !map.isEmpty()) {
				map.put("status", stat);
				SmsCache.CHANNEL.put(channelId, map);
				log.info("修改通道状态; channelId:" + channelId + ", stat:" + stat);
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("修改通道状态异常", "[ChannelCache.updateChannelStat(" + channelId + "," + stat + ") ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 接口修改通道信息入口
	 * 
	 * @param method
	 * @param uid
	 * @param mobile
	 */
	public void excuteChannel(Integer method, Integer channelId, Integer stat) {
		try {
			if (method == null || channelId == null) {
				return;
			}
			if (method == METHOD.ADD || method == METHOD.UPDATE) {
				addOrUpdateChannel(channelId);
			} else if (method == METHOD.DELETE) {
				deleteChannel(channelId);
			} else if (method == METHOD.RELOAD) {
				loadChannel();
			} else if (method == METHOD.STAT) {
				log.info("update channel[" + channelId + "] status");
				addOrUpdateChannel(channelId);
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("修改通道缓存异常", "[ChannelCache.excuteChannel(" + method + "," + channelId + "," + stat + ") ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 根据通道组ID获取该组子通道列表
	 * 
	 * @param channelId
	 * @return
	 */
	public List<Map<String, Object>> getGroupChannel(Integer channelId) {
		try {
			if (channelId == null || channelId.intValue() == 0)
				return null;
			return SmsCache.CHANNEL_GROUP.get(channelId);
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("获取通道组信息异常", "[ChannelCache.getGroupChannel(" + channelId + ") ]" + LogInfo.getTrace(e));
		}
		return null;
	}

	/***
	 * 加载通道组ID
	 * 
	 * @param channelId
	 */
	public void loadGroupChannel(Integer channelId) {
		try {
			List<Map<String, Object>> groupList = SysCacheDao.getInstance().findChannelGourp(channelId);
			if (groupList != null) {
				List<Map<String, Object>> subList = null;
				Map<String, Object> group = null;
				int groupId = 0;
				for (int i = 0; i < groupList.size(); i++) {
					group = groupList.get(i);
					// 如果为第一个集合，获取通道组ID不和前一个通道组ID相同，则初始化通道组信息
					if (i == 0 && groupId != Integer.parseInt(group.get("group_channel_id").toString())) {
						// 如果通道组集合不为空，先保存已获取的通道组
						if (subList != null && !subList.isEmpty()) {
							SmsCache.CHANNEL_GROUP.put((Integer) group.get("group_channel_id"), subList);
						}
						// 初始化通道组集合
						subList = new ArrayList<Map<String, Object>>();
					}
					// 将通道放入通道组集合中
					subList.add(group);
					// 记录当前通道组ID
					groupId = Integer.parseInt(group.get("group_channel_id").toString());

					if (i == groupList.size() - 1) {
						SmsCache.CHANNEL_GROUP.put((Integer) group.get("group_channel_id"), subList);
					}
				}
				log.info("通道组加载【" + groupList.size() + "】个");
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("加载通道组信息异常", "[ChannelCache.loadGroupChannel(" + channelId + ") ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 删除通道组
	 * 
	 * @param channelId
	 */
	public void deleteGroupChannel(Integer channelId) {
		try {
			if (SmsCache.CHANNEL_GROUP.containsKey(channelId)) {
				SmsCache.CHANNEL_GROUP.remove(channelId);
				log.info("删除通道组; channelId:" + channelId);
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("删除通道组信息异常", "[ChannelCache.deleteGroupChannel(" + channelId + ") ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 接口修改通道组入口
	 * 
	 * @param method
	 * @param channelId
	 */
	public void excuteChannelGroup(Integer method, Integer channelId) {
		try {
			switch (method) {
			case METHOD.ADD:
				loadGroupChannel(channelId);
				break;
			case METHOD.UPDATE:
				loadGroupChannel(channelId);
				break;
			case METHOD.DELETE:
				deleteGroupChannel(channelId);
				break;
			case METHOD.RELOAD:
				loadGroupChannel(0);
				break;
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("修改通道组缓存异常", "[ChannelCache.excuteChannelGroup(" + method + "," + channelId + ") ]" + LogInfo.getTrace(e));
		}
	}
}
