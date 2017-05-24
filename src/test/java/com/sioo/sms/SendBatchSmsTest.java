package com.sioo.sms;

import com.alibaba.fastjson.JSON;
import com.sioo.hy.cmpp.vo.SendingVo;
import com.sioo.util.RabbitMQProducerUtil;
import com.sioo.util.SmsCache;

public class SendBatchSmsTest {

	public static void main(String[] args) throws Exception {
//		SmsCache.RABBIT_HOST = "210.5.158.31";
//		SmsCache.RABBIT_PORT = 5672;
//		SmsCache.RABBIT_USERNAME = "sioo";
//		SmsCache.RABBIT_PASSWORD = "sioo58657686";
//		SmsCache.RABBIT_PREFETCH_SIZE = 1;
//		RabbitMQProducerUtil util = RabbitMQProducerUtil.getProducerInstance();
//
//		String str = "{\"arrive_fail\":0,\"autoFlag\":0,\"channel\":30,\"content\":\"【希奥yy】欢迎您，您的上网验证码为：362283。\",\"contentNum\":0,\"expid\":\"12354\",\"fail\":0,\"grade\":0,\"handStat\":0,\"hisids\":0,\"id\":0,\"mobile\":13721009953,\"mtype\":1,\"pid\":12090893,\"senddate\":20161213130625,\"source\":\"CMPP1\",\"stat\":0,\"succ\":0,\"uid\":30032}";
//		SendingVo vo = JSON.parseObject(str, SendingVo.class);
//		new Thread(new Send(util, vo, "1")).start();
		// new Thread(new Send(util, vo, "3")).start();
		// new Thread(new Send(util, vo, "4")).start();
		// new Thread(new Send(util, vo, "7")).start();
	}
}

class Send implements Runnable {
	public Send(RabbitMQProducerUtil util, SendingVo vo, String type) {
		this.util = util;
		this.vo = vo;
		this.type = type;
	}

	private RabbitMQProducerUtil util;
	private SendingVo vo;
	private String type;

	@Override
	public void run() {
		for (int i = 0; i < 1; i++) {
			try {
				util.send("SUBMIT_QUEUE_" + type, vo);
				System.out.println("SUBMIT_QUEUE_" + type + ": " + i);
				Thread.sleep(100);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}
