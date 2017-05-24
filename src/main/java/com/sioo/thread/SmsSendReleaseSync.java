package com.sioo.thread;

import com.alibaba.fastjson.JSON;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sioo.cache.ChannelCache;
import com.sioo.cache.LocalQueueCache;
import com.sioo.cache.UserCache;
import com.sioo.dao.SmsUserSendingReleaseDao;
import com.sioo.db.mongo.MongoManager;
import com.sioo.hy.cmpp.vo.SendingVo;
import com.sioo.log.LogInfo;
import com.sioo.mq.SendChannelProducer;
import com.sioo.util.ConstantStatus;
import com.sioo.util.ConstantSys;
import com.sioo.util.RabbitMQProducerUtil;
import com.sioo.util.SmsCache;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SmsSendReleaseSync implements Runnable {
	private RabbitMQProducerUtil util = null;
	private MongoManager mongo = null;

	public SmsSendReleaseSync(RabbitMQProducerUtil util, MongoManager mongo) {
		this.util = util;
		this.mongo = mongo;
	}

	private static Logger log = Logger.getLogger(SmsSendReleaseSync.class);

	public void run() {
		SmsCache.QUEUE_CHECK_RELEASE_FLG = false;
		LocalQueueCache localQueueCache = LocalQueueCache.getInstance();
		List<SendingVo> passList = null;
		while (true) {
			if (SmsCache.CONTROL) {
				try {
					passList = SmsUserSendingReleaseDao.getInstance().getSmsUserSendingRelease();
					if (passList == null || passList.isEmpty()) {
						// log.info("release is empty,sleep...");
						SmsCache.QUEUE_CHECK_RELEASE_FLG = true;
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							log.error("sleep error!");
						}
					} else {
						// log.debug("get release size:" + passList.size());
						SmsCache.QUEUE_CHECK_RELEASE_FLG = false;
						List<Long> ids = new ArrayList<Long>();
						StringBuffer stb = new StringBuffer();
						DBObject where = null;
						DBObject set = null;
						for (SendingVo vo : passList) {
							try {
								stb.append("===审核消息===   ").append(vo.getHisids());
								if (vo.getHandStat() == 1) {
									stb.append("通过审核");
									long id = vo.getId();
									
									// 获取用户信息
									Map<String, Object> user_map = UserCache.getInstance().getUser(vo.getUid());
									// 放入通道队列
									vo.setId(vo.getHisids());

									// 判断通道状态，如果通道停止，放入队列中
									Map<String, Object> channelInfo = ChannelCache.getInstance().getChannel(vo.getChannel());
									// 判断通道状态0为正常,1为暂停,2为停止
									int status = 1;
									if (channelInfo != null && channelInfo.get("status") != null) {
										status = Integer.parseInt(String.valueOf(channelInfo.get("status").toString()));
									}

									if (status > 0) {
										stb.append("[失败]，通道已停止。");
										// 判断通道状态，如果通道停止，放入队列中
										vo.setAutoFlag(ConstantSys.AUTO_FLAG_NONE);
										localQueueCache.putSending(vo);
										continue;
									} else {
										// 判断通道状态，如果通道正常，发送短消息
										SendChannelProducer.send(vo, util, Integer.parseInt(String.valueOf(user_map.get("userkind"))));
										stb.append("[成功]，已发送审核消息。");
									}

									ids.add(id);
								} else if (vo.getHandStat() == 2) {
									long id = vo.getId();
									stb.append("审核驳回。");
									vo.setId(vo.getHisids());
									// 返回状态报告
									vo.setRptStat(ConstantStatus.SYS_STATUS_REJECT);
									localQueueCache.putReport(vo);

									where = new BasicDBObject();
									where.put("a", vo.getHisids());

									set = new BasicDBObject();
									set.put("r", vo.getContentNum());
									set.put("j", vo.getContentNum());
									set.put("s", 1);
									mongo.update2("sms_send_history_unknown", set, where);

									ids.add(id);
									BasicDBObject set0 = new BasicDBObject();
									set0.put("e", (int)0);//Arrive_succ
									set0.put("f", vo.getContentNum());//Arrive_fail
									BasicDBObject where0 = new BasicDBObject();
									where0.put("b",vo.getUid());//uid
									where0.put("a", Long.parseLong((vo.getSenddate()+"").substring(0, 8)));//time
									mongo.update3("sms_user_day_count", set0, where0);
//									countMongo(vo);
								} else {
									stb.append("审核状态不正确。");
								}

								log.info(stb.toString());
								stb.setLength(0);

							} catch (Exception e) {
								LogInfo.getLog().errorAlert("处理审核信息异常", "[SmsSendReleaseSync.run() Exception]; data=" + JSON.toJSONString(vo) + LogInfo.getTrace(e));
							}
						}
						passList.isEmpty();
						passList = null;
					}
				} catch (Exception e) {
					// 异常消息打印
					if (passList != null && !passList.isEmpty()) {
						StringBuffer stb = new StringBuffer();
						for (SendingVo vo : passList) {
							stb.append(JSON.toJSONString(vo)).append("\r\n");
						}
						LogInfo.getLog().errorData(stb.toString());
					}
					LogInfo.getLog().errorAlert("获取审核信息异常", "[SmsSendReleaseSync.run() Exception,  release sms failed]" + LogInfo.getTrace(e));
					log.error("[SmsSendReleaseSync error];Msg: " + e.getMessage(), e);
				} finally {
					SmsCache.QUEUE_CHECK_RELEASE_FLG = true;
				}
			} else {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {

				}
			}
		}

	}



}
