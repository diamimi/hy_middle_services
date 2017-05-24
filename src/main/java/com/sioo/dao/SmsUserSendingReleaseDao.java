package com.sioo.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.alibaba.fastjson.JSON;
import com.sioo.db.mybatis.SessionFactory;
import com.sioo.db.mybatis.mapper.SmsUserSendingReleaseMapper;
import com.sioo.hy.cmpp.vo.SendingVo;
import com.sioo.log.LogInfo;

/***
 * 审核信息Dao类
 * 
 * @author OYJM
 * @date 2016年9月20日
 *
 */
public class SmsUserSendingReleaseDao {
	private static SmsUserSendingReleaseDao smsUserSendingReleaseDao;

	public static SmsUserSendingReleaseDao getInstance() {
		if (smsUserSendingReleaseDao != null) {
			return smsUserSendingReleaseDao;
		}
		synchronized (SmsUserSendingReleaseDao.class) {
			if (smsUserSendingReleaseDao == null) {
				smsUserSendingReleaseDao = new SmsUserSendingReleaseDao();
			}
		}
		return smsUserSendingReleaseDao;
	}

	/***
	 * 批量保存审核信息
	 * 
	 * @param vo
	 * @throws Exception
	 */
	public void saveSmsUserSendingReleaseListByMySql(List<SendingVo> vo) throws Exception {
		SqlSession session = null;
		try {
			session = SessionFactory.getSessionFactory().openSession();
			SmsUserSendingReleaseMapper mapper = session.getMapper(SmsUserSendingReleaseMapper.class);
			mapper.batchSave(vo);
		} catch (Exception ex) {
			LogInfo.getLog().errorAlert("批量保存审核信息异常",
					"[SmsUserSendingReleaseDao.saveSmsUserSendingReleaseListByMySql() Exception]; data:" + JSON.toJSONString(vo) + LogInfo.getTrace(ex));
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}

	/***
	 * 获取已审核信息列表，并删除获取对象
	 * 
	 * @return
	 */
	public List<SendingVo> getSmsUserSendingRelease() {
		SqlSession session = null;
		try {
			session = SessionFactory.getSessionFactory().openSession();
			SmsUserSendingReleaseMapper mapper = session.getMapper(SmsUserSendingReleaseMapper.class);
			List<SendingVo> list = mapper.getSmsUserSendingRelease();
			// 删除消息
			List<Long> ids = new ArrayList<Long>();
			for (SendingVo vo : list) {
				ids.add(vo.getId());
				if (ids.size() > 999 || ids.size() == list.size()) {
					mapper.deleteByIds(ids);
					ids.clear();
				}
			}
			return list;
		} catch (Exception ex) {
			LogInfo.getLog().errorAlert("获取审核信息异常", "[SmsUserSendingReleaseDao.getSmsUserSendingRelease() Exception]" + LogInfo.getTrace(ex));
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return null;
	}
}
