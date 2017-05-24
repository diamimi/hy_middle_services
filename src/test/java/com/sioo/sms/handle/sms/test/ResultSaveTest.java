package com.sioo.sms.handle.sms.test;

import java.io.IOException;

import com.sioo.db.mongo.MongoManager;
import com.sioo.hy.cmpp.vo.ResultVo;
import com.sioo.thread.ResultSave;
import com.sioo.util.SmsCache;

public class ResultSaveTest {
	public static void main(String[] args) throws IOException, InterruptedException {
		SmsCache.mongoHost = "119.15.137.35 ";
		SmsCache.mongoPort = 27017;
		SmsCache.mongoDbName = "SMS";
		ResultVo resultVo = new ResultVo();
		resultVo.setUid(30032);
		resultVo.setSenddate(20161108);
		resultVo.setSubmitTotal(1);
		resultVo.setSubmitFail(0);
		SmsCache.QUEUE_RESULT.put(resultVo);

		resultVo = new ResultVo();
		resultVo.setUid(30022);
		resultVo.setSenddate(20161108);
		resultVo.setSubmitTotal(1);
		resultVo.setSubmitFail(0);
		SmsCache.QUEUE_RESULT.put(resultVo);
		// 声明mongoDB、RabbitMQ操作类对象
		MongoManager mongo = MongoManager.getInstance();

		new Thread(new ResultSave(mongo)).start(); // 用户发送统计入mongo库
	}
}
