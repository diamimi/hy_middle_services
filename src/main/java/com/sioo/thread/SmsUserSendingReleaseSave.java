package com.sioo.thread;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.sioo.dao.SmsUserSendingReleaseDao;
import com.sioo.hy.cmpp.vo.SendingVo;
import com.sioo.log.LogInfo;
import com.sioo.util.SmsCache;

/**
 * 插入审核表线程
 * 
 * @author CQL 331737188@qq.com
 * @date : 2016年9月23日 下午4:00:19
 *
 */
public class SmsUserSendingReleaseSave implements Runnable {

	public void run() {
		List<SendingVo> list = null;
		int size = 0;
		while (true) {
			try {
				// 如果为空，休眠1秒；否则休眠100毫秒
				if (SmsCache.QUEUE_SENDING_RELEASE.isEmpty()) {
					Thread.sleep(1000);
					continue;
				} else {
					Thread.sleep(100);
				}

				list = new ArrayList<SendingVo>();
				size = SmsCache.QUEUE_SENDING_RELEASE.drainTo(list, 2000);
				SmsCache.QUEUE_SENDING_RELEASE_FLG = false;
				if (size > 0) {
					SmsUserSendingReleaseDao.getInstance().saveSmsUserSendingReleaseListByMySql(list);
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
				LogInfo.getLog().errorAlert("保存审核信息异常", "[SmsUserSendingReleaseSave.run() Exception, save to mysql sms_user_sending_release failed]" + LogInfo.getTrace(e));
			} finally {
				SmsCache.QUEUE_SENDING_RELEASE_FLG = true;
				// 释放内存
				if (list != null) {
					list.clear();
					list = null;
				}
			}
		}
	}

}
