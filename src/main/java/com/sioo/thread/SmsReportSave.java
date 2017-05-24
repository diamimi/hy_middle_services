package com.sioo.thread;

import com.alibaba.fastjson.JSON;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sioo.client.cmpp.vo.DeliverVo;
import com.sioo.db.mongo.MongoManager;
import com.sioo.hy.cmpp.vo.SendingVo;
import com.sioo.log.LogInfo;
import com.sioo.util.DateUtils;
import com.sioo.util.RabbitMQProducerUtil;
import com.sioo.util.SmsCache;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/***
 * 状态报告mongodb操作类
 * 
 * 插入到状态报告中 修改历史理论状态
 * 
 * @author OYJM
 * @date 2016年9月29日
 *
 */
public class SmsReportSave implements Runnable {
	private static Logger log = Logger.getLogger(SmsReportSave.class);

	private MongoManager mongo = null;
	private RabbitMQProducerUtil util = null;
	public SmsReportSave(MongoManager mongo,RabbitMQProducerUtil util) {
		this.mongo = mongo;
		this.util = util;
	}

	public void run() {
		List<SendingVo> list = null;
		List<DBObject> dbObjRpt = null;
		int size = 0;
		while (true) {
			try {
				// 如果为空，休眠1秒；否则休眠100毫秒
				if (SmsCache.QUEUE_REPORT.isEmpty()) {
					Thread.sleep(1000);
					continue;
				} else {
					Thread.sleep(100);
				}

				list = new ArrayList<SendingVo>();
				SmsCache.QUEUE_REPORT_FLG = false;
				size = SmsCache.QUEUE_REPORT.drainTo(list, 2000);
				if (size > 0) {
					// 批量入库
					dbObjRpt = new ArrayList<DBObject>();
					long now = DateUtils.getTime();
					//上次告警是否在一小时前
//					boolean flg = (now-SmsCache.NOSIGN_TIME.get())>10000;
					for (SendingVo vo : list) {
						// 长短信时添加两条
						for (int i = 0; i < vo.getContentNum(); i++) {
							dbObjRpt.add(convertRpt(vo,now));
							//添加签名错误的次数
//							if(flg && (vo.getRptStat().equals(ConstantStatus.USER_STATUS_NOSIGN) || vo.getRptStat().equals(ConstantStatus.SYS_STATUS_NOREPORTSIGN))){
//								SmsCache.NOSIGN_COUNT.getAndIncrement();
//							}
						}
						//发送到客户端的状态，用于更新历史记录
						util.send("DELIVER_TO_CLIENT", convertDeliverVo(vo,now));
						
					}
					//用户状态表
					mongo.batchInsert("sms_report", dbObjRpt);
					//给用户推送状态表
					mongo.batchInsert("sms_report_push", dbObjRpt);
					
//					if(SmsCache.NOSIGN_COUNT.get()>=50){
//						log.info("sign error count>50.");
//						//重置条数，最后发送告警时间
//						SmsCache.NOSIGN_COUNT = new AtomicInteger(0);
//						SmsCache.NOSIGN_TIME = new AtomicLong(now);
//					}
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
				LogInfo.getLog().errorAlert("保存短信发送状态异常", "[SmsCheckThread.run() Exception,  save to mongodb sms_report failed]" + LogInfo.getTrace(e));
			} finally {
				SmsCache.QUEUE_REPORT_FLG = true;
				// 释放内存
				if (list != null) {
					list.clear();
					list = null;
				}
				if (dbObjRpt != null) {
					dbObjRpt.clear();
					dbObjRpt = null;
				}
			}
		}
	}

	private DBObject convertRpt(SendingVo vo, long now) {
		try {
			DBObject obj = new BasicDBObject();
			obj.put("a", vo.getUid()); // uid
			obj.put("b", vo.getMobile()); // mobile
			obj.put("c", vo.getRptStat()); // rpt_code
			obj.put("d", vo.getId()); // hisId
			obj.put("e", vo.getPid());// pid
			obj.put("f", 0);// status
			obj.put("g", now);// rpt_time
			obj.put("h", vo.getChannel());// channel
			return obj;
		} catch (Exception e) {
			log.error("[SmsReportSave.convertRpt Exception];Msg:" + e.getMessage(), e);
		}
		return null;
	}
	
	
	private DeliverVo convertDeliverVo(SendingVo vo, long now){
		DeliverVo deliver = new DeliverVo();
		deliver.setHisId(vo.getId());
		deliver.setMobile(vo.getMobile()+"");
		deliver.setChannel(vo.getChannel());
		deliver.setUid(vo.getUid());
		deliver.setPid(vo.getPid());
		deliver.setRpt_code(vo.getRptStat());
		deliver.setRpt_time(now+"");
		return deliver;
	}
}
