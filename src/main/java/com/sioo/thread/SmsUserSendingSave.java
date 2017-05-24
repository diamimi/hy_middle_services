package com.sioo.thread;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.sioo.dao.SmsUserSendingDao;
import com.sioo.hy.cmpp.vo.SendingVo;
import com.sioo.log.LogInfo;
import com.sioo.util.SmsCache;

/****
 * 将通道不能发送消息放入通道队列
 * 
 * @author OYJM
 * @date 2016年10月19日
 *
 */
public class SmsUserSendingSave implements Runnable {

	public void run() {
		List<SendingVo> list = null;
		int size = 0;
		while (true) {
			try {
				// 如果为空，休眠1秒；否则休眠100毫秒
				if (SmsCache.QUEUE_SENDING.isEmpty()) {
					Thread.sleep(1000);
					continue;
				} else {
					Thread.sleep(100);
				}

				list = new ArrayList<SendingVo>();
				SmsCache.QUEUE_SENDING_FLG = false;
				size = SmsCache.QUEUE_SENDING.drainTo(list, 2000);
				if (size > 0) {
					SmsUserSendingDao.getInstance().saveSmsUserSendingListByMySql(list);
				}
			} catch (Exception e) {
				// 异常消息打印
				if (list != null && !list.isEmpty()) {
					StringBuffer stb = new StringBuffer();
					for (SendingVo vo : list) {
						stb.append(JSON.toJSONString(vo)).append("\r\n");
					}
					LogInfo.getLog().errorData(stb.toString());
				}
				LogInfo.getLog().errorAlert("保存通道队列消息异常", "[SmsUserSendingSave.run() Exception, save to mysql sms_user_sending failed]" + LogInfo.getTrace(e));
			} finally {
				SmsCache.QUEUE_SENDING_FLG = true;
				// 释放内存
				if (list != null) {
					list.clear();
					list = null;
				}
			}
		}
	}
}
