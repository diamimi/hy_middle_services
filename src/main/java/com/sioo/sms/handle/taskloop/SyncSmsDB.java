package com.sioo.sms.handle.taskloop;

import com.sioo.cache.UserSmsCache;
import com.sioo.dao.SmsUserDao;
import com.sioo.hy.cmpp.vo.ConsumeVo;
import com.sioo.hy.cmpp.vo.SmsUserVo;
import com.sioo.log.LogInfo;
import com.sioo.util.SmsCache;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/****
 * 用户短信余额
 * 
 * @author OYJM
 * @date 2016年9月7日
 *
 */
public class SyncSmsDB {
	private static Logger log = Logger.getLogger(SyncSmsDB.class);

	/**
	 * 同步用户短信余额
	 * 
	 * 设置自增长标志，从缓存中拿取数据前先自增长，保证拿出来的数据，与正在保存中的数据不冲突
	 * */
	public static void sysnSms() {
		log.info("sysnSms ScheduledThreadPoolExecutor start...");
		// 构造一个ScheduledThreadPoolExecutor对象，并且设置它的容量为1
		ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
		// 延迟执行定时任务，同步用户余额
		exec.scheduleAtFixedRate(new Runnable() {
			public void run() {
				try {
					// 判断是否系统暂停了
					if (!SmsCache.CONTROL) {
						return;
					}

					SmsCache.QUEUE_SMS_CACHE_FLG = false;

					UserSmsCache userSmaCache = UserSmsCache.getInstance();
					// 如果没数据先不处理
					if (userSmaCache.isEmpty()) {
						SmsCache.QUEUE_SMS_CACHE_FLG = true;
						Thread.sleep(1000);
						return;
					}

					// 标志先自增长，防止数据冲突
					Long currentKey = SmsCache.ATOMIC.incrementAndGet();
					SmsCache.SUB_ATOMIC = new AtomicLong(1);
					// 暂停再获取，防止pid未覆盖
					Thread.sleep(5);
					// 获取当前统计的key
					Long pid = currentKey - 1;
					// 获取改批次的计费数量
					Map<String, Integer> map = userSmaCache.getSmsKouMap(pid);
					if (map == null || map.isEmpty()) {
						SmsCache.QUEUE_SMS_CACHE_FLG = true;
						userSmaCache.removeSmsKouMap(pid);
						return;
					}
					// log.error("get pid: " + pid + " map size: " +
					// map.size());
					Integer uidTemp = 0;
					// 将用户条数统计
					Map<Integer, Integer> mapUser = new ConcurrentHashMap<Integer, Integer>();
					for (String key : map.keySet()) {
						// log.error("get pid: " + pid + " key: " + key +
						// " value: " + map.get(key));
						if (key.isEmpty() || key.length() <= 1 || key.indexOf("_") == -1) {
							continue;
						}
					/**
					 * 获得用户id
					 */
						uidTemp = Integer.parseInt(key.split("_")[1]);
						if (mapUser.containsKey(uidTemp)) {
							mapUser.put(uidTemp, mapUser.get(uidTemp) + map.get(key));
						} else {
							mapUser.put(uidTemp, map.get(key));
						}
					}
					// 先更新消费记录
					String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
					List<ConsumeVo> consumeList = new ArrayList<ConsumeVo>();
					ConsumeVo consumeVo = null;
					for (Integer uid : mapUser.keySet()) {
						consumeVo = new ConsumeVo();
						consumeVo.setUid(uid);
						consumeVo.setDate(Long.parseLong(date));
						consumeVo.setKousms(mapUser.get(uid));
						consumeList.add(consumeVo);
					}
					SmsUserDao.getInstance().saveSmsUserConsume(consumeList);
					int result = SmsUserDao.getInstance().updateSmsUserConsume(consumeList, Long.parseLong(date));
					if (result < 1) {
						return;
					}

					// 再更新余额
					List<SmsUserVo> list = new ArrayList<SmsUserVo>();
					SmsUserVo smsUserVo = null;
					for (Integer uid : mapUser.keySet()) {
						smsUserVo = new SmsUserVo();
						smsUserVo.setUid(uid);
						smsUserVo.setSms(mapUser.get(uid));
						if(SmsCache.USER_LINE_SYNC_STATE.get(uid)!=null&&SmsCache.USER_LINE_SYNC_STATE.get(uid)==1){
							continue;
						}
						list.add(smsUserVo);
					}
					// 修改数据库余额及发送条数
					result = SmsUserDao.getInstance().updateSmsUser(list);
					if (result < 1) {
						SmsCache.QUEUE_SMS_CACHE_FLG = true;
						return;
					}

					// 处理数据
					for (Integer uid : mapUser.keySet()) {
						Integer kouSms = mapUser.get(uid);
						if (null != kouSms) {
							// 如果短信扣除了，这里在同步，客户余额就为负数.也就是剩余的短信小于增量，就变负数。扣除余额同时更改CACHE_CHARGEING
							// 获取用户计费前的短信余额
							Integer smsBefore = userSmaCache.getUserSms(uid);
							// 计算后的短信余额
							Integer smsAfter = smsBefore - kouSms;
							// 修改缓存中用户短信条数
							userSmaCache.putUserSms(uid, smsAfter);
							log.info("sync user("+uid+"), smsbefore:"+smsBefore+",  kousms:"+kouSms+", yukousms:"+userSmaCache.getUserSmsYukou(uid)+",  smsAfter:"+smsAfter);
							// LogInfo.getLog().sysn("[SyncSmsDB.sysnSms()], uid: "
							// + uid + ", smsObj: " + smsAfter);
						}
					}

					Thread.sleep(10);
					// 如果已处理的批次全部删除
					userSmaCache.removeSmsKouMap(pid);
					// 防止溢出
					if (currentKey.longValue() > 10000000) {
						SmsCache.ATOMIC = new AtomicLong(1);
					}

				} catch (Exception e) {
					LogInfo.getLog().errorAlert("同步用户余额信息异常", "[SyncSmsDB.sysnSms() Exception]" + LogInfo.getTrace(e));
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						log.error("sleep error! ");
					}
				} finally {
					SmsCache.QUEUE_SMS_CACHE_FLG = true;
				}
			}
		}, 60, 60, TimeUnit.SECONDS); // 5秒后执行，5秒执行一次
	}
}