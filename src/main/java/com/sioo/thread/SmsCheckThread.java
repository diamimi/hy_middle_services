package com.sioo.thread;

import com.alibaba.fastjson.JSON;
import com.sioo.hy.cmpp.vo.SendingVo;
import com.sioo.log.LogInfo;
import com.sioo.service.SmsService;
import com.sioo.util.RabbitMQProducerUtil;
import com.sioo.util.SmsCache;

import java.util.ArrayList;
import java.util.List;

/***
 * 校验短信线程
 * 
 * @author OYJM
 * @date 2016年9月28日
 *
 */
public class SmsCheckThread implements Runnable {
	private int type = 0; // 类型，0普通队列，1优先队列
	private RabbitMQProducerUtil util = null; // rabbitmq操作类

	public SmsCheckThread(int type, RabbitMQProducerUtil util) {
		this.type = type;
		this.util = util;
	}

	private SmsService smsService = SmsService.getInstance();

	public void run() {
		List<SendingVo> list = null;
		int size = 0;
		while (true) {
			try {
				// 如果为空，休眠1秒；否则休眠50毫秒
				if (SmsCache.QUEUE_SENDING_PRIORITIZED.isEmpty() && SmsCache.QUEUE_SMSCHECK_NORMAL.isEmpty()) {
					Thread.sleep(1000);
					continue;
				} else {
					Thread.sleep(50);
				}

				list = new ArrayList<SendingVo>();
				if (type == 1) {
					// 优先类型从优先本地队列中获取
					size = SmsCache.QUEUE_SENDING_PRIORITIZED.drainTo(list, 2000);
				} else {
					// 普通类型从普通本地队列中获取
					size = SmsCache.QUEUE_SMSCHECK_NORMAL.drainTo(list, 2000);
				}

				if (size > 0) {
					for (SendingVo vo : list) {
						// 处理消息
						try {
							smsService.smsHandle(type, vo, util);
						} catch (Exception e) {
							LogInfo.getLog().errorAlert("处理短信异常",
									"[SmsCheckThread.run() smsService.smsHandle Exception]; \r\n data: " + JSON.toJSONString(vo) + "/r/n" + LogInfo.getTrace(e));
						}
					}
				}
			} catch (Exception e) {
				LogInfo.getLog().errorAlert("从本地队列中获取处理短信异常", "[SmsCheckThread.run() Exception]" + LogInfo.getTrace(e));
			} finally {
				// 释放缓存
				if (list != null) {
					list.clear();
					list = null;
				}
			}

		}
	}
}
