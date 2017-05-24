package com.sioo.service;

import com.sioo.cache.UserSmsCache;
import com.sioo.log.LogInfo;
import com.sioo.servlet.HttpSubmitServer;
import com.sioo.util.EhcacheUtil;
import com.sioo.util.FileUtils;
import com.sioo.util.SmsCache;
import com.sioo.util.UpdateCacheConstant.METHOD;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/***
 * 关闭服务
 * 
 * @author OYJM
 * @date 2016年10月23日
 *
 */
public class StopService {
	private StopService() {
	}

	private static Logger log = Logger.getLogger(StopService.class);
	private static StopService stopService;

	public static StopService getInstance() {
		if (stopService != null) {
			return stopService;
		}
		synchronized (SmsService.class) {
			if (stopService == null) {
				stopService = new StopService();
			}
		}
		return stopService;
	}

	public String excute(Integer method, Boolean control) {
		if (method == METHOD.UPDATE) {
			if (this.stop(control)) {
				return HttpSubmitServer.SUCC;
			} else {
				return HttpSubmitServer.FAIL;
			}
		} else if (method == METHOD.GET) {
			return SmsCache.CONTROL + "";
		}
		return HttpSubmitServer.FAIL;
	}

	public boolean stop(boolean control) {
		try {
			SmsCache.CONTROL = control;
			if (!SmsCache.CONTROL) {
				log.info("stop service start...");
				boolean flg = false;
				int i = 0;
				do {
					flg = false;
					i++;
					// 判断本地队列的消息是否处理完
					// 接收消息队列线程
					if (!SmsCache.QUEUE_SMSCHECK_NORMAL.isEmpty() && !SmsCache.QUEUE_SENDING_PRIORITIZED.isEmpty()) {
						for (String key : SmsCache.QUEUE_FIRST_FLG.keySet()) {
							if (SmsCache.QUEUE_FIRST_FLG.get(key)) {
								flg = true;
								break;
							}
						}
						if (flg) {
							log.info("local history queue is not empty.sleep...");
							Thread.sleep(1000);
							continue;
						}
					}
					log.info("local revice queue is empty.");

					if (!SmsCache.QUEUE_SMS_BATCH_FLG) {
						log.info("local revice batch queue is running.sleep...");
						Thread.sleep(1000);
						flg = true;
						continue;
					}
					log.info("local revice batch queue is empty.");
					
					if (!SmsCache.QUEUE_SMS_CHANNEL_FLG) {
						log.info("local revice smsChannelCheck queue is running.sleep...");
						Thread.sleep(1000);
						flg = true;
						continue;
					}
					log.info("local revice smsChannelCheck queue is empty.");

					// 历史记录保存线程
					if (!SmsCache.QUEUE_SENDING_HISTORY.isEmpty() && !SmsCache.QUEUE_SENDING_HISTORY_FLG) {
						log.info("local history queue is not empty.sleep...");
						Thread.sleep(1000);
						flg = true;
						continue;
					}
					log.info("local history queue is empty.");

					// 审核信息保存线程
					if (!SmsCache.QUEUE_SENDING_RELEASE.isEmpty() && !SmsCache.QUEUE_SENDING_RELEASE_FLG) {
						log.info("local release queue is not empty.sleep...");
						Thread.sleep(1000);
						flg = true;
						continue;
					}
					log.info("local release queue is empty.");
					
					// 用户状态报告保存线程
					if (!SmsCache.QUEUE_REPORT.isEmpty() && !SmsCache.QUEUE_REPORT_FLG) {
						log.info("local report queue is not empty.sleep...");
						Thread.sleep(1000);
						flg = true;
						continue;
					}
					log.info("local report queue is empty.");

					// 队列消息保存线程
					if (!SmsCache.QUEUE_SENDING.isEmpty() && !SmsCache.QUEUE_SENDING_FLG) {
						log.info("local sending queue is not empty.sleep...");
						Thread.sleep(1000);
						flg = true;
						continue;
					}
					log.info("local sending queue is empty.");

					// 用户报表保存线程
					if (!SmsCache.QUEUE_RESULT.isEmpty() && !SmsCache.QUEUE_RESULT_FLG) {
						log.info("local result queue is not empty.sleep...");
						Thread.sleep(1000);
						flg = true;
						continue;
					}
					log.info("local result queue is empty.");

					if (!SmsCache.QUEUE_RATIO.isEmpty() && !SmsCache.QUEUE_RATIO_FLG) {
						log.info("local ratio queue is not empty.sleep...");
						Thread.sleep(1000);
						flg = true;
						continue;
					}
					log.info("local ratio queue is empty.");
					
					if (!SmsCache.QUEUE_CHECK_RELEASE_FLG) {
						log.info("check release is running.sleep...");
						Thread.sleep(1000);
						flg = true;
						continue;
					}
					log.info("check release is stop.");

					if (!SmsCache.QUEUE_CHECK_ALERT_FLG) {
						log.info("check alert is running.sleep...");

						Thread.sleep(1000);
						flg = true;
						continue;
					}
					log.info("check alert is stop.");
					
					//如果正在保存用户余额，等待保存完
					if(!SmsCache.QUEUE_SMS_CACHE_FLG){
						log.info("save usersms is running.sleep...");
						Thread.sleep(1000);
						flg = true;
						continue;
					}
					// 将计费信息从缓存读到文件中
					if (!UserSmsCache.getInstance().isEmpty()) {
						UserSmsCache userSmsCache = UserSmsCache.getInstance();
						log.info("sms cache is not empty.");
						flg = true;
						// 从缓存中读取内容并放入本地文件中
						Set<Long> keys = userSmsCache.getAllKey();
						ConcurrentHashMap<String, Integer> map=null;
						Integer uidTemp;
						// 将用户条数统计
						Map<Integer, Integer> mapUser = new ConcurrentHashMap<Integer, Integer>();
						for (Long pid : keys) {
							map = userSmsCache.getSmsKouMap(pid);
							try {
								for (String key : map.keySet()) {
                                    if (key.isEmpty() || key.length() <= 1 || key.indexOf("_") == -1) {
                                        continue;
                                    }
                                    log.info("get pid: " + pid + " uid: " + key + " sms: " + map.get(key));
                                    uidTemp = Integer.parseInt(key.split("_")[1]);
                                    if (mapUser.containsKey(uidTemp)) {
                                        mapUser.put(uidTemp, mapUser.get(uidTemp) + map.get(key));
                                    } else {
                                        mapUser.put(uidTemp, map.get(key));
                                    }
                                }
							} catch (Exception e) {
								log.error(e.getMessage(),e);
							}
						}
						FileUtils.saveSmsCancheTxtFile(mapUser);
						for (Long pid : keys) {
							userSmsCache.removeSmsKouMap(pid);
						}
						continue;
					}
					
					log.info("sms cache is empty.");

					// 校验第二遍
					if (i < 2) {
						flg = true;
					}
				} while (flg);
				this.saveCache();
				log.info("=======stop service success=========");
			}
			return true;
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("停止服务异常", "[StopService.stop() Exception]" + LogInfo.getTrace(e));
		}
		return false;
	}

	/**
	 * 把本地缓存的数据持久化
	 */
	public void saveCache() throws Exception{
		if(SmsCache.CACHE_FLAG){
			try {
				SmsCache.CACHE_FLAG=false;
				EhcacheUtil.getInstance().put("sms", "USER_BLACK_MOBILE",  SmsCache.USER_BLACK_MOBILE);
				EhcacheUtil.getInstance().put("sms", "ALL_BLACK_USER_MOBILE",  SmsCache.ALL_BLACK_USER_MOBILE);
				EhcacheUtil.getInstance().put("sms", "MOBILE_AREA",  SmsCache.MOBILE_AREA);

				EhcacheUtil.getInstance().put("sms", "CHANNEL_SIGN",  SmsCache.CHANNEL_SIGN);
				EhcacheUtil.getInstance().put("sms", "USER_SIGN",  SmsCache.USER_SIGN);
				EhcacheUtil.getInstance().put("sms", "EXPEND_SIGN",  SmsCache.EXPEND_SIGN);

				EhcacheUtil.getInstance().put("sms","USER_ALERT",SmsCache.USER_ALERT);

				EhcacheUtil.getInstance().put("group", "GROUP_WHITE_MOBILE",  SmsCache.GROUP_WHITE_MOBILE);
				EhcacheUtil.getInstance().put("group", "GROUP_BLACK_MOBILE",  SmsCache.GROUP_BLACK_MOBILE);
				EhcacheUtil.getInstance().put("group", "GROUP_WHITE_SIGN",  SmsCache.GROUP_WHITE_SIGN);
				EhcacheUtil.getInstance().put("group", "GROUP_BLACK_SIGN",  SmsCache.GROUP_BLACK_SIGN);
				EhcacheUtil.getInstance().put("group", "GROUP_RELEASE_WORDS_SCREENTYPE",  SmsCache.GROUP_RELEASE_WORDS_SCREENTYPE);
				EhcacheUtil.getInstance().put("group", "GROUP_AUTO_WORDS_SCREENTYPE",  SmsCache.GROUP_AUTO_WORDS_SCREENTYPE);

				EhcacheUtil.getInstance().put("repeat", "USER_REPEAT_MOBILE",  SmsCache.USER_REPEAT_MOBILE);
				EhcacheUtil.getInstance().put("repeat", "USER_REPEAT_MOBILE_TEMP",  SmsCache.USER_REPEAT_MOBILE_TEMP);
			} catch (Exception e) {
				log.error(e.getMessage(),e);
			}finally {
				EhcacheUtil.getInstance().shutdown();
				log.info("=======缓存保存完毕=========");
			}
		}
	}
}
