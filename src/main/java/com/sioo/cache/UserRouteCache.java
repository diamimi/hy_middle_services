package com.sioo.cache;

import com.sioo.dao.SysCacheDao;
import com.sioo.log.LogInfo;
import com.sioo.util.SmsCache;
import com.sioo.util.UpdateCacheConstant.METHOD;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/***
 * 用户路由缓存操作类
 *
 * @author OYJM
 * @date 2016年12月3日
 *
 */
public class UserRouteCache {
    private static Logger log = LoggerFactory.getLogger(UserRouteCache.class);
    private static UserRouteCache userRouteCache = null;

    public static UserRouteCache getInstance() {
        if (userRouteCache != null) {
            return userRouteCache;
        }
        synchronized (UserRouteCache.class) {
            if (userRouteCache == null) {
                userRouteCache = new UserRouteCache();
            }
        }
        return userRouteCache;
    }

    /***
     * 接口修改用户路由入口
     *
     * @param method
     * @param uid
     * @param content
     */
    public void excute(Integer method, Integer uid, String content) {
        try {
            if (method.equals(METHOD.ADD) || method.equals(METHOD.UPDATE) || method.equals(METHOD.RELOAD) || method.equals(METHOD.DELETE)) {
                deleteUserRouteByUid(uid);
                loadUserRoute(uid);
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("修改用户路由缓存异常", "[UserRouteCache.excute(" + method + "," + uid + "," + content + ") ]" + LogInfo.getTrace(e));
        }
    }


    /****
     * 加载用户路由
     *
     * @param uid
     */
    public void loadUserRoute(Integer uid) {
        try {
            List<Map<String, Object>> userRountList = SysCacheDao.getInstance().findSmsUserRoute(uid);
            if (null != userRountList && userRountList.size() > 0) {
                List<Map<String, Object>> array = null;
                Object uidFlag = userRountList.get(0).get("uid");
                for (int i = 0; i < userRountList.size(); i++) {
                    Map<String, Object> map = userRountList.get(i);
                    if (i == 0 || !map.get("uid").equals(uidFlag)) {
                        array = new ArrayList<Map<String, Object>>();
                        uidFlag = map.get("uid");
                    }
                    array.add(map);
                    if (i == userRountList.size() - 1 || !map.get("uid").equals(userRountList.get(i + 1).get("uid"))) {
                        SmsCache.USER_ROUTE.put((Integer) map.get("uid"), array);
                    }
                }
                log.info("用户" + uid + "路由加载【" + userRountList.size() + "】个");
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("加载用户路由缓存异常", "[UserRouteCache.loadUserRoute(" + uid + ") ]" + LogInfo.getTrace(e));
        }
    }

    /***
     * 重新加载用户路由
     *
     */
    public void reloadUserRoute() {
        this.loadUserRoute(null);
    }


    /***
     * 删除用户路由
     *
     * @param uid
     * @param content
     */
    public void deleteUserRoute(Integer uid, String content) {
        try {
            if (uid == null || content == null || content.length() < 1) {
                return;
            }

            content = URLDecoder.decode(content, "UTF-8");

            if (SmsCache.USER_ROUTE.containsKey(uid) && null != SmsCache.USER_ROUTE.get(uid)) {
                List<Map<String, Object>> array = SmsCache.USER_ROUTE.get(uid);
                for (Map<String, Object> map : array) {
                    String routeContent = map.get("content").toString();
                    if (content.equals(routeContent)) {
                        array.remove(map);
                        log.info("删除用户路由; uid:" + uid + ", content:" + content);
                        break;
                    }
                }
                SmsCache.USER_ROUTE.put(uid, array);
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("根据关键字删除用户路由缓存异常", "[UserRouteCache.deleteUserRoute(" + uid + ", " + content + ") ]" + LogInfo.getTrace(e));
        }
    }

    /****
     * 根据用户ID删除用户路由
     *
     * @param uid
     */
    public void deleteUserRouteByUid(Integer uid) {
        try {
            if (SmsCache.USER_ROUTE.containsKey(uid) && null != SmsCache.USER_ROUTE.get(uid)) {
                SmsCache.USER_ROUTE.remove(uid);
                log.info("删除用户路由; uid:" + uid);
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("根据ID删除用户路由缓存异常", "[UserRouteCache.deleteUserRouteByUid(" + uid + ") ]" + LogInfo.getTrace(e));
        }
    }

    /**
     * 设置用户路由2.0版
     *
     * @param uid
     * @param mtype
     * @param content
     * @param mobile
     * @param provinceCode
     * @param cityCode
     * @return
     */
    public Map<String, Object> getUserRouteChannel(int uid, int mtype, String content, Long mobile, int provinceCode, int cityCode) {
        try {
            List<Map<String, Object>> userRouteList = SmsCache.USER_ROUTE.get(uid);
            if (userRouteList != null && userRouteList.size() > 0) {
                for (Map<String, Object> userRoute : userRouteList) {
                    if (userRoute.get("mtype").toString().equals(String.valueOf(mtype))) {
                        String routeContent = userRoute.get("content").toString();
                        int province = userRoute.get("province") == null ? 0 : Integer.valueOf(userRoute.get("province").toString());
                        int city = userRoute.get("city") == null ? 0 : Integer.valueOf(userRoute.get("city").toString());
                        int contentType = Integer.valueOf(userRoute.get("contentType").toString());
                        String[] routes = routeContent.split(",");
                        for (String s : routes) {
                            if (province == 0) {
                                if (contentType == 0) {
                                    if (s != null && s.length() > 0 && content.indexOf(s) != -1) {
                                        log.info("触发用户路由; uid:{},mobile:{},words:{}", uid, mobile, s);
                                        return userRoute;
                                    }
                                } else if (contentType == 1) {
                                    if (s != null && s.length() > 0 && (StringUtils.startsWith(content, s) || StringUtils.endsWith(content, s))) {
                                        log.info("触发用户路由; uid:{},mobile:{},words:{}", uid, mobile, s);
                                        return userRoute;
                                    }
                                }
                            } else if (province > 0) {
                                if (city == 0) {
                                    if (contentType == 0) {
                                        if (s != null && s.length() > 0 && provinceCode == province && content.indexOf(s) != -1) {
                                            log.info("触发用户路由; uid:{},mobile:{},words:{},province:{}", uid, mobile, s, provinceCode);
                                            return userRoute;
                                        }
                                    } else if (contentType == 1) {
                                        if (s != null && s.length() > 0 && provinceCode == province && (StringUtils.startsWith(content, s) || StringUtils.endsWith(content, s))) {
                                            log.info("触发用户路由; uid:{},mobile:{},words:{},province:{}", uid, mobile, s, provinceCode);
                                            return userRoute;
                                        }
                                    }
                                } else if (city > 0) {
                                    if (contentType == 0) {
                                        if (s != null && s.length() > 0 && provinceCode == province && cityCode == city && content.indexOf(s) != -1) {
                                            log.info("触发用户路由; uid:{},mobile:{},words:{},province:{},city:{}", uid, mobile, s, provinceCode, cityCode);
                                            return userRoute;
                                        }
                                    } else if (contentType == 1) {
                                        if (s != null && s.length() > 0 && provinceCode == province && cityCode == city && (StringUtils.startsWith(content, s) || StringUtils.endsWith(content, s))) {
                                            log.info("触发用户路由; uid:{},mobile:{},words:{},province:{},city:{}", uid, mobile, s, provinceCode, cityCode);
                                            return userRoute;
                                        }
                                    }

                                }
                            }

                        }
                    }
                }
            }

        } catch (Exception e) {
            LogInfo.getLog().errorAlert("获取用户路由缓存异常", "[UserRouteCache.getUserRouteChannel(" + uid + ", " + content + ") ]" + LogInfo.getTrace(e));
        }
        return null;
    }

}
