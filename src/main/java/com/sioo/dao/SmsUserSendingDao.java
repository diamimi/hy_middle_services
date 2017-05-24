package com.sioo.dao;

import com.alibaba.fastjson.JSON;
import com.sioo.db.mybatis.SessionFactory;
import com.sioo.db.mybatis.mapper.SmsUserSendingMapper;
import com.sioo.hy.cmpp.vo.SendingVo;
import com.sioo.log.LogInfo;
import org.apache.ibatis.session.SqlSession;

import java.util.ArrayList;
import java.util.List;

/***
 * 待发信息Dao类
 * 
 * @author OYJM
 * @date 2016年9月20日
 *
 */
public class SmsUserSendingDao {
	private static SmsUserSendingDao smsUserSendingDao;

	public static SmsUserSendingDao getInstance() {
		if (smsUserSendingDao != null) {
			return smsUserSendingDao;
		}
		synchronized (SmsUserSendingDao.class) {
			if (smsUserSendingDao == null) {
				smsUserSendingDao = new SmsUserSendingDao();
			}
		}
		return smsUserSendingDao;
	}

	/***
	 * 批量保存队列信息
	 * 
	 * @param vo
	 * @throws Exception
	 */
	public void saveSmsUserSendingListByMySql(List<SendingVo> vo) throws Exception {
		SqlSession session = null;
		try {
			session = SessionFactory.getSessionFactory().openSession();
			SmsUserSendingMapper mapper = session.getMapper(SmsUserSendingMapper.class);
			mapper.batchSave(vo);
		} catch (Exception ex) {
			LogInfo.getLog().errorAlert("批量保存通道队列信息异常", "[SmsUserSendingDao.saveSmsUserSendingListByMySql() Exception]; data:" + JSON.toJSONString(vo) + LogInfo.getTrace(ex));
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}

    public List<SendingVo> getSmsUserSending() {
		SqlSession session = null;
		try {
			session = SessionFactory.getSessionFactory().openSession();
			SmsUserSendingMapper mapper = session.getMapper(SmsUserSendingMapper.class);
			List<SendingVo> list = mapper.getSmsUserSending();
			// 删除消息
			List<Long> ids = new ArrayList<>();
			for (SendingVo vo : list) {
				ids.add(vo.getId());
				if (ids.size() > 999 || ids.size() == list.size()) {
					mapper.deleteByIds(ids);
					ids.clear();
				}
			}
			return list;
		} catch (Exception ex) {
			LogInfo.getLog().errorAlert("获取队列管理信息异常", "[SmsUserSendingDao.getSmsUserSending() Exception]" + LogInfo.getTrace(ex));
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return null;
    }
}
