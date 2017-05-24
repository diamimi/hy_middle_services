package com.sioo.thread;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sioo.db.mongo.MongoManager;
import com.sioo.hy.cmpp.vo.SendingVo;
import com.sioo.log.LogInfo;
import com.sioo.util.RabbitMQProducerUtil;
import com.sioo.util.SmsCache;

/***
 * 保存发送记录到mongoDB
 * 
 * @author OYJM
 * @date 2016年10月12日
 *
 */
public class SmsSendHistoryUnknownSave implements Runnable {
	private MongoManager mongo = null;
	private RabbitMQProducerUtil util = null;
	public SmsSendHistoryUnknownSave(MongoManager mongo,RabbitMQProducerUtil util) {
		this.mongo = mongo;
		this.util = util;
	}

	public void run() {
		List<SendingVo> list = null;
		List<DBObject> dbObj = null;
		int size = 0;
		while (true) {
			try {
				// 如果为空，休眠1秒；否则休眠100毫秒
				if (SmsCache.QUEUE_SENDING_HISTORY.isEmpty()) {
					Thread.sleep(1000);
					continue;
				} else {
					Thread.sleep(100);
				}

				list = new ArrayList<SendingVo>();
				SmsCache.QUEUE_SENDING_HISTORY_FLG = false;
				size = SmsCache.QUEUE_SENDING_HISTORY.drainTo(list, 2000);
				if (size > 0) {
					// 批量入库
					dbObj = new ArrayList<DBObject>();
					for (SendingVo vo : list) {
						dbObj.add(convertBean(vo));
						//需要往客户端放入一份
						util.send("HISTORY_TO_CLIENT", vo);
					}
					mongo.batchInsert("sms_send_history_unknown", dbObj);
				}
			} catch (Exception e) {
				// 异常消息打印
				if (list != null && !list.isEmpty()) {
					StringBuffer stb = new StringBuffer();
					for (SendingVo vo : list) {
						stb.append(JSON.toJSONString(vo)).append("\r\n");
					}
					LogInfo.getLog().errorData(stb.toString());
				}
				LogInfo.getLog().errorAlert("保存历史记录异常", "[SmsSendHistoryUnknownSave.run() Exception, save to mysql sms_send_history_unknown failed]" + LogInfo.getTrace(e));
			} finally {
				SmsCache.QUEUE_SENDING_HISTORY_FLG = true;
				// 释放内存
				if (list != null) {
					list.clear();
					list = null;
				}
				if (dbObj != null) {
					dbObj.clear();
					dbObj = null;
				}
			}
		}
	}

	/***
	 * 将bean转换为DBObject对象
	 * 
	 * @param vo
	 * @return
	 */
	private DBObject convertBean(SendingVo vo) {
		DBObject smsSendHistoryUnknown = new BasicDBObject();
		// smsSendHistoryUnknown.put("_id", vo.getId());
		smsSendHistoryUnknown.put("a", vo.getId()); // id
		smsSendHistoryUnknown.put("b", 0); // stype
		smsSendHistoryUnknown.put("c", vo.getMtype()); // mtype
		smsSendHistoryUnknown.put("d", vo.getSenddate()); // senddate
		smsSendHistoryUnknown.put("e", vo.getUid()); // uid
		smsSendHistoryUnknown.put("f", Long.valueOf(vo.getMobile()));// mobile
		smsSendHistoryUnknown.put("g", vo.getChannel());// channel
		smsSendHistoryUnknown.put("h", vo.getContent());// content
		smsSendHistoryUnknown.put("i", vo.getContentNum());// contentNum
		smsSendHistoryUnknown.put("j", vo.getSucc());// succ
		smsSendHistoryUnknown.put("k", vo.getFail());// fail
		smsSendHistoryUnknown.put("l", vo.getMtStat() == null ? 0 : vo.getMtStat());// mtstat
		smsSendHistoryUnknown.put("m", vo.getPid());// pid
		smsSendHistoryUnknown.put("n", vo.getGrade());// grade
		smsSendHistoryUnknown.put("o", vo.getExpid());// expid
		smsSendHistoryUnknown.put("p", vo.getLocation());// location
		smsSendHistoryUnknown.put("q", 0);// arrive_succ
		smsSendHistoryUnknown.put("r", vo.getArrive_fail());// arrive_fail
		smsSendHistoryUnknown.put("s", vo.getStat());// stat
		smsSendHistoryUnknown.put("z", vo.getContentNum());// contentNum
		return smsSendHistoryUnknown;
	}
}
