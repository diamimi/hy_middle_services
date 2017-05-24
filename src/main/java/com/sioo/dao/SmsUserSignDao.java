package com.sioo.dao;

import com.alibaba.fastjson.JSON;
import com.sioo.db.mybatis.SessionFactory;
import com.sioo.db.mybatis.mapper.SmsUserSignMapper;
import com.sioo.log.LogInfo;
import com.sioo.service.model.UserSign;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/***
 * 通道签名Dao类
 * 
 * @author OYJM
 * @date 2016年9月20日
 *
 */
public class SmsUserSignDao {
	private static Logger log = Logger.getLogger(SmsUserSignDao.class);

	private static SmsUserSignDao smsSignChannelDao;

	public static SmsUserSignDao getInstance() {
		if (smsSignChannelDao != null) {
			return smsSignChannelDao;
		}
		synchronized (SmsUserSignDao.class) {
			if (smsSignChannelDao == null) {
				smsSignChannelDao = new SmsUserSignDao();
			}
		}
		return smsSignChannelDao;
	}


	/***
	 * 获取用户签名列表
	 * 
	 * @param uid
	 * @param store
	 * @param type
	 * @return
	 */
	public CopyOnWriteArrayList<UserSign> findUserSignByUidAndStore(Integer uid, String store, Integer type) {
		SqlSession session = null;
		try {
			session = SessionFactory.getSessionFactory().openSession();
			SmsUserSignMapper mapper = session.getMapper(SmsUserSignMapper.class);
			return mapper.findUserSignByUidAndStore(uid, store, type);
		} catch (Exception ex) {
			LogInfo.getLog().errorAlert("获取用户签名列表异常", "[SmsSignChannelDao.findUserSignByUidAndStore()]" + LogInfo.getTrace(ex));
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return null;
	}

	/***
	 * 获取签名扩展
	 * 
	 * @return
	 */
	public String findMaxExpend2() {
		SqlSession session = null;
		try {
			session = SessionFactory.getSessionFactory().openSession();
			SmsUserSignMapper mapper = session.getMapper(SmsUserSignMapper.class);
			String result = mapper.findMaxExpend2();
			return result;
		} catch (Exception ex) {
			LogInfo.getLog().errorAlert("获取普通用户自增长拓展号异常", "[SmsSignChannelDao.findMaxExpend2()]" + LogInfo.getTrace(ex));
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return null;
	}

	/***
	 * 根据生成时间获取用户签名信息列表
	 * 
	 * @return
	 */
	public List<UserSign> findUserSignByExpends(String expends) {
		SqlSession session = null;
		try {
			session = SessionFactory.getSessionFactory().openSession();
			SmsUserSignMapper mapper = session.getMapper(SmsUserSignMapper.class);
			return mapper.findUserSignByExpends(expends);
		} catch (Exception ex) {
			LogInfo.getLog().errorAlert("根据生成时间获取用户签名信息异常", "[SmsSignChannelDao.findUserSignByExpends()]" + LogInfo.getTrace(ex));
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return null;
	}


	/***
	 * 添加通道签名
	 * 
	 * @return
	 */
	public Integer insertUserSign(UserSign userSign) {
		SqlSession session = null;
		try {
			session = SessionFactory.getSessionFactory().openSession();
			SmsUserSignMapper mapper = session.getMapper(SmsUserSignMapper.class);
			Integer result = mapper.insertUserSign(userSign);
			return result;
		} catch (Exception ex) {
			// 不需要告警信息
			log.error(ex.getMessage());
			log.error("保存签名信息异常[SmsSignChannelDao.insertSmsSignChannel(" + JSON.toJSONString(userSign) + ")]");
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return -1;
	}
	
	/***
	 * 修改用户签名
	 * @param userSign
	 * @return
	 */
	public Integer updateUserSign(UserSign userSign) {
		SqlSession session = null;
		try {
			session = SessionFactory.getSessionFactory().openSession();
			SmsUserSignMapper mapper = session.getMapper(SmsUserSignMapper.class);
			Integer result = mapper.updateUserSign(userSign);
			return result;
		} catch (Exception ex) {
			// 不需要告警信息
			log.error("修改签名信息异常[SmsSignChannelDao.updateUserSign(" + JSON.toJSONString(userSign) + ")]" + LogInfo.getTrace(ex));
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return -1;
	}
}
