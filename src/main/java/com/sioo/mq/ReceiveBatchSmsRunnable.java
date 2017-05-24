package com.sioo.mq;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.sioo.hy.cmpp.vo.BatchSendingVo;
import com.sioo.log.LogInfo;
import com.sioo.util.RabbitMQProducerUtil;
import com.sioo.util.SmsCache;

/***
 * 接收用户批量消息 消息队列名称BATCH_QUEUE_TEMP
 * 
 * @author OYJM
 * @date 2016年11月3日
 *
 */
public class ReceiveBatchSmsRunnable implements Runnable {
	private static Logger log = Logger.getLogger(ReceiveBatchSmsRunnable.class);

	// rabbitMQUtil类
	private RabbitMQProducerUtil util = null;

	public ReceiveBatchSmsRunnable(String queueName, RabbitMQProducerUtil util) {
		this.queueName = queueName;
		this.util = util;
	}

	// 队列名称
	private String queueName = "";

	public void run() {
		try {
			Channel channel = util.getChannel();
			// 持久化
			channel.queueDeclare(queueName, true, false, false, null);
			// 流量控制
			channel.basicQos(1);
			// 声明消费者
			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(queueName, false, consumer);

			log.info("listener queue[" + queueName + "] start...");

			// 等待队列推送消息
			BatchSendingVo vo = null;
			while (true) {
				if (SmsCache.CONTROL) {
					try {
						SmsCache.QUEUE_SMS_BATCH_FLG = true;
						// 如果本地队列未被消费，先不取消息队列的值
						if (SmsCache.QUEUE_BATCHSMS.size() > 5000) {
							log.info("local queue[BATCH_QUEUE_TEMP] size greater than 5000, sleep。。。");
							Thread.sleep(100);
							continue;
						}

						// 接收推送消息
						QueueingConsumer.Delivery delivery = consumer.nextDelivery();
						// 序列化为对象
						Object obj = util.deSerialize(delivery.getBody());
						if (obj != null) {
							// 回复确认消息
							channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
							// 判断消息类型
							if (obj instanceof BatchSendingVo) {
								vo = (BatchSendingVo) obj;
								SmsCache.QUEUE_BATCHSMS.put(vo);
							} else {
								// 如果消息类型不匹配，将消息记录到日志，并打印log
								LogInfo.getLog().errorData(JSON.toJSONString(obj));
								log.error("receive BatchSendingVo type error, is not BatchSendingVo!");
							}
						} else {
							log.error("recevice BatchSendingVo is empty!");
						}

						SmsCache.QUEUE_SMS_BATCH_FLG = false;
					} catch (Exception e) {
						LogInfo.getLog().errorAlert("接收批处理历史记录信息异常", "[ReceiveBatchSmsRunnable.run() queue[" + queueName + "] receive Exception]" + LogInfo.getTrace(e));
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
			LogInfo.getLog().errorAlert("监听接收批处理历史记录消息队列异常", "[ReceiveBatchSmsRunnable.run() queue[" + queueName + "] listener Exception]" + LogInfo.getTrace(e));
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e1) {
				log.error("sleep error!");
			}
		}

	}
}
