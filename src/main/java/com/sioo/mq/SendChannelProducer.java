package com.sioo.mq;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.sioo.util.Md5Util;
import com.sioo.util.RabbitMQProducerUtil2;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.sioo.cache.RptRatioConfigCache;
import com.sioo.hy.cmpp.vo.SendingVo;
import com.sioo.log.LogInfo;
import com.sioo.util.RabbitMQProducerUtil;
import com.sioo.util.SmsCache;

/****
 * 发送到通道队列
 * 
 * @author OYJM
 * @date 2016年9月21日
 *
 */
public class SendChannelProducer {
	private static Logger log = Logger.getLogger(SendChannelProducer.class);
	
	public static void send(SendingVo vo, RabbitMQProducerUtil util, Integer userkind) {
		// 发送到通道消息队列中
		try {
			if(doRptRatio(vo,util)){
				int priority = convertPriority(userkind);
				util.send("SEND_QUEUE_" + vo.getChannel(), vo, priority);
				log.info(vo.getMobile()+" to SEND_QUEUE_" + vo.getChannel()+"("+priority+")");
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("发送到通道消息队列异常", "[SendChannelProducer.send(" + JSON.toJSONString(vo) + ",util," + userkind + ") Exception]" + LogInfo.getTrace(e));
			LogInfo.getLog().errorData(JSON.toJSONString(vo));
		}
	}

	/***
	 * 判断是否为扣量用户
	 * @param vo
	 * @return
	 */
	private static boolean doRptRatio(SendingVo vo, RabbitMQProducerUtil util){
		boolean result = true;
		//获取用户扣量配置信息
		Map<String, Object> rptratio = RptRatioConfigCache.getInstance().getRptRatioConfig(vo.getUid());
		if(rptratio != null && vo.getGrade() != 1){
			long size = 1;
			String md5 = Md5Util.getMD5(vo.getContent());
			Map<String, AtomicLong> subMap = null;
			//先累加并获取用户当天发送了多少条数据
			if(SmsCache.RPT_RATIO_USER_SEND!=null && SmsCache.RPT_RATIO_USER_SEND.containsKey(vo.getUid())){
				subMap = SmsCache.RPT_RATIO_USER_SEND.get(vo.getUid());
				if(subMap != null && subMap.containsKey(md5)){
					size = SmsCache.RPT_RATIO_USER_SEND.get(vo.getUid()).get(md5).incrementAndGet();
				}else{
					subMap.put(md5, new AtomicLong(1));
					SmsCache.RPT_RATIO_USER_SEND.put(vo.getUid(), subMap);
				}
			}else{
				subMap = new ConcurrentHashMap<String, AtomicLong>();
				subMap.put(md5, new AtomicLong(1));
				SmsCache.RPT_RATIO_USER_SEND.put(vo.getUid(), subMap);
			}
			//如果用户发送的条数大于设置的初始值开始扣量
			if(Long.parseLong(rptratio.get("defalt").toString())<size){
				//获取用户设置的扣量比例
				int rate = Integer.parseInt(rptratio.get("rate").toString());
				//获取一个随机值 0-100
				Random r = new Random();
				int num = r.nextInt(101);
				log.info("sms_rpt_ratio_config uid:"+vo.getUid()+" ,mobile:"+vo.getMobile()+" ,defalt:"+rptratio.get("defalt")+" ,rate:"+rptratio.get("rate")+" ,sendsize:"+size+" ,random:"+num);
				//判断随机值是否在扣量比例中
				if(num<rate){
					vo.setGrade(9);
					result = false;
				}else{
					vo.setGrade(1);
				}
			}
			try {
				util.send("RATIO_QUEUE", vo);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.error(e.getMessage(), e);
			}
//			SmsCache.QUEUE_RATIO.add(vo);	2017-02-15日11:28 陈泉霖注释，使用Soket会引发其他端口异常，改为放入rabbitMQ处理
		}
		return result;
	}
	/***
	 * 转换优先级别
	 * 
	 * @param num
	 * @return
	 */
	private static int convertPriority(int num) {
		if (num == 0) {
			return 9;
		}
		if (num == 1) {
			return 5;
		}
		if (num == 2) {
			return 3;
		}
		if (num == 3) {
			return 1;
		}
		return 1;
	}
}
