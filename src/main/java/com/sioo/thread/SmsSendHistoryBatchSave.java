package com.sioo.thread;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sioo.db.mongo.MongoManager;
import com.sioo.hy.cmpp.vo.BatchSendingVo;
import com.sioo.log.LogInfo;
import com.sioo.util.SmsCache;

/***
 * 保存批次发送记录到mongoDB
 * 
 * @author OYJM
 * @date 2016年10月12日
 *
 */
public class SmsSendHistoryBatchSave implements Runnable {
	private static Logger log = Logger.getLogger(SmsSendHistoryBatchSave.class);

	private MongoManager mongo = null;

	public SmsSendHistoryBatchSave(MongoManager mongo) {
		this.mongo = mongo;
	}

	public void run() {
		List<BatchSendingVo> list = null;
		List<DBObject> dbObj = null;
		int size = 0;
		while (true) {
			try {
				// 如果为空，休眠1秒；否则休眠100毫秒
				if (SmsCache.QUEUE_BATCHSMS.isEmpty()) {
					Thread.sleep(1000);
					continue;
				} else {
					Thread.sleep(100);
				}

				list = new ArrayList<BatchSendingVo>();
				size = SmsCache.QUEUE_BATCHSMS.drainTo(list, 2000);
				if (size > 0) {
					// 批量入库
					dbObj = new ArrayList<DBObject>();
					for (BatchSendingVo vo : list) {
						dbObj.add(convertBean(vo));
					}
					mongo.batchInsert("sms_send_history_batch", dbObj);
				}
			} catch (Exception e) {
				// 异常消息打印
				if (list != null && !list.isEmpty()) {
					StringBuffer stb = new StringBuffer();
					for (BatchSendingVo vo : list) {
						stb.append(JSON.toJSONString(vo)).append("\r\n");
					}
					LogInfo.getLog().errorData(stb.toString());
				}
				LogInfo.getLog().errorAlert("保存批量消息异常", "[SmsSendHistoryBatchSave.run() Exception,  save to mongodb sms_send_history_batch failed]" + LogInfo.getTrace(e));
			} finally {
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
	private DBObject convertBean(BatchSendingVo vo) {
		DBObject batchSending = new BasicDBObject();
		try {
			batchSending.put("a", vo.getPid()); // pid
			batchSending.put("b", vo.getSenddate()); // senddate
			batchSending.put("c", vo.getNum()); // num
			batchSending.put("d", vo.getContent()); // content
			batchSending.put("e", vo.getUid()); // uid
			return batchSending;
		} catch (Exception e) {
			log.error("[SmsSendHistoryBatchSave.convertRpt Exception]; data: " + batchSending.toString(), e);
		}
		return null;
	}
}
