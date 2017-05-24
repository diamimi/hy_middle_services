package com.sioo.mq;

import java.util.Map;

import com.sioo.util.RabbitMQProducerUtil2;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.sioo.cache.LocalQueueCache;
import com.sioo.cache.MobileAreaCache;
import com.sioo.cache.UserCache;
import com.sioo.db.mongo.MongoManager;
import com.sioo.hy.cmpp.vo.SendingVo;
import com.sioo.log.LogInfo;
import com.sioo.service.SmsService;
import com.sioo.util.RabbitMQProducerUtil;
import com.sioo.util.SmsCache;

public class ReceiveSmsChannelCheck implements Runnable {
	private static Logger log = Logger.getLogger(ReceiveSmsChannelCheck.class);
	// rabbitMQUtil类
	private RabbitMQProducerUtil util = null;
	private MongoManager mongo = null;
	public ReceiveSmsChannelCheck(RabbitMQProducerUtil util,MongoManager mongo){
		this.util = util;
		this.mongo = mongo;
	}
	
	private String queueName = "EXAMINE_QUEUE_TEMP";
	
	@Override
	public void run() {
		try {
			Channel channel = util.getChannel();
			// 持久化//SUBMIT_CMPP_PRIORITY
			channel.queueDeclare(queueName, true, false, false, null);
			// 流量控制
			channel.basicQos(1);
			// 声明消费者
			QueueingConsumer consumer = new QueueingConsumer(channel);

			channel.basicConsume(queueName, false, consumer);
			log.info("listener queue[" + queueName + "] start...");

			SmsService smsService = SmsService.getInstance();
			LocalQueueCache localQueueCache = LocalQueueCache.getInstance();
			UserCache userCache = UserCache.getInstance();
			Map<String, Object> user_map = null;
			Map<String, Object> map = null;
			// 等待队列推送消息
			SendingVo vo = null;
			while (true) {
				if (SmsCache.CONTROL) {
					try {
						SmsCache.QUEUE_SMS_CHANNEL_FLG = true;
						
						// 接收推送消息
						QueueingConsumer.Delivery delivery = consumer.nextDelivery();
						// 序列化为对象
						Object obj = util.deSerialize(delivery.getBody());
						if (obj != null) {
							// 回复确认消息
							channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
							// 判断消息类型
							if (obj instanceof SendingVo) {
								vo = (SendingVo) obj;
								//获取用户信息
								user_map = userCache.getUser(vo.getUid());
								//获取手机号码归属地
								map = MobileAreaCache.getInstance().getMobileArea(vo.getMobile());
								if (map != null && !map.isEmpty()) {
									vo.setLocation(map.get("province").toString() + "," + map.get("provincecode")+ "&" + map.get("citycode"));
								} else {
									vo.setLocation("全国,-1");
								}
								
								//通道信息校验
								boolean result = smsService.smsBasicCheck(0, vo, util, mongo, user_map, localQueueCache, null);
								log.info("Resend sms "+(result?"success!":"fail!")+"mobile: "+vo.getMobile());
							} else {
								// 如果消息类型不匹配，将消息记录到日志，并打印log
								LogInfo.getLog().errorData(JSON.toJSONString(obj));
								log.error("recevice check SendingVo type error, is not SendingVo!");
							}
						} else {
							log.error("recevice check SendingVo is empty!");
						}
						SmsCache.QUEUE_SMS_CHANNEL_FLG = false;
					} catch (Exception e) {
						LogInfo.getLog().errorAlert("接收审核短信息异常", "[ReceiveSmsChannelCheck.run() queue[" + queueName + "] receive Exception]" + LogInfo.getTrace(e));
						// 记录数据
						if (vo != null) {
							LogInfo.getLog().errorData(JSON.toJSONString(vo));
						}

						try {
							Thread.sleep(5000);
						} catch (InterruptedException e1) {
							log.error("sleep error!");
						}
					}
				} else {
					Thread.sleep(2000);
				}
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("监听接收审核短信息消息队列异常", "[ReceiveSmsChannelCheck.run() queue[" + queueName + "] listener Exception]" + LogInfo.getTrace(e));
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e1) {
				log.error("sleep error!");
			}
		}

	}

}
