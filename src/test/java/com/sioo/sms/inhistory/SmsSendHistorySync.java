package com.sioo.sms.inhistory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sioo.db.druid.DBUtils;
import com.sioo.db.mongo.MongoManager;
import com.sioo.util.DateUtils;
import com.sioo.util.SmsCache;

public class SmsSendHistorySync {
	private static Logger log = Logger.getLogger(SmsSendHistorySync.class);

	public static void main(String[] args) throws Exception {
		SmsCache.mongoHost = "101.227.68.21";
		SmsCache.mongoPort = 27017;
		SmsCache.mongoDbName = "SMS";
		sync();
	}

	private static void sync() throws Exception {
		System.out.println("===sync SmsSendHistory start===");
		// 查询同步表数据总条数
		String sql = "select count(1) from smshy.sms_send_history_unknown_copy";
		Object object = DBUtils.getInstance().findUniqueValue(sql);
		// 如果为空不操作
		if (object == null) {
			return;
		}
		// 获取条数
		long size = Long.parseLong(object.toString());
		// 如果没有数据不操作
		if (size < 1) {
			return;
		}

		log.info("wait sync total size: " + size);

		// 查询语句
		sql = "select `id`,  `stype`,  `mtype`,  `senddate`,  `sendtime`,  `uid`,  `mobile`,  `channel`,  `content`,  `content_num`,  `mobile_num`, `stat`,"
				+ "`mtstat`,  `pid`,  `grade`,  `expid`,  `md`,  `rptstat`,  `rpttime`,  `stotal`,  `sindex`,  `sseq`,  `msgid`,  `idorder`,  `contentmd` from smshy.`sms_send_history_unknown_copy` order by `id`";

		List<DBObject> dbObjList = null;
		List<Map<String, Object>> smsSendHistoryList = null;
		MongoManager mongo = MongoManager.getInstance();

		// 每次处理2000条数据，计算需要循环多少次
		long length = size / 2000 + (size % 2000 > 0 ? 1 : 0);

		String sqlTmep = "";
		for (int i = 0; i < length; i++) {
			// 设置查询每次的数量
			sqlTmep = sql + " limit " + i * 2000 + ",2000";
			log.info(sqlTmep);
			// 查询数据列表
			smsSendHistoryList = DBUtils.getInstance().findModelRows(sqlTmep);
			if (smsSendHistoryList != null && smsSendHistoryList.size() > 0) {
				// 转换为mongodb数据并保存
				dbObjList = new ArrayList<DBObject>();
				for (Map<String, Object> map : smsSendHistoryList) {
					dbObjList.add(convertBean(map));
				}
				mongo.batchInsert("sms_send_history_unknown", dbObjList);
				log.info("sync[" + (i + 1) + "] to mongodb success");
				// 删除记录
				long beginId = Long.parseLong(smsSendHistoryList.get(0).get("id").toString());
				long endId = Long.parseLong(smsSendHistoryList.get(smsSendHistoryList.size() - 1).get("id").toString());
				DBUtils.getInstance().executeUpdate("delete from smshy.sms_send_history_unknown_copy where id>=" + beginId + " and id<=" + endId);
				length--;
				log.info("sync[" + (i + 1) + "] delete by id (" + beginId + "," + endId + ") success");
			}

		}
		System.out.println("===sync SmsSendHistory end===");
	}

	private static DBObject convertBean(Map<String, Object> map) {
		DBObject smsSendHistoryUnknown = new BasicDBObject();
		// smsSendHistoryUnknown.put("_id", vo.getId());
		smsSendHistoryUnknown.put("a", map.get("id")); // id
		smsSendHistoryUnknown.put("b", map.get("stype")); // stype
		smsSendHistoryUnknown.put("c", map.get("mtype")); // mtype
		Date date = (Date) map.get("senddate");
		smsSendHistoryUnknown.put("d", DateUtils.getLongTime(date)); // senddate
		smsSendHistoryUnknown.put("e", map.get("uid")); // uid
		smsSendHistoryUnknown.put("f", Long.parseLong(map.get("mobile").toString()));// mobile
		smsSendHistoryUnknown.put("g", map.get("channel"));// channel
		smsSendHistoryUnknown.put("h", map.get("content"));// content
		smsSendHistoryUnknown.put("i", map.get("content_num"));// contentNum
		int stat = Integer.valueOf( map.get("stat").toString());
		smsSendHistoryUnknown.put("j", stat == 1 ? 1 : 0);// succ
		smsSendHistoryUnknown.put("k", stat == 1 ? 0 : 1);// fail
		smsSendHistoryUnknown.put("l", map.get("mtstat"));// mtstat
		smsSendHistoryUnknown.put("m", map.get("pid"));// pid
		smsSendHistoryUnknown.put("n", map.get("grade"));// grade
		smsSendHistoryUnknown.put("o", map.get("expid"));// expid
		smsSendHistoryUnknown.put("p", "未知");// location
		String rptstat = map.get("rptstat").toString(); // DELIVRD
		smsSendHistoryUnknown.put("q", rptstat.equals("DELIVRD") ? 1 : 0);// arrive_succ
		smsSendHistoryUnknown.put("r", rptstat.equals("DELIVRD") ? 0 : 1);// arrive_fail
		smsSendHistoryUnknown.put("s", stat);// stat
		return smsSendHistoryUnknown;
	}
}
