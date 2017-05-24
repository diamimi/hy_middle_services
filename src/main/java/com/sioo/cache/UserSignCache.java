package com.sioo.cache;

import com.alibaba.fastjson.JSON;
import com.sioo.dao.SmsUserSignDao;
import com.sioo.log.LogInfo;
import com.sioo.service.model.UserSign;
import com.sioo.util.EhcacheUtil;
import com.sioo.util.SmsCache;
import com.sioo.util.UpdateCacheConstant.METHOD;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/***
 * 用户签名缓存操作类
 * 
 * @author OYJM
 * @date 2016年12月3日
 *
 */
public class UserSignCache {
	private static Logger log = Logger.getLogger(UserSignCache.class);
	private static UserSignCache userSignCache = null;

	public static UserSignCache getInstance() {
		if (userSignCache != null) {
			return userSignCache;
		}
		synchronized (UserSignCache.class) {
			if (userSignCache == null) {
				userSignCache = new UserSignCache();
			}
		}
		return userSignCache;
	}

	/***
	 * 加载签名
	 * 
	 * @param uid
	 * @param store
	 */
	public void loadUserSign(Integer uid, String store) {
		try {
			Map<Integer, List<UserSign>> CHANNEL_SIGN=null;
			if(SmsCache.CHANNEL_SIGN_INIT){
				CHANNEL_SIGN=(Map<Integer, List<UserSign>>) EhcacheUtil.getInstance().get("sms","CHANNEL_SIGN");
				SmsCache.CHANNEL_SIGN_INIT=false;
			}
			if(CHANNEL_SIGN==CHANNEL_SIGN||CHANNEL_SIGN.size()==0){
				List<UserSign> channelList = SmsUserSignDao.getInstance().findUserSignByUidAndStore(uid, store, 1);
				if (channelList != null) {
					for (UserSign channelSign : channelList) {
						saveChannelCache(channelSign);
					}
				}
			}else{
				SmsCache.CHANNEL_SIGN=CHANNEL_SIGN;
				SmsCache.CHANNEL_SIGN_INIT=false;
				log.info("从缓存加载通道签名");
			}
			Map<Integer, CopyOnWriteArrayList<UserSign>> USER_SIGN=null;
			Map<String, UserSign> EXPEND_SIGN=null;
			if(SmsCache.USER_SIGN_INIT&&SmsCache.EXPEND_SIGN_INIT){
				USER_SIGN=(Map<Integer, CopyOnWriteArrayList<UserSign>>)EhcacheUtil.getInstance().get("sms","USER_SIGN");
				EXPEND_SIGN=(Map<String, UserSign>)EhcacheUtil.getInstance().get("sms","EXPEND_SIGN");
				SmsCache.USER_SIGN_INIT=false;
				SmsCache.EXPEND_SIGN_INIT=false;
			}
			if((USER_SIGN==null||USER_SIGN.size()==0)&&(EXPEND_SIGN==null||EXPEND_SIGN.size()==0)){
				List<UserSign> userSignList = SmsUserSignDao.getInstance().findUserSignByUidAndStore(uid, store, null);
				if (userSignList != null) {
					for (UserSign userSign : userSignList) {
						saveCache(userSign);
					}
					this.loadMaxExpend2();
					log.info("用户"+uid+"，签名加载【" + userSignList.size() + "】个");
				}
			}else {
				SmsCache.USER_SIGN=USER_SIGN;
				SmsCache.EXPEND_SIGN=EXPEND_SIGN;
				log.info("从缓存加载用户签名,拓展签名");
			}

		} catch (Exception e) {
			LogInfo.getLog().errorAlert("加载签名库异常", "[ChannelSignCache.loadChannelSign(" + uid + "," + store + ") Exception]" + LogInfo.getTrace(e));
		}
	}

	/****
	 * 保存签名缓存
	 * 
	 * @param userSign
	 */
	private void saveCache(UserSign userSign) {
		if (SmsCache.USER_SIGN.containsKey(userSign.getUid())) {
			SmsCache.USER_SIGN.get(userSign.getUid()).add(userSign);
		} else {
			CopyOnWriteArrayList<UserSign> newUserSignList = new CopyOnWriteArrayList<UserSign>();
			newUserSignList.add(userSign);
			SmsCache.USER_SIGN.put(userSign.getUid(), newUserSignList);
		}
		
		if(userSign.getType() ==2){
			SmsCache.EXPEND_SIGN.put(userSign.getExpend(), userSign);
		}
	}

	private void saveChannelCache(UserSign userSign) {
		if (SmsCache.CHANNEL_SIGN.containsKey(userSign.getUid())) {
			SmsCache.CHANNEL_SIGN.get(userSign.getUid()).add(userSign);
		} else {
			List<UserSign> newUserSignList = new ArrayList<UserSign>();
			newUserSignList.add(userSign);
			SmsCache.CHANNEL_SIGN.put(userSign.getUid(), newUserSignList);
		}
	}

	/***
	 * 重新加载签名
	 * 
	 */
	public void reloadUserSign() {
		this.loadUserSign(0, null);
	}

	/***
	 * 批量加载签名
	 * 
	 */
	public void loadUserSignByExpends(String expends) {
		try {
			if (expends == null || expends.isEmpty() || expends.equals(",")) {
				return;
			}

			if (expends.endsWith(",")) {
				expends = expends.substring(0, expends.length() - 1);
			}

			List<UserSign> userSignList = SmsUserSignDao.getInstance().findUserSignByExpends(expends);
			if (userSignList != null) {
				for (UserSign userSign : userSignList) {
					if (userSign.getType().equals("2")) {
						saveCache(userSign);
					} else if (userSign.getType().equals("1")) {
						saveChannelCache(userSign);
					}
				}
				this.loadMaxExpend2();
				log.info("签名加载【" + userSignList.size() + "】个");
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("加载签名库异常", "[ChannelSignCache.loadUserSignByExpends(" + expends + ") Exception]" + LogInfo.getTrace(e));
		}
	}

	public void reloadNewSign() {
		List<UserSign> channelList = SmsUserSignDao.getInstance().findUserSignByUidAndStore(0, null, 1);
		if (channelList != null) {
			Map<Integer, List<UserSign>> channelSignMap = new ConcurrentHashMap<Integer, List<UserSign>>();
			for (UserSign channelSign : channelList) {
				if (channelSignMap.containsKey(channelSign.getUid())) {
					channelSignMap.get(channelSign.getUid()).add(channelSign);
				} else {
					List<UserSign> newChannelSignList = new ArrayList<UserSign>();
					newChannelSignList.add(channelSign);
					channelSignMap.put(channelSign.getUid(), newChannelSignList);
				}
			}
			SmsCache.CHANNEL_SIGN = channelSignMap;
			log.info("重新加载通道签名【" + channelList.size() + "】个");
		}

		List<UserSign> userSignList = SmsUserSignDao.getInstance().findUserSignByUidAndStore(0, null, null);
		if (userSignList != null) {
			if (SmsCache.EXPEND_SIGN != null && SmsCache.EXPEND_SIGN.size() > 0) {
				// 如果新增签名超过1W,不往缓存里面加
				if ((userSignList.size() - SmsCache.EXPEND_SIGN.size()) > 10000) {
					LogInfo.getLog().errorAlert("加载新签名异常，新增签名10000条记录。", "[ChannelSignCache.loadNewSign() Exception]");
					return;
				}
				// 如果签名条数一样，不需要重新加载
				// if (userSignList.size() == SmsCache.EXPEND_SIGN.size()) {
				// return;
				// }
			}

			Map<Integer, CopyOnWriteArrayList<UserSign>> userSignMap = new ConcurrentHashMap<Integer, CopyOnWriteArrayList<UserSign>>();
			for (UserSign userSign : userSignList) {
				if (userSignMap.containsKey(userSign.getUid())) {
					userSignMap.get(userSign.getUid()).add(userSign);
				} else {
					CopyOnWriteArrayList<UserSign> newUserSignList = new CopyOnWriteArrayList<UserSign>();
					newUserSignList.add(userSign);
					userSignMap.put(userSign.getUid(), newUserSignList);
				}
				
				if(userSign.getType()!=null && userSign.getType() ==2){
					SmsCache.EXPEND_SIGN.put(userSign.getExpend(), userSign);
				}
			}
			SmsCache.USER_SIGN = userSignMap;

			log.info("重新加载签名【" + userSignList.size() + "】个");
		}
		this.loadMaxExpend2();
	}

	/***
	 * 删除签名
	 * 
	 * @param uid
	 * @param store
	 */
	public void deleteUserSign(Integer uid, String store, String expend, Integer type) {
		try {
			if (type == 2) {
				if (SmsCache.EXPEND_SIGN.containsKey(expend)) {
					SmsCache.EXPEND_SIGN.remove(expend);
				}
			} else if (type == 1) {
				if (SmsCache.CHANNEL_SIGN.containsKey(uid)) {
					List<UserSign> signList = SmsUserSignDao.getInstance().findUserSignByUidAndStore(uid, null, 1);
					if (signList != null && signList.size() > 0) {
						SmsCache.CHANNEL_SIGN.put(uid, signList);
					} else {
						SmsCache.CHANNEL_SIGN.remove(uid);
					}
				}
			}
			
			if (SmsCache.USER_SIGN.containsKey(uid)) {
				CopyOnWriteArrayList<UserSign> signList = SmsUserSignDao.getInstance().findUserSignByUidAndStore(uid, null, null);
				if (signList != null && signList.size() > 0) {
					SmsCache.USER_SIGN.put(uid, signList);
				} else {
					SmsCache.USER_SIGN.remove(uid);
				}
			}
			log.info("删除签名; uid:" + uid + ",expend:" + expend);
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("删除签名异常", "[ChannelSignCache.deleteChannelSign(" + uid + "," + expend + ") Exception]" + LogInfo.getTrace(e));
		}
	}

	/****
	 * 添加签名
	 * 
	 * @param userSign
	 */
	public void addUserSign(UserSign userSign) {
		try {
			if (userSign == null) {
				return;
			}
			saveCache(userSign);
			log.info("添加签名; data:" + JSON.toJSONString(userSign));
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("添加签名异常", "[ChannelSignCache.addChannelSign(" + JSON.toJSONString(userSign) + ") Exception]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 重新加载用户签名
	 */
	public void reloadUserSign(Integer uid){
		List<UserSign> userSignList = SmsUserSignDao.getInstance().findUserSignByUidAndStore(uid, null, null);
		Map<Integer, List<UserSign>> channelSign = new ConcurrentHashMap<Integer, List<UserSign>>();
		Map<Integer, CopyOnWriteArrayList<UserSign>> sysSign = new ConcurrentHashMap<Integer, CopyOnWriteArrayList<UserSign>>();
		if(uid == null || uid ==0){
			SmsCache.EXPEND_SIGN.clear();
		}else{
			for(String key:SmsCache.EXPEND_SIGN.keySet()){
				UserSign val = SmsCache.EXPEND_SIGN.get(key);
				if(val.getUid().equals(uid)){
					SmsCache.EXPEND_SIGN.remove(key);
				}
			}
		}
		
		for(UserSign userSign : userSignList){
			if(sysSign.containsKey(userSign.getUid())){
				sysSign.get(userSign.getUid()).add(userSign);
			}else{
				CopyOnWriteArrayList<UserSign> list = new CopyOnWriteArrayList<UserSign>();
				list.add(userSign);
				sysSign.put(userSign.getUid(), list);
			}
			
			if(userSign.getType() == 1){
				if(channelSign.containsKey(userSign.getUid())){
					channelSign.get(userSign.getUid()).add(userSign);
				}else{
					List<UserSign> list = new ArrayList<UserSign>();
					list.add(userSign);
					channelSign.put(userSign.getUid(), list);
				}
			}else if(userSign.getType() == 2){
				SmsCache.EXPEND_SIGN.put(userSign.getExpend(), userSign);
			}
		}
		
		if(uid == null || uid ==0){
			SmsCache.CHANNEL_SIGN = channelSign;
			SmsCache.USER_SIGN =  sysSign;
		}else{
			if(channelSign.containsKey(uid)){
				SmsCache.CHANNEL_SIGN.put(uid, channelSign.get(uid));
			}else{
				SmsCache.CHANNEL_SIGN.remove(uid);
			}
			if(sysSign.containsKey(uid)){
				SmsCache.USER_SIGN.put(uid, sysSign.get(uid));
			}else{
				SmsCache.USER_SIGN.remove(uid);
			}
		}
		
		log.info("重新加载签名，uid:"+uid);
	}
	
	/***
	 * 接口修改通道签名组入口
	 * 
	 * @param method
	 * @param uid
	 * @param store
	 */
	public void excute(Integer method, Integer uid, String store, String expend, String expends, Integer type) {
		try {
			if (method.equals(METHOD.ADD) || method.equals(METHOD.UPDATE) || method.equals(METHOD.RELOAD)) {
				if(uid!=null){
					reloadUserSign(uid);
				}
			} else if (method.equals(METHOD.DELETE)) {
				deleteUserSign(uid, store, expend, type);
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("修改签名库缓存异常", "[ChannelCache.excuteChannelGroup(" + method + "," + uid + "," + store + ") Exception]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 加载普通用户最多自增长拓展
	 */
	public void loadMaxExpend2() {
		String max = SmsUserSignDao.getInstance().findMaxExpend2();
		if (max == null || max.isEmpty()) {
			SmsCache.MAX_EXPEND2.set(1);
		} else {
			SmsCache.MAX_EXPEND2.set(Long.parseLong(max));
		}
	}

	/***
	 * 验证用户签名 已报备 自定义拓展 true通过 false不通过
	 * 
	 * @param uid
	 * @param expend
	 * @param store
	 * @return
	 */
	public String isSignReport(int uid, String expend, String store) {
		try {
			List<UserSign> userSignList = SmsCache.CHANNEL_SIGN.get(uid);
			if (userSignList != null && !userSignList.isEmpty()) {
				for (UserSign userSign : userSignList) {
					if (userSign.getStore().equalsIgnoreCase(store) && userSign.getStatus().equals(1)) {
//						if (userSign.getExpend().equals(expend) || expend.startsWith(userSign.getExpend())) {
							return userSign.getExpend();
//						}
					}
				}
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("判断用户签名是否存在异常", "[UserSignCache.isUserSignCorrect(" + uid + "," + expend + "," + store + ") Exception]" + LogInfo.getTrace(e));
		}
		return null;
	}
	
	/***
	 * 验证用户签名 已报备 自定义拓展 true通过 false不通过(支持多个重复签名时)
	 * 
	 * @param uid
	 * @param expend
	 * @param store
	 * @param repeatSignNum
	 * @return
	 */
	public String isSignReport(int uid, String expend, String store, int repeatSignNum) {
		try {
			if(repeatSignNum == 0){
				return isSignReport(uid, expend, store);
			}
			List<UserSign> userSignList = SmsCache.CHANNEL_SIGN.get(uid);
			if (userSignList != null && !userSignList.isEmpty()) {
				for (UserSign userSign : userSignList) {
					if (userSign.getStore().equalsIgnoreCase(store) && userSign.getStatus().equals(1)) {
						//如果一个用户支持多个签名，找拓展和签名一致的那个
						if (userSign.getUserexpend().equals(expend) || expend.startsWith(userSign.getUserexpend())) {
							return userSign.getExpend();
						}
					}
				}
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("判断用户签名是否存在异常", "[UserSignCache.isUserSignCorrect(" + uid + "," + expend + "," + store + ") Exception]" + LogInfo.getTrace(e));
		}
		return null;
	}

	/***
	 * 获取用户签名拓展号
	 * 
	 * @param uid
	 * @param store
	 * @return
	 */
	public String getSignExpendReport(int uid, String store) {
		try {
			List<UserSign> userSignList = SmsCache.CHANNEL_SIGN.get(uid);
			if (userSignList != null && !userSignList.isEmpty()) {
				for (UserSign userSign : userSignList) {
					if (userSign.getStore().equalsIgnoreCase(store) && userSign.getStatus().equals(1)) {
						return userSign.getExpend();
					}
				}
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("后去用户签名拓展号异常", "[UserSignCache.getUserSignExpend(" + uid + "," + store + ") Exception]" + LogInfo.getTrace(e));
		}
		return null;
	}

	/***
	 * 获取用户签名信息
	 * 
	 * @param uid
	 * @param expid
	 * @param store
	 * @return
	 */
	public UserSign getUserSign(int uid, String expid, String store) {
		try {
			if (SmsCache.USER_SIGN.containsKey(uid)) {
				List<UserSign> list = SmsCache.USER_SIGN.get(uid);
				for (UserSign userSign : list) {
					if (userSign != null && userSign.getStore().equalsIgnoreCase(store)) {
						return userSign;
					}
				}

				if (expid == null || expid.equals("")) {
					expid = uid + "";
				} else if (!expid.startsWith(uid + "")) {
					expid = uid + "" + expid;
				}

				// 临时加上用户推送拓展判断
				for (UserSign userSign : list) {
					if (userSign != null && userSign.getStore().equalsIgnoreCase(store) && userSign.getUserexpend().equals(expid)) {
						return userSign;
					}
				}
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("获取用户签名信息异常", "[UserSignCache.getUserSignMap(" + uid + "," + expid + "," + store + ") Exception]" + LogInfo.getTrace(e));
		}
		return null;
	}
	
	/***
	 * 获取用户签名信息(支持多个重复签名时)
	 * 
	 * @param uid
	 * @param expid
	 * @param store
	 * @param repeatSignNum
	 * @return
	 */
	public UserSign getUserSign(int uid, String expid, String store, int repeatSignNum) {
		try {
			if(repeatSignNum == 0){
				return getUserSign(uid, expid, store);
			}
			
			if (SmsCache.USER_SIGN.containsKey(uid)) {
				List<UserSign> list = SmsCache.USER_SIGN.get(uid);
				if(list ==null || list.size()==0){
					return null;
				}
				
				List<UserSign> repeatSignList = new ArrayList<UserSign>();
				for (UserSign userSign : list) {
					if (userSign != null && userSign.getStore().equalsIgnoreCase(store)) {
						if(userSign.getType() ==2) {
							repeatSignList.add(userSign);
						}
						
						if (expid.startsWith(uid + "") && (userSign.getUserexpend().equals(expid) || expid.startsWith(userSign.getUserexpend()))) {
							return userSign;
						}else if (!expid.startsWith(uid + "") && (userSign.getUserexpend().equals(uid +expid) || (uid +expid).startsWith(userSign.getUserexpend()))) {
							return userSign;
						}
					}
				}
				
				//没有匹配到签名拓展，如果相同签名个数小于最大数，则返回null并生成签名，否则拿添加时间最近的那个签名
				if(repeatSignList.size()<(repeatSignNum+1)){
					return null;
				}else{
					//排序后获取最新添加的那个签名拓展
					UserSign.Sort(repeatSignList);
					return repeatSignList.get(0);
				}
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("获取用户签名信息异常", "[UserSignCache.getUserSignMap(" + uid + "," + expid + "," + store + ") Exception]" + LogInfo.getTrace(e));
		}
		return null;
	}
	
	/****
	 * 判断缓存中是否存在该拓展号
	 * 
	 * @param expend
	 * @return
	 */
	public boolean isCurrentExpend(String expend) {
		if (SmsCache.EXPEND_SIGN.containsKey(expend)) {
			return true;
		}
		return false;
	}

	/***
	 * 获取最大拓展号
	 * 
	 * @param uid
	 * @param userType
	 * @return
	 */
	public String getMaxExpend(int uid, int userType) {
		try {
			String maxExpend = "" + uid;
			if (userType == 1) {
				// 生成最大的拓展号
				// 循环找出最大值
				long k = 9999;
				while (true) {
					k++;
					if(SmsCache.EXPEND_SIGN.containsKey(uid+""+k)){
						continue;
					}else{
						maxExpend = this.appendString(k + "", 5, "0");
						break;
					}
				}
			} else {
				// 如果为普通用户：拓展生成规则为：7位自增长
				Long maxExpend2 = SmsCache.MAX_EXPEND2.incrementAndGet();
				maxExpend = appendString(maxExpend2.toString(), 7, "0");
			}

			return maxExpend;
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("获取用户签名最大拓展号异常", "[UserSignCache.getMaxExpend(" + uid + "," + userType + ") Exception]" + LogInfo.getTrace(e));
		}
		return null;
	}

	/***
	 * 字符串拼接
	 * 
	 * @param content
	 * @param size
	 * @param appendStr
	 * @return
	 */
	private String appendString(String content, int size, String appendStr) {
		try {
			if (content.length() < size) {
				int length = content.length();
				for (int i = 0; i < size - length; i++) {
					content = appendStr + content;
				}
			} else if (content.length() > size) {
				log.error("拼接的字符串超出最大限制");
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("字符串补位异常", "[UserSignCache.deleteUserSign(" + content + "," + size + "," + appendStr + ") Exception]" + LogInfo.getTrace(e));
		}
		return content;
	}
	
	public UserSign getUserSign(int uid, String expid){
		List<UserSign> userSignList = SmsCache.USER_SIGN.get(uid);
		if (userSignList != null) {
			for(UserSign userSign:userSignList){
				if(userSign.getExpend().equals(expid)){
					return userSign;
				}
			}
		}else{
			return null;
		}
		
		return null;
	}
}
