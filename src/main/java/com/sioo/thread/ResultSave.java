package com.sioo.thread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sioo.cache.LocalQueueCache;
import com.sioo.db.mongo.MongoManager;
import com.sioo.hy.cmpp.vo.ResultVo;
import com.sioo.log.LogInfo;
import com.sioo.util.SmsCache;

/***
 * 状态报告mongodb操作类
 * 
 * 插入到状态报告中 修改历史理论状态
 * 
 * @author OYJM
 * @date 2016年9月29日
 *
 */
public class ResultSave implements Runnable {
	private MongoManager mongo = null;

	public ResultSave(MongoManager mongo) {
		this.mongo = mongo;
	}

	public void run() {
		LocalQueueCache localQueueCache = LocalQueueCache.getInstance();
		List<ResultVo> list = null;
		Map<String, ResultVo> map = null;
		List<String> keyList = null;
		int size = 0;
		while (true) {
			try {
				// 如果为空，休眠1秒；否则休眠100毫秒
				if (SmsCache.QUEUE_RESULT.isEmpty()) {
					Thread.sleep(1000);
					continue;
				} else {
					Thread.sleep(100);
				}

				// 从队列中获取数据
				list = new ArrayList<ResultVo>();
				SmsCache.QUEUE_RESULT_FLG = false;
				size = SmsCache.QUEUE_RESULT.drainTo(list, 2000);
				if (size > 0) {
					map = new HashMap<String, ResultVo>();
					keyList = new ArrayList<String>();
					String key = null;
					ResultVo temp = null;
					// 先统计用户的发送信息
					for (ResultVo vo : list) {
						key = vo.getSenddate() + "" + vo.getUid();
						keyList.add(key);
						if (map.containsKey(key)) {
							temp = map.get(key);
							temp.setSubmitTotal(temp.getSubmitTotal() + vo.getSubmitTotal());
							temp.setSubmitSuccess(temp.getSubmitSuccess() + vo.getSubmitSuccess());
							temp.setSubmitFail(temp.getSubmitFail() + vo.getSubmitFail());
							map.put(key, temp);
						} else {
							map.put(key, vo);
						}
					}

					DBObject whereObj = null;
					DBObject incObj = null;
					// 将reids中的发送信息自增
					for (String id : map.keySet()) {
						temp = map.get(id);
						whereObj = new BasicDBObject();
						whereObj.put("a", temp.getSenddate()); // time
						whereObj.put("b", temp.getUid()); // uid

						incObj = new BasicDBObject();
						incObj.put("c", temp.getSubmitTotal());// total
						incObj.put("d", temp.getSubmitFail());// fail

						mongo.updateByInc("sms_user_day_count", incObj, whereObj);
						// 处理完删除键值
						keyList.remove(id);
					}
				}
			} catch (Exception e) {
				LogInfo.getLog().errorAlert("保存用户日报表异常", "[ResultSave.run() Exception, update to mongo sms_user_day_count failed]" + LogInfo.getTrace(e));
				if ((list != null && !list.isEmpty()) && (map != null && !map.isEmpty())) {
					if (keyList == null || keyList.isEmpty()) {
						for (String id : map.keySet()) {
							localQueueCache.putResult(map.get(id));
						}
					} else {
						for (String id : map.keySet()) {
							for (String key : keyList) {
								if (id.equals(key)) {
									localQueueCache.putResult(map.get(id));
								}
							}
						}
					}
				}
			} finally {
				SmsCache.QUEUE_RESULT_FLG = true;
				// 释放内存
				if (list != null) {
					list.clear();
					list = null;
				}
				if (map != null) {
					map.clear();
					map = null;
				}
				if (keyList != null) {
					keyList.clear();
					keyList = null;
				}
			}
		}
	}
}
