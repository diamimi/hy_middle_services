package com.sioo.thread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.sioo.dao.SmsUserDao;
import com.sioo.hy.cmpp.vo.ConsumeVo;
import com.sioo.hy.cmpp.vo.SendingVo;
import com.sioo.log.LogInfo;
import com.sioo.util.DateUtils;
import com.sioo.util.SmsCache;

/***
 * 保存发送记录到mongoDB
 * 
 * @author OYJM
 * @date 2016年10月12日
 *
 */
public class SmsUnKouSave implements Runnable {
	private static Logger log = Logger.getLogger(SmsUnKouSave.class);
	
	public void run() {
		List<SendingVo> list = null;
		int size = 0;
		while (true) {
			try {
				// 如果为空，休眠1秒；否则休眠100毫秒
				if (SmsCache.QUEUE_UNKOU_SMS.isEmpty()) {
					Thread.sleep(1000*30);
					continue;
				} else {
					Thread.sleep(100);
				}

				list = new ArrayList<SendingVo>();
			
				size = SmsCache.QUEUE_UNKOU_SMS.drainTo(list, 2000);
				if (size > 0) {
					long dayStart = DateUtils.getDayStart();
					Long day = null;
					Long lastDay = null;
					Map<Integer, Integer> mapUser = new HashMap<Integer, Integer>();
					Map<Integer, Integer> lastMapUser = new HashMap<Integer, Integer>();
					for(SendingVo vo:list){
						if(vo.getSenddate()>dayStart){
							if(day ==null){
								day = Long.parseLong((vo.getSenddate()+"").substring(0, 8));
							}
							int sms = mapUser.get(vo.getUid()) == null ?0:mapUser.get(vo.getUid());
							sms+=vo.getContentNum();
							mapUser.put(vo.getUid(), sms);
						}else{
							if(lastDay ==null){
								lastDay = Long.parseLong((vo.getSenddate()+"").substring(0, 8));
							}
							int sms = lastMapUser.get(vo.getUid()) == null ?0:lastMapUser.get(vo.getUid());
							sms+=vo.getContentNum();
							lastMapUser.put(vo.getUid(), sms);
						}
					}
					
					if(mapUser != null && mapUser.size()>0){
						List<ConsumeVo> consumeList = new ArrayList<ConsumeVo>();
						ConsumeVo consumeVo = null;
						for (Integer uid : mapUser.keySet()) {
							consumeVo = new ConsumeVo();
							consumeVo.setUid(uid);
							consumeVo.setDate(day);
							consumeVo.setUnkousms(mapUser.get(uid));
							consumeList.add(consumeVo);
							log.info("save unkou SmsConsume,uid:"+uid+",date:"+day+",unkousms:"+mapUser.get(uid));
						}
						SmsUserDao.getInstance().saveSmsUserConsume(consumeList);
						SmsUserDao.getInstance().updateSmsUserConsumeUnKou(consumeList, day);
					}
					
					if(lastMapUser != null && lastMapUser.size()>0){
						List<ConsumeVo> consumeList = new ArrayList<ConsumeVo>();
						ConsumeVo consumeVo = null;
						for (Integer uid : lastMapUser.keySet()) {
							consumeVo = new ConsumeVo();
							consumeVo.setUid(uid);
							consumeVo.setDate(lastDay);
							consumeVo.setUnkousms(lastMapUser.get(uid));
							consumeList.add(consumeVo);
							log.info("save unkou SmsConsume,uid:"+uid+",date:"+lastDay+",unkousms:"+lastMapUser.get(uid));
						}
						SmsUserDao.getInstance().saveSmsUserConsume(consumeList);
						SmsUserDao.getInstance().updateSmsUserConsumeUnKou(consumeList, lastDay);
					}
				}
				
				Thread.sleep(1000*60*15);
			} catch (Exception e) {
				// 异常消息打印
				if (list != null && !list.isEmpty()) {
					StringBuffer stb = new StringBuffer();
					for (SendingVo vo : list) {
						stb.append(JSON.toJSONString(vo)).append("\r\n");
					}
					LogInfo.getLog().errorData(stb.toString());
				}
				LogInfo.getLog().errorAlert("保存未扣费消费记录异常", "[SmsUnKouSave.run() Exception, save to mysql sms_user_consume failed]" + LogInfo.getTrace(e));
			} finally {
				// 释放内存
				if (list != null) {
					list.clear();
					list = null;
				}
			}
		}
	}
}
