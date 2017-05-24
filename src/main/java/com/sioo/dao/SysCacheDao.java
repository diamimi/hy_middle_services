package com.sioo.dao;

import com.sioo.db.mybatis.SessionFactory;
import com.sioo.db.mybatis.mapper.SysCacheMapper;
import com.sioo.log.LogInfo;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Map;

/***
 * 缓存信息DAO类
 *
 * @author OYJM
 * @date 2016年9月18日
 *
 */
public class SysCacheDao {
    private static SysCacheDao sysCacheDao;

    public static SysCacheDao getInstance() {
        if (sysCacheDao != null) {
            return sysCacheDao;
        }
        synchronized (SysCacheDao.class) {
            if (sysCacheDao == null) {
                sysCacheDao = new SysCacheDao();
            }
        }
        return sysCacheDao;
    }

    public List<Map<String, Object>> findUserBlackMobile(Integer uid) {
        SqlSession session = null;
        try {
            session = SessionFactory.getSessionFactory().openSession();
            SysCacheMapper mapper = session.getMapper(SysCacheMapper.class);
            List<Map<String, Object>> list = mapper.findUserBlackMobile(uid);
            return list;
        } catch (Exception ex) {
            LogInfo.getLog().errorAlert("获取用户黑名单异常", "[SysCacheDao.findUserBlackMobile(" + uid + ") ]" + LogInfo.getTrace(ex));
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public List<Map<String, Object>> findAllUserBlackMobile() {
        SqlSession session = null;
        try {
            session = SessionFactory.getSessionFactory().openSession();
            SysCacheMapper mapper = session.getMapper(SysCacheMapper.class);
            List<Map<String, Object>> list = mapper.findAllUserBlackMobile();
            return list;
        } catch (Exception ex) {
            LogInfo.getLog().errorAlert("获取用户黑名单异常", "[SysCacheDao.findAllUserBlackMobile() ]" + LogInfo.getTrace(ex));
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public List<Map<String, Object>> findUserWhiteMobile(Integer uid) {
        SqlSession session = null;
        try {
            session = SessionFactory.getSessionFactory().openSession();
            SysCacheMapper mapper = session.getMapper(SysCacheMapper.class);
            List<Map<String, Object>> list = mapper.findUserWhiteMobile(uid);
            return list;
        } catch (Exception ex) {
            LogInfo.getLog().errorAlert("获取用户白名单异常", "[SysCacheDao.findUserWhiteMobile(" + uid + ") ]" + LogInfo.getTrace(ex));
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public List<Map<String, Object>> findSmsWordsUser(Integer uid,int type) {
        SqlSession session = null;
        try {
            session = SessionFactory.getSessionFactory().openSession();
            SysCacheMapper mapper = session.getMapper(SysCacheMapper.class);
            List<Map<String, Object>> list = mapper.findSmsWordsUser(uid,type);
            return list;
        } catch (Exception ex) {
            LogInfo.getLog().errorAlert("获取用户屏蔽词异常", "[SysCacheDao.findSmsWordsUser(" + uid + ") ]" + LogInfo.getTrace(ex));
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public List<Map<String, Object>> findSmsWordsChannel(Integer channel) {
        SqlSession session = null;
        try {
            session = SessionFactory.getSessionFactory().openSession();
            SysCacheMapper mapper = session.getMapper(SysCacheMapper.class);
            List<Map<String, Object>> list = mapper.findSmsWordsChannel(channel);
            return list;
        } catch (Exception ex) {
            LogInfo.getLog().errorAlert("获取通道屏蔽词异常", "[SysCacheDao.findSmsWordsChannel(" + channel + ") ]" + LogInfo.getTrace(ex));
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public Map<String, Object> findSmsUserByUid(Integer uid) {
        if (uid == null) {
            return null;
        }
        SqlSession session = null;
        try {
            session = SessionFactory.getSessionFactory().openSession();
            SysCacheMapper mapper = session.getMapper(SysCacheMapper.class);
            Map<String, Object> list = mapper.findSmsUserByUid(uid);
            return list;
        } catch (Exception ex) {
            LogInfo.getLog().errorAlert("获取用户信息异常", "[SysCacheDao.findSmsUserByUid(" + uid + ") ]" + LogInfo.getTrace(ex));
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public List<Map<String, Object>> findSmsUser() {

        SqlSession session = null;
        try {
            session = SessionFactory.getSessionFactory().openSession();
            SysCacheMapper mapper = session.getMapper(SysCacheMapper.class);
            return mapper.findSmsUser();
        } catch (Exception ex) {
            LogInfo.getLog().errorAlert("获取所有用户列表异常", "[SysCacheDao.findSmsUser() ]" + LogInfo.getTrace(ex));
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public List<Integer> findUid(Integer uid, Integer child) {
        SqlSession session = null;
        try {
            session = SessionFactory.getSessionFactory().openSession();
            SysCacheMapper mapper = session.getMapper(SysCacheMapper.class);
            List<Integer> list = mapper.findUid(uid, child);
            return list;
        } catch (Exception ex) {
            LogInfo.getLog().errorAlert("获取用户ID列表异常", "[SysCacheDao.findUid(" + uid + "," + child + ") ]" + LogInfo.getTrace(ex));
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public Map<String, Object> findSmsUserControl(Integer uid) {
        if (uid == null) {
            return null;
        }
        SqlSession session = null;
        try {
            session = SessionFactory.getSessionFactory().openSession();
            SysCacheMapper mapper = session.getMapper(SysCacheMapper.class);
            Map<String, Object> list = mapper.findSmsUserControl(uid);
            return list;
        } catch (Exception ex) {
            LogInfo.getLog().errorAlert("获取用户控制信息异常", "[SysCacheDao.findSmsUserControl(" + uid + ") ]" + LogInfo.getTrace(ex));
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public List<Map<String, Object>> findSmsChannel() {
        SqlSession session = null;
        try {
            session = SessionFactory.getSessionFactory().openSession();
            SysCacheMapper mapper = session.getMapper(SysCacheMapper.class);
            List<Map<String, Object>> list = mapper.findSmsChannel();
            return list;
        } catch (Exception ex) {
            LogInfo.getLog().errorAlert("获取所有通道信息异常", "[SysCacheDao.findSmsChannel() ]" + LogInfo.getTrace(ex));
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public Map<String, Object> findSmsChannelById(Integer channelId) {
        SqlSession session = null;
        try {
            session = SessionFactory.getSessionFactory().openSession();
            SysCacheMapper mapper = session.getMapper(SysCacheMapper.class);
            return mapper.findSmsChannelById(channelId);
        } catch (Exception ex) {
            LogInfo.getLog().errorAlert("根据Id获取通道信息异常", "[SysCacheDao.findSmsChannelById(" + channelId + ") ]" + LogInfo.getTrace(ex));
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public List<Map<String, Object>> findSmsUserAlert(Integer uid) {
        SqlSession session = null;
        try {
            session = SessionFactory.getSessionFactory().openSession();
            SysCacheMapper mapper = session.getMapper(SysCacheMapper.class);
            List<Map<String, Object>> list = mapper.findSmsUserAlert(uid);
            return list;
        } catch (Exception ex) {
            LogInfo.getLog().errorAlert("获取用户提醒信息异常", "[SysCacheDao.findSmsUserAlert(" + uid + ") ]" + LogInfo.getTrace(ex));
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public List<Map<String, Object>> findSmsSignChannelBlack(Integer uid, String store) {
        SqlSession session = null;
        try {
            session = SessionFactory.getSessionFactory().openSession();
            SysCacheMapper mapper = session.getMapper(SysCacheMapper.class);
            List<Map<String, Object>> list = mapper.findSmsSignChannelBlack(uid, store);
            return list;
        } catch (Exception ex) {
            LogInfo.getLog().errorAlert("获取黑签名库异常", "[SysCacheDao.findSmsSignChannelBlack(" + uid + "," + store + ") ]" + LogInfo.getTrace(ex));
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public List<Map<String, Object>> findUserWhiteSign(Integer uid) {
        SqlSession session = null;
        try {
            session = SessionFactory.getSessionFactory().openSession();
            SysCacheMapper mapper = session.getMapper(SysCacheMapper.class);
            List<Map<String, Object>> list = mapper.findUserWhiteSign(uid);
            return list;
        } catch (Exception ex) {
            LogInfo.getLog().errorAlert("获取用户白签名异常", "[SysCacheDao.findUserWhiteSign(" + uid + ") ]" + LogInfo.getTrace(ex));
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public List<Map<String, Object>> findSmsUserSendModel(Integer uid) {
        SqlSession session = null;
        try {
            session = SessionFactory.getSessionFactory().openSession();
            SysCacheMapper mapper = session.getMapper(SysCacheMapper.class);
            List<Map<String, Object>> list = mapper.findSmsUserSendModel(uid);
            return list;
        } catch (Exception ex) {
            LogInfo.getLog().errorAlert("获取用户发送模板异常", "[SysCacheDao.findSmsUserSendModel(" + uid + ") ]" + LogInfo.getTrace(ex));
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public List<Map<String, Object>> findSmsUserRoute(Integer uid) {
        SqlSession session = null;
        try {
            session = SessionFactory.getSessionFactory().openSession();
            SysCacheMapper mapper = session.getMapper(SysCacheMapper.class);
            List<Map<String, Object>> list = mapper.findSmsUserRoute(uid);
            return list;
        } catch (Exception ex) {
            LogInfo.getLog().errorAlert("获取用户路由异常", "[SysCacheDao.findSmsUserRoute(" + uid + ") ]" + LogInfo.getTrace(ex));
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public List<Map<String, Object>> findSmsUserStrategyRelation(Integer uid) {
        SqlSession session = null;
        try {
            session = SessionFactory.getSessionFactory().openSession();
            SysCacheMapper mapper = session.getMapper(SysCacheMapper.class);
            List<Map<String, Object>> list = mapper.findSmsUserStrategyRelation(uid);
            return list;
        } catch (Exception ex) {
            LogInfo.getLog().errorAlert("获取用户策略组信息异常", "[SysCacheDao.findSmsUserStrategyRelation(" + uid + ") ]" + LogInfo.getTrace(ex));
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public List<Map<String, Object>> findStrategyGourp(Integer type) {
        SqlSession session = null;
        try {
            session = SessionFactory.getSessionFactory().openSession();
            SysCacheMapper mapper = session.getMapper(SysCacheMapper.class);
            List<Map<String, Object>> list = mapper.findStrategyGourp(type);
            return list;
        } catch (Exception ex) {
            LogInfo.getLog().errorAlert("获取所有策略组信息异常", "[SysCacheDao.findStrategyGourp(" + type + ") ]" + LogInfo.getTrace(ex));
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public List<Map<String, Object>> findChannelGourp(Integer channel) {
        SqlSession session = null;
        try {
            session = SessionFactory.getSessionFactory().openSession();
            SysCacheMapper mapper = session.getMapper(SysCacheMapper.class);
            List<Map<String, Object>> list = mapper.findChannelGourp(channel);
            return list;
        } catch (Exception ex) {
            LogInfo.getLog().errorAlert("获取通道组异常", "[SysCacheDao.findChannelGourp(" + channel + ") ]" + LogInfo.getTrace(ex));
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public List<Map<String, Object>> findSmsMobileArea(Integer provincecode, Integer citycode) {
        SqlSession session = null;
        try {
            session = SessionFactory.getSessionFactory().openSession();
            SysCacheMapper mapper = session.getMapper(SysCacheMapper.class);
            List<Map<String, Object>> list = mapper.findSmsMobileArea(provincecode, citycode);
            return list;
        } catch (Exception ex) {
            LogInfo.getLog().errorAlert("获取归属地异常", "[SysCacheDao.findSmsMobileArea(" + provincecode + ") ]" + LogInfo.getTrace(ex));
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public Map<String, Object> findSmsMobileAreaById(Integer id) {
        SqlSession session = null;
        try {
            session = SessionFactory.getSessionFactory().openSession();
            SysCacheMapper mapper = session.getMapper(SysCacheMapper.class);
            Map<String, Object> map = mapper.findSmsMobileAreaById(id);
            return map;
        } catch (Exception ex) {
            LogInfo.getLog().errorAlert("根据id获取归属地异常", "[SysCacheDao.findSmsMobileAreaById(" + id + ") ]" + LogInfo.getTrace(ex));
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public List<Map<String, Object>> findSmsBlackArea(Integer uid) {
        SqlSession session = null;
        try {
            session = SessionFactory.getSessionFactory().openSession();
            SysCacheMapper mapper = session.getMapper(SysCacheMapper.class);
            List<Map<String, Object>> list = mapper.findSmsBlackArea(uid);
            return list;
        } catch (Exception ex) {
            LogInfo.getLog().errorAlert("获取用户屏蔽地区异常", "[SysCacheDao.findSmsBlackArea(" + uid + ") ]" + LogInfo.getTrace(ex));
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public List<Map<String, Object>> findSmsBlackAreaByChannel(Integer channelId) {
        SqlSession session = null;
        try {
            session = SessionFactory.getSessionFactory().openSession();
            SysCacheMapper mapper = session.getMapper(SysCacheMapper.class);
            List<Map<String, Object>> list = mapper.findSmsBlackAreaByChannel(channelId);
            return list;
        } catch (Exception ex) {
            LogInfo.getLog().errorAlert("获取通道屏蔽地区异常", "[SysCacheDao.findSmsBlackAreaByChannel(" + channelId + ") ]" + LogInfo.getTrace(ex));
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public int findSysConfig() {
        SqlSession session = null;
        try {
            session = SessionFactory.getSessionFactory().openSession();
            SysCacheMapper mapper = session.getMapper(SysCacheMapper.class);
            int control = mapper.findSysConfig();
            return control;
        } catch (Exception ex) {
            LogInfo.getLog().errorAlert("获取系统配置信息异常", "[SysCacheDao.findSysConfig() ]" + LogInfo.getTrace(ex));
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return 0;
    }

    public List<Map<String, Object>> findSmsRptRatioConfig(Integer uid) {
        SqlSession session = null;
        try {
            session = SessionFactory.getSessionFactory().openSession();
            SysCacheMapper mapper = session.getMapper(SysCacheMapper.class);
            List<Map<String, Object>> list = mapper.findSmsRptRatioConfig(uid);
            return list;
        } catch (Exception ex) {
            LogInfo.getLog().errorAlert("获取扣量用户配置信息异常", "[SysCacheDao.findSmsRptRatioConfig(" + uid + ") ]" + LogInfo.getTrace(ex));
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public List<Map<String, Object>> findReleaseTemplate(Integer uid) {
        SqlSession session = null;
        try {
            session = SessionFactory.getSessionFactory().openSession();
            SysCacheMapper mapper = session.getMapper(SysCacheMapper.class);
            List<Map<String, Object>> list = mapper.findReleaseTemplate(uid);
            return list;
        } catch (Exception ex) {
            LogInfo.getLog().errorAlert("获取审核模板异常", "[SysCacheDao.findReleaseTemplate(" + uid + ") ]" + LogInfo.getTrace(ex));
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    /**
     * 加载系统策略组
     * @return
     */
    public List<Map<String,Object>> findSmsStrategyGroup() {
        SqlSession session = null;
        try {
            session = SessionFactory.getSessionFactory().openSession();
            SysCacheMapper mapper = session.getMapper(SysCacheMapper.class);
            List<Map<String, Object>> list = mapper.findSmsStrategyGroup();
            return list;
        } catch (Exception ex) {
            LogInfo.getLog().errorAlert("获取审核模板异常", "[SysCacheDao.findReleaseTemplate]" + LogInfo.getTrace(ex));
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }
}