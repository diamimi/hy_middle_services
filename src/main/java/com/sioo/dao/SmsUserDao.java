package com.sioo.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.alibaba.fastjson.JSON;
import com.sioo.db.mybatis.SessionFactory;
import com.sioo.db.mybatis.mapper.SmsUserMapper;
import com.sioo.hy.cmpp.vo.ConsumeVo;
import com.sioo.hy.cmpp.vo.SmsUserVo;
import com.sioo.log.LogInfo;

/****
 * 用户相关信息Dao类
 * 
 * @author OYJM
 * @date 2016年9月20日
 *
 */
public class SmsUserDao {
	private static SmsUserDao smsUserDao;

	public static SmsUserDao getInstance() {
		if (smsUserDao != null) {
			return smsUserDao;
		}
		synchronized (SmsUserDao.class) {
			if (smsUserDao == null) {
				smsUserDao = new SmsUserDao();
			}
		}
		return smsUserDao;
	}

	/***
	 * 修改用户余额
	 * 
	 * @param uid
	 * @param send
	 * @param sms
	 * @return
	 */
	public int updateSmsUser(List<SmsUserVo> list) {
		if (list == null || list.isEmpty()) {
			return 0;
		}
		SqlSession session = null;
		try {
			session = SessionFactory.getSessionFactory().openSession(true);
			SmsUserMapper mapper = session.getMapper(SmsUserMapper.class);
			return mapper.updateSmsUser(list);
		} catch (Exception ex) {
			LogInfo.getLog().errorAlert("修改用户信息异常", "[SmsUserDao.updateSmsUser() Exception]; data:" + JSON.toJSONString(list) + LogInfo.getTrace(ex));
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return -1;
	}

	/***
	 * 更新用户消费记录
	 * 
	 * @param uid
	 * @param send
	 * @param date
	 * @return
	 */
	public int updateSmsUserConsume(List<ConsumeVo> list, Long date) {
		if (list == null || list.isEmpty()) {
			return 0;
		}
		SqlSession session = null;
		try {
			session = SessionFactory.getSessionFactory().openSession(true);
			SmsUserMapper mapper = session.getMapper(SmsUserMapper.class);
			int result = mapper.updateSmsUserConsume(list, date);
			return result;
		} catch (Exception ex) {
			LogInfo.getLog().errorAlert("批量修改用户消费记录异常", "[SmsUserDao.updateSmsUserConsume() Exception]; data:" + JSON.toJSONString(list) + LogInfo.getTrace(ex));
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return -1;
	}

	public int updateSmsUserConsumeUnKou(List<ConsumeVo> list, Long date) {
		if (list == null || list.isEmpty()) {
			return 0;
		}
		SqlSession session = null;
		try {
			session = SessionFactory.getSessionFactory().openSession(true);
			SmsUserMapper mapper = session.getMapper(SmsUserMapper.class);
			int result = mapper.updateSmsUserConsumeUnKou(list, date);
			return result;
		} catch (Exception ex) {
			LogInfo.getLog().errorAlert("批量修改用户消费记录异常", "[SmsUserDao.updateSmsUserConsume() Exception]; data:" + JSON.toJSONString(list) + LogInfo.getTrace(ex));
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return -1;
	}
	/***
	 * 添加用户消费记录
	 * 
	 * @param list
	 * @return
	 */
	public int saveSmsUserConsume(List<ConsumeVo> list) {
		if (list == null || list.isEmpty()) {
			return 0;
		}
		SqlSession session = null;
		try {
			session = SessionFactory.getSessionFactory().openSession(true);
			SmsUserMapper mapper = session.getMapper(SmsUserMapper.class);
			int result = mapper.saveSmsUserConsume(list);
			return result;
		} catch (Exception ex) {
			LogInfo.getLog().errorAlert("批量保存用户消费记录异常", "[SmsUserDao.saveSmsUserConsume() Exception]; data:" + JSON.toJSONString(list) + LogInfo.getTrace(ex));
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return -1;
	}

	/****
	 * 查询用户余额
	 * 
	 * @param id
	 * @return
	 */
	public int findSmsById(Integer id) {
		SqlSession session = null;
		try {
			session = SessionFactory.getSessionFactory().openSession();
			SmsUserMapper mapper = session.getMapper(SmsUserMapper.class);
			Map<String, Object> map = mapper.findSmsUserById(id);
			if (map == null || map.get("sms") == null) {
				return 0;
			} else {
				return Integer.valueOf(map.get("sms").toString());
			}

		} catch (Exception ex) {
			LogInfo.getLog().errorAlert("根据用户ID查询用户余额异常", "[SmsUserDao.findSmsById(" + id + ") Exception]" + LogInfo.getTrace(ex));
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return 0;
	}

	/***
	 * 根据ID查询用户信息
	 * 
	 * @param id
	 * @return
	 */
	public Map<String, Object> findSmsUserById(Integer id) {
		SqlSession session = null;
		try {
			session = SessionFactory.getSessionFactory().openSession();
			SmsUserMapper mapper = session.getMapper(SmsUserMapper.class);
			return mapper.findSmsUserById(id);
		} catch (Exception ex) {
			LogInfo.getLog().errorAlert("根据ID获取用户信息异常", "[SmsUserDao.findSmsUserById(" + id + ") Exception]" + LogInfo.getTrace(ex));
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return null;
	}

	/***
	 * 查询用户短信审核数量
	 * 
	 * @param uid
	 * @return
	 */
	public int findSmsUserReleaseNum(Integer uid) {
		SqlSession session = null;
		try {
			session = SessionFactory.getSessionFactory().openSession();
			SmsUserMapper mapper = session.getMapper(SmsUserMapper.class);
			return mapper.findSmsUserReleaseNum(uid);
		} catch (Exception ex) {
			LogInfo.getLog().errorAlert("获取用户审核条数异常", "[SmsUserDao.findSmsUserReleaseNum(" + uid + ") Exception]" + LogInfo.getTrace(ex));
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return 0;
	}

	/***
	 * 更新用户基本信息
	 * 
	 * @param map
	 * @return
	 */
	public int saveOrUpdateUser(Map<String, Object> map) {
		int result = 0;
		if (map == null || map.isEmpty()) {
			return result;
		}
		SqlSession session = null;
		try {
			session = SessionFactory.getSessionFactory().openSession(true);
			SmsUserMapper mapper = session.getMapper(SmsUserMapper.class);
			Map<String, Object> current = mapper.findSmsUserById(Integer.parseInt(map.get("id").toString()));
			if (current != null) {
				map.put("sms", null);
				map.put("send", null);
				result = mapper.updateUser(map);
			} else {
				// 初始值为10000000
				map.put("sms", 10000000);
				map.put("send", 0);
				result = mapper.saveUser(map);
			}
			return result;
		} catch (Exception ex) {
			LogInfo.getLog().errorAlert("保存用户信息异常", "[SmsUserDao.saveOrUpdateUser() Exception]" + LogInfo.getTrace(ex));
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return -1;
	}

	/***
	 * 更新用户控制信息
	 * 
	 * @param map
	 * @return
	 */
	public int saveOrUpdateUserControl(Map<String, Object> map) {
		int result = 0;
		if (map == null || map.isEmpty()) {
			return result;
		}
		SqlSession session = null;
		try {
			session = SessionFactory.getSessionFactory().openSession(true);
			SmsUserMapper mapper = session.getMapper(SmsUserMapper.class);
			Map<String, Object> current = mapper.findSmsUserControlById(Integer.parseInt(map.get("uid").toString()));
			if (current != null) {
				result = mapper.updateUserControl(map);
			} else {
				result = mapper.saveUserControl(map);
			}
			return result;
		} catch (Exception ex) {
			LogInfo.getLog().errorAlert("保存用户控制信息异常", "[SmsUserDao.saveOrUpdateUserControl() Exception]" + LogInfo.getTrace(ex));
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return -1;
	}

}
