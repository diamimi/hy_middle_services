package com.sioo.sms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sioo.client.cmpp.vo.DeliverVo;
import com.sioo.db.mongo.MongoManager;
import com.sioo.hy.cmpp.vo.SendingVo;
import com.sioo.log.LogInfo;
import com.sioo.util.DateUtils;
import com.sioo.util.RabbitMQProducerUtil;
import com.sioo.util.SmsCache;

public class SendSms {
	private static Logger log = Logger.getLogger(SendSms.class);

	public static void main(String[] args) throws Exception {
		//sendErrorSMS();
		sendErrorSMS();
	}
	
	
	private static void sendErrorSMS(){
		SmsCache.RABBIT_HOST = "101.227.68.21";
		SmsCache.RABBIT_PORT = 5672;
		SmsCache.RABBIT_USERNAME = "sioomq";
		SmsCache.RABBIT_PASSWORD = "sioo58657686";
		SmsCache.RABBIT_PREFETCH_SIZE = 1;
		RabbitMQProducerUtil util = RabbitMQProducerUtil.getProducerInstance();
		
		String sms = "{\"arrive_fail\":0,\"autoFlag\":0,\"channel\":15,\"content\":\"OA办公系统提醒您：您有一份公文需要处理,标识H_E_Y时间2017-03-14 15:30:00来自3号【阳光互动】\",\"contentNum\":0,\"expid\":\"7996100\",\"fail\":0,\"grade\":0,\"handStat\":0,\"hisids\":0,\"id\":0,\"mobile\":18709869860,\"mtype\":0,\"pid\":91633388,\"senddate\":20170314153008,\"source\":\"CMPP1\",\"stat\":0,\"succ\":0,\"uid\":799}";
		SendingVo vo = JSON.parseObject(sms, SendingVo.class);
		vo.setMtype(0);
		log.info(sms);
		log.info(vo.getMobile());
		try {
		util.send("SUBMIT_QUEUE_7", vo);
		} catch (Exception e) {
			log.info("error: " + e);
			log.info(sms);
		}
//		File file = new File("C:\\Users\\Administrator\\Desktop\\error.log");
//		if (file.isFile() && file.exists()) { // 判断文件是否存在
//			int i = 0;
//			try {
//				List<Integer> list = new ArrayList<Integer>();
//				InputStreamReader read = new InputStreamReader(new FileInputStream(file), "utf-8");// 考虑到编码格式
//				BufferedReader bufferedReader = new BufferedReader(read);
//				String sms = null;
//				while ((sms = bufferedReader.readLine()) != null) {
//					if (sms == null || sms.isEmpty() || sms.indexOf(" - {") == -1 || !sms.endsWith("}")) {
//						continue;
//					}
//					//if(sms.indexOf("验证码")!=-1){
//						
//					//}
//					
//						
//					sms = sms.substring(sms.indexOf(" - {")+2);
//					
//					SendingVo vo = JSON.parseObject(sms, SendingVo.class);
//					if(sms.indexOf("验证码")!=-1){
//						i++;
//						//log.info(sms);
//						vo.setMtype(0);
//						vo.setChannel(21);
//						//log.info(vo.getMobile());
//					}
//					 
//					 try {
//						 
//						 if(sms.indexOf("验证码")!=-1 && i>2){
//							 log.info(sms);
//							 log.info(vo.getMobile());
//							 util.send("SUBMIT_QUEUE_7", vo);
//						 }
//						 
//						 
//					 } catch (Exception e) {
//					 log.info("error: " + e);
//					 log.info(sms);
//					 }
//				}
//				log.info("count:"+i);
//				read.close();
//			} catch (UnsupportedEncodingException e) {
//				log.info("读取用户消费短信记录文件时，编码方式设置错误。" + "[FileUtils.readSmsCancheTxtFile() UnsupportedEncodingException]" + LogInfo.getTrace(e));
//			} catch (FileNotFoundException e) {
//				log.info("读取用户消费短信记录文件时，文件不存在。" + "[FileUtils.readSmsCancheTxtFile() FileNotFoundException]" + LogInfo.getTrace(e));
//			} catch (IOException e) {
//				log.info("读取用户消费短信记录文件时，IO异常。" + "[FileUtils.readSmsCancheTxtFile() IOException]" + LogInfo.getTrace(e));
//			}
//		}
	}

	
	private static void saveMongodb() {
		SmsCache.mongoHost = "101.227.68.21 ";
		SmsCache.mongoPort = 27017;
		SmsCache.mongoDbName = "SMS";
		MongoManager mongo = MongoManager.getInstance();
		File file = new File("C:\\data.txt");
		if (file.isFile() && file.exists()) { // 判断文件是否存在
			try {
				InputStreamReader read = new InputStreamReader(new FileInputStream(file), "utf-8");// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String sms = null;
				List<DBObject> historyList= new ArrayList<DBObject>();
				List<DBObject> dbObjRpt = new ArrayList<DBObject>(); 
				while ((sms = bufferedReader.readLine()) != null) {
					if (sms == null || sms.isEmpty() || !sms.endsWith("NullPointerException")) {
						continue;
					}
					sms = sms.substring(sms.indexOf("data: {")+6,(sms.length()-"java.lang.NullPointerException".length()));
					log.info(sms);
					SendingVo vo = JSON.parseObject(sms, SendingVo.class);
					if (vo.getLocation() != null && vo.getLocation().indexOf(",") != -1) {
						vo.setLocation(vo.getLocation().split(",")[0]);
					}
					vo.setContentNum(calcContentNum(vo.getContent()));
					vo.setSucc(vo.getContentNum());
					vo.setFail(0);
					vo.setArrive_fail(vo.getContentNum());
					vo.setStat(1);
					vo.setRptStat("UNDELIV");
					historyList.add(convertBean(vo));
					
					// 长短信时添加两条
					for (int i = 0; i < vo.getContentNum(); i++) {
						dbObjRpt.add(convertRpt(vo));
					}
				}
				
				log.info(historyList.size());
				mongo.batchInsert("sms_send_history_unknown", historyList);
				log.info(dbObjRpt.size());
				mongo.batchInsert("sms_report", dbObjRpt);
			} catch (UnsupportedEncodingException e) {
				log.info("读取用户消费短信记录文件时，编码方式设置错误。" + "[FileUtils.readSmsCancheTxtFile() UnsupportedEncodingException]" + LogInfo.getTrace(e));
			} catch (FileNotFoundException e) {
				log.info("读取用户消费短信记录文件时，文件不存在。" + "[FileUtils.readSmsCancheTxtFile() FileNotFoundException]" + LogInfo.getTrace(e));
			} catch (IOException e) {
				log.info("读取用户消费短信记录文件时，IO异常。" + "[FileUtils.readSmsCancheTxtFile() IOException]" + LogInfo.getTrace(e));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally{
				try{
					mongo.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		}
	}
	
	private static void updateMongoHistory() throws Exception {
		SmsCache.mongoHost = "101.227.68.21 ";
		SmsCache.mongoPort = 27017;
		SmsCache.mongoDbName = "SMS";
		MongoManager mongo = MongoManager.getInstance();
		DBObject where = new BasicDBObject();
		where.put("e", 80157);
		where.put("d", 20170309140846L);
		where.put("g", 25);
		where.put("s", 0);
		List<DBObject> list = mongo.find("sms_send_history_unknown", where);
		
		if(list!=null && list.size()>0){
			for(DBObject o:list){
				if(o.get("h").toString().indexOf("回T退订")==-1){
					DBObject set = new BasicDBObject();
					set.put("h", o.get("h").toString()+"回T退订");
					
					DBObject updateWhere = new BasicDBObject();
					updateWhere.put("a", Long.parseLong(o.get("a").toString()));
					System.out.println(updateWhere.get("a"));
					
					mongo.update2("sms_send_history_unknown", set, updateWhere);
				}
			}
		}
		System.out.println(list.size());
	}
	
	private static void saveRPTtoClient() throws Exception {
		SmsCache.mongoHost = "101.227.68.21 ";
		SmsCache.mongoPort = 27017;
		SmsCache.mongoDbName = "SMS";
		MongoManager mongo = MongoManager.getInstance();
		
		SmsCache.RABBIT_HOST = "101.227.68.21";
		SmsCache.RABBIT_PORT = 5672;
		SmsCache.RABBIT_USERNAME = "sioomq";
		SmsCache.RABBIT_PASSWORD = "sioo58657686";
		SmsCache.RABBIT_PREFETCH_SIZE = 1;
		RabbitMQProducerUtil util = RabbitMQProducerUtil.getProducerInstance();
		
		DBObject where = new BasicDBObject();
		where.put("a", 81013);
		where.put("c", "XA:0001");
		where.put("g", 20170119101537L);
		
		List<DBObject> list = mongo.find("sms_report", where);
		if(list!=null && list.size()>0){
			for(DBObject o:list){
				System.out.println(o.get("b").toString());
				DeliverVo deliver = new DeliverVo();
      			deliver.setHisId(Integer.parseInt(o.get("d").toString()));
      			deliver.setMobile(o.get("b").toString());
      			deliver.setChannel(Integer.parseInt(o.get("h").toString()));
      			deliver.setUid(Integer.parseInt(o.get("a").toString()));
      			deliver.setPid(Integer.parseInt(o.get("e").toString()));
      			deliver.setRpt_code(o.get("c").toString());
      			deliver.setRpt_time(o.get("g").toString());
      			util.send("DELIVER_TO_CLIENT", deliver);
			}
		}
	}
	
	private static int calcContentNum(String content) {
		// 重新计算条数
		int contentLength = content.length();
		int cCount = contentLength > 70 ? 67 : 70;
		if (contentLength % cCount != 0) {
			cCount = (contentLength / cCount) + 1;
		} else {
			cCount = contentLength / cCount;
		}
		return cCount;
	}
	private static DBObject convertBean(SendingVo vo) {
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
	}
	
	private static DBObject convertRpt(SendingVo vo) {
		try {
			DBObject obj = new BasicDBObject();
			obj.put("a", vo.getUid()); // uid
			obj.put("b", vo.getMobile()); // mobile
			obj.put("c", vo.getRptStat()); // rpt_code
			obj.put("d", vo.getId()); // hisId
			obj.put("e", vo.getPid());// pid
			obj.put("f", 0);// status
			obj.put("g", DateUtils.getTime());// rpt_time
			obj.put("h", vo.getChannel());// channel
			return obj;
		} catch (Exception e) {
			log.error("[SmsReportSave.convertRpt Exception];Msg:" + e.getMessage(), e);
		}
		return null;
	}
}
