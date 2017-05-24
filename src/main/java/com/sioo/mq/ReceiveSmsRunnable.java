package com.sioo.mq;

import com.alibaba.fastjson.JSONObject;
import com.sioo.util.*;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.sioo.hy.cmpp.vo.SendingVo;
import com.sioo.log.LogInfo;

/***
 * 接收用户消息
 *
 * @author OYJM
 * @date 2016年9月21日
 *
 */
public class ReceiveSmsRunnable implements Runnable {
    private static Logger log = Logger.getLogger(ReceiveSmsRunnable.class);
    private static Logger testLog = Logger.getLogger("testData");

    // rabbitMQUtil类
    private RabbitMQProducerUtil util = null;

    public ReceiveSmsRunnable(String queueName, RabbitMQProducerUtil util) {
        SmsCache.QUEUE_FIRST_FLG.put(queueName, true);
        this.queueName = queueName;
        this.util = util;
    }

    // 队列名称
    private String queueName = "";

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

            // 等待队列推送消息
            SendingVo vo = null;
            while (true) {
                if (SmsCache.CONTROL) {

                    SmsCache.QUEUE_FIRST_FLG.put(queueName, true);
                    try {
                        // 如果本地队列未被消费，先不取消息队列的值
                        if (queueName.equals("SUBMIT_CMPP_PRIORITY") || queueName.equals("SUBMIT_HTTP_PRIORITY")) {
                            if (SmsCache.QUEUE_SENDING_PRIORITIZED.size() > 10000) {
                                log.info("local queue[QUEUE_SENDING_PRIORITIZED] size greater than 10000, sleep。。。");
                                Thread.sleep(100);
                                continue;
                            }
                        } else {
                            if (SmsCache.QUEUE_SMSCHECK_NORMAL.size() > 10000) {
                                log.info("local queue[QUEUE_SMSCHECK_NORMAL] size greater than 10000, sleep。。。");
                                Thread.sleep(100);
                                continue;
                            }
                        }

                        // 接收推送消息
                        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                        // 序列化为对象
                        Object obj = util.deSerialize(delivery.getBody());
                        if (obj != null) {
                            testLog.debug(JSON.toJSONString(obj));
                            // 回复确认消息
                            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                            // 判断消息类型
                            if (obj instanceof SendingVo) {
                                vo = (SendingVo) obj;
                                // 如果是优先队列，放入到本地优先队列中处理
                                if (SmsCache.USER_LINE.get(vo.getUid()) != null && SmsCache.USER_LINE.get(vo.getUid()) == 1) {
                                    //把消息发送到另一条线路
                                    RabbitMQProducerUtil2.getProducerInstance().send(queueName, vo);
                                } else {
                                    if (queueName.equals("SUBMIT_CMPP_PRIORITY") || queueName.equals("SUBMIT_HTTP_PRIORITY")) {
                                        SmsCache.QUEUE_SENDING_PRIORITIZED.put(vo);
                                    } else {
                                        SmsCache.QUEUE_SMSCHECK_NORMAL.put(vo);
                                    }
                                }
                            } else {
                                // 如果消息类型不匹配，将消息记录到日志，并打印log
                                LogInfo.getLog().errorData(JSON.toJSONString(obj));
                                log.error("recevice SendingVo type error, is not SendingVo!");
                            }
                        } else {
                            log.error("recevice SendingVo is empty!");
                        }
                    } catch (Exception e) {
                        LogInfo.getLog().errorAlert("接收短信息异常", "[ReceiveSmsRunnable.run() queue[" + queueName + "] receive Exception]" + LogInfo.getTrace(e));
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
                    SmsCache.QUEUE_FIRST_FLG.put(queueName, false);
                } else {
                    Thread.sleep(2000);
                }
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("监听接收短信息消息队列异常", "[ReceiveSmsRunnable.run() queue[" + queueName + "] listener Exception]" + LogInfo.getTrace(e));
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e1) {
                log.error("sleep error!");
            }
        }
    }
}
