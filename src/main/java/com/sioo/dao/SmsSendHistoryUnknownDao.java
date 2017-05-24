package com.sioo.dao;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sioo.db.mongo.MongoManager;
import com.sioo.hy.cmpp.vo.SendingVo;
import com.sioo.log.LogInfo;
import com.sioo.util.SmsCache;

/***
 * 历史记录Dao类
 * 
 * @author OYJM
 * @date 2016年9月20日
 *
 */
public class SmsSendHistoryUnknownDao {
	private static SmsSendHistoryUnknownDao smsSendHistoryUnknownDao;

	public static SmsSendHistoryUnknownDao getInstance() {
		if (smsSendHistoryUnknownDao != null) {
			return smsSendHistoryUnknownDao;
		}
		synchronized (SmsSendHistoryUnknownDao.class) {
			if (smsSendHistoryUnknownDao == null) {
				smsSendHistoryUnknownDao = new SmsSendHistoryUnknownDao();
			}
		}
		return smsSendHistoryUnknownDao;
	}

	/***
	 * 往mongodb中插入一条历史记录
	 * 
	 * @param vo
	 * @throws Exception
	 */
	public void saveSmsSendHistoryUnknown(SendingVo vo, MongoManager mongo) {
		try {
			mongo.insert("sms_send_history_unknown", convertBean(vo));
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("保存单条历史记录异常", "[SmsSendHistoryUnknownDao.saveSmsSendHistoryUnknown() Exception]; data:" + JSON.toJSONString(vo) + LogInfo.getTrace(e));
		}
	}

	/**
	 * 批量插入MongoDB
	 * 
	 * @param name
	 * @param obj
	 */
	public void batchInsert(MongoManager mongo, String name, List<DBObject> obj) {
		try {
			mongo.batchInsert(name, obj);
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("批量保存历史记录异常", "[SmsSendHistoryUnknownDao.batchInsert() Exception]; data:" + JSON.toJSONString(obj) + LogInfo.getTrace(e));
		}
	}

	/***
	 * 查询最大ID
	 * 
	 * @return
	 * @throws Exception
	 */
	public Integer findMaxHisId(MongoManager mongo) {
		try {
			DBObject where = new BasicDBObject();
			DBObject subwhere = new BasicDBObject();
			if(SmsCache.SERVER_TYPE == null || SmsCache.SERVER_TYPE == 0){
				subwhere.put("$gte", 10000000);
				subwhere.put("$lte", 1000000000);
			}else{
				subwhere.put("$gte", 1000000001);
				subwhere.put("$lte", 2000000000);
			}
			where.put("a", subwhere);
			//先按时间再按ID排序，防止老数据还未备份又从新生成ID
			DBObject orderBy = new BasicDBObject();
			orderBy.put("d", -1);  //时间
			orderBy.put("a", -1);  //ID
			List<DBObject> list = mongo.getMaxDBObject("sms_send_history_unknown", where, orderBy);
			if (list != null && !list.isEmpty()) {
				DBObject object = list.get(0);
				return Integer.parseInt(object.get("a").toString());
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("获取历史记录最大ID异常", "[SmsSendHistoryUnknownDao.findMaxHisId() Exception]" + LogInfo.getTrace(e));
		}
		return 0;
	}

	/***
	 * 将bean转换为DBObject对象
	 * 
	 * @param vo
	 * @return
	 */
	private DBObject convertBean(SendingVo vo) {
		try {
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
			return smsSendHistoryUnknown;
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("将对象转换为DBObject异常", "[SmsSendHistoryUnknownDao.convertBean() Exception]; data:" + JSON.toJSONString(vo) + LogInfo.getTrace(e));
		}
		return null;
	}
}
