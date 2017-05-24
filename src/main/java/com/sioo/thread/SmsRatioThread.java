package com.sioo.thread;

import org.apache.log4j.Logger;

public class SmsRatioThread implements Runnable {
	private static Logger log = Logger.getLogger(SmsRatioThread.class);
	
	@Override
	public void run() {//2017-02-15日11:28 陈泉霖注释，使用Soket会引发其他端口异常，改为放入rabbitMQ处理
//		SmsRatioClient userNumClient = new SmsRatioClient("127.0.0.1", 20094);
//		
//		List<SendingVo> list = null;
//		int size = 0;
//		while (true) {
//			try {
//				// 如果为空，休眠1秒；否则休眠100毫秒
//				if (SmsCache.QUEUE_RATIO.isEmpty()) {
//					Thread.sleep(1000);
//					continue;
//				} else {
//					Thread.sleep(100);
//				}
//
//				list = new ArrayList<SendingVo>();
//				SmsCache.QUEUE_RATIO_FLG = false;
//				size = SmsCache.QUEUE_RATIO.drainTo(list, 2000);
//				if (size > 0) {
//					for (SendingVo vo : list) {
//						//log.info("1," + vo.getId()+",9,"+new MD5().getMD5ofStr(vo.getContent())+","+vo.getContentNum()+","+vo.getUid()+","+vo.getMobile()+","+vo.getPid()+","+vo.getChannel()+","+vo.getSenddate());
//						userNumClient.submit("1," + vo.getId()+","+vo.getGrade()+","+new MD5().getMD5ofStr(vo.getContent())+","+vo.getContentNum()+","+vo.getUid()+","+vo.getMobile()+","+vo.getPid()+","+vo.getChannel()+","+vo.getSenddate());
//					}
//				}
//			}catch(Exception e){
//				log.error("send to SmsRatioClient error."+ LogInfo.getTrace(e));
//			} finally {
//				SmsCache.QUEUE_RATIO_FLG = true;
//				// 释放内存
//				if (list != null) {
//					list.clear();
//					list = null;
//				}
//			}
//		}

	}

}
