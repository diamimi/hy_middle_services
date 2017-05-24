package com.sioo.cache;

import com.sioo.dao.SysCacheDao;
import com.sioo.hy.cmpp.vo.SendingVo;
import com.sioo.hy.cmpp.vo.SmsBlackWords;
import com.sioo.log.LogInfo;
import com.sioo.servlet.HttpSubmitServer;
import com.sioo.util.*;
import com.sioo.util.UpdateCacheConstant.METHOD;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/****
 * 策略组缓存操作类
 *
 * @author OYJM
 * @date 2016年9月27日
 *
 */
public class StrategyGroupCache {
    private static Logger log = LoggerFactory.getLogger(StrategyGroupCache.class);
    private static StrategyGroupCache strategyGroupCache = null;

    public static StrategyGroupCache getInstance() {
        if (strategyGroupCache != null) {
            return strategyGroupCache;
        }
        synchronized (StrategyGroupCache.class) {
            if (strategyGroupCache == null) {
                strategyGroupCache = new StrategyGroupCache();
            }
        }
        return strategyGroupCache;
    }

    /**
     * 操作系统策略组:增,删,改
     *
     * @param method
     * @param groupType
     * @param groupId
     */
    public String excute(Integer method, Integer groupType, Integer groupId) {
        String result = HttpSubmitServer.FAIL;
        try {
            if (method == null || groupId == null || groupType == null) {
                return HttpSubmitServer.FAIL;
            }
            if (method.equals(METHOD.ADD) || method.equals(METHOD.UPDATE) || method.equals(METHOD.RELOAD)) {
                Map<Integer, String> sysStrategyGroup = SmsCache.SYS_STRATEGY_GROUP;
                for (Integer key : sysStrategyGroup.keySet()) {
                    if (key.equals(groupType)) {
                        String groupIds = sysStrategyGroup.get(key);
                        String[] idString = groupIds.split(",");
                        boolean flag = true;
                        for (String s : idString) {
                            if (Integer.valueOf(s).equals(groupId)) {
                                flag = false;
                                break;
                            }
                        }
                        if (flag) {
                            groupIds += "," + groupId;
                            sysStrategyGroup.put(key, groupIds);
                            SmsCache.SYS_STRATEGY_GROUP = sysStrategyGroup;
                            log.info("增加策略组:{},groupId:{}", groupType, groupId);
                            result = HttpSubmitServer.SUCC;
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("修改策略组缓存信息异常",
                    "[StrategyGroupCache.excuteStrategyGroup(" + method + "," + groupType + "," + groupId);
        }
        return result;
    }


    /**
     * 后台发送的修改策略组请求，更新本地缓存
     *
     * @param method
     * @param groupType
     * @param groupId
     * @param content
     * @param screenType
     */
    public String excuteStrategyGroup(Integer method, Integer groupType, Integer groupId, String content, int screenType) {
        String result = HttpSubmitServer.SUCC;
        try {
            if (method == null || groupType == null) {
                return HttpSubmitServer.FAIL;
            }

            if (content != null && content.length() > 0) {
                content = URLDecoder.decode(content, "UTF-8");
            }

            switch (groupType) {
                case ConstantSys.USER_STRATEGY_GROUP_SYS_BLACK_WORDS_AUTO:
                    if (method.equals(METHOD.ADD) || method.equals(METHOD.UPDATE) || method.equals(METHOD.RELOAD)) {
                        if (content == null || content.isEmpty()) {
                            SmsCache.GROUP_AUTO_WORDS_SCREENTYPE.remove(groupId);
                            loadGroupAutoWords1();
                            log.info("重新加载自动屏蔽词策略组【" + groupId + "】");
                        } else {
                            CopyOnWriteArrayList<SmsBlackWords> list = SmsCache.GROUP_AUTO_WORDS_SCREENTYPE.get(groupId);
                            if (list == null) {
                                list = new CopyOnWriteArrayList<>();
                            }
                            String[] arrays = content.split(",");
                            for (String item : arrays) {
                                // 如果策略组为空，或策略组不存在该策略
                                if (list.isEmpty()) {
                                    SmsBlackWords smsBlackWords = new SmsBlackWords();
                                    smsBlackWords.setWords(item);
                                    smsBlackWords.setScreentype(screenType);
                                    smsBlackWords.setGroup_id(groupId);
                                    list.add(smsBlackWords);
                                    SmsCache.GROUP_AUTO_WORDS_SCREENTYPE.put(groupId, list);
                                    log.info("加载自动屏蔽词策略组:{},屏蔽词:{},屏蔽类型:{}成功", groupId, item, screenType);
                                } else {
                                    int falg = 1;
                                    for (SmsBlackWords blackWords : list) {
                                        if (blackWords.getScreentype().equals(screenType) && StringUtils.equals(blackWords.getWords(), item) && blackWords.getGroup_id().equals(groupId)) {
                                            falg = -1;
                                            result = HttpSubmitServer.FAIL;
                                            log.info("加载自动屏蔽词策略组【" + groupId + "】屏蔽词【" + item + "】失败，屏蔽词已存在");
                                            break;
                                        }
                                    }
                                    if (falg == 1) {
                                        SmsBlackWords smsBlackWords = new SmsBlackWords();
                                        smsBlackWords.setWords(item);
                                        smsBlackWords.setScreentype(screenType);
                                        smsBlackWords.setGroup_id(groupId);
                                        list.add(smsBlackWords);
                                        SmsCache.GROUP_AUTO_WORDS_SCREENTYPE.put(groupId, list);
                                        log.info("加载自动屏蔽词策略组:{},屏蔽词:{},屏蔽类型:{}成功", groupId, item, screenType);
                                    }
                                }
                            }
                        }
                    } else if (method.equals(METHOD.DELETE)) {
                        if (content == null || content.isEmpty()) {
                            SmsCache.GROUP_AUTO_WORDS_SCREENTYPE.remove(groupId);
                            String groupIds = SmsCache.SYS_STRATEGY_GROUP.get(groupType);
                            if (StringUtils.contains(groupIds, "," + groupId)) {
                                groupIds = groupIds.replace("," + groupId, "");
                            } else if (StringUtils.contains(groupIds, groupId + "")) {
                                groupIds = groupIds.replace(groupId + ",", "");
                            }
                            SmsCache.SYS_STRATEGY_GROUP.put(groupType, groupIds);
                            log.info("删除自动屏蔽词策略组【" + groupId + "】");
                            log.info("系统自动屏蔽词策略移除:{}", groupId);
                        } else {
                            CopyOnWriteArrayList<SmsBlackWords> list = SmsCache.GROUP_AUTO_WORDS_SCREENTYPE.get(groupId);
                            int flag = 1;
                            if (list != null && list.size() > 0) {
                                for (SmsBlackWords smsBlackWords : list) {
                                    if (smsBlackWords.getScreentype().equals(screenType) && StringUtils.equals(smsBlackWords.getWords(), content) && smsBlackWords.getGroup_id().equals(groupId)) {
                                        list.remove(smsBlackWords);
                                        SmsCache.GROUP_AUTO_WORDS_SCREENTYPE.put(groupId, list);
                                        flag = 0;
                                        log.info("删除自动屏蔽词策略组【" + groupId + "】屏蔽词【" + content + "】" + "】屏蔽类型【" + screenType + "】成功");
                                        break;
                                    }
                                }
                            }
                            if (flag == 1) {
                                log.info("删除自动屏蔽词策略组【" + groupId + "】屏蔽词【" + content + "】" + "】屏蔽类型【" + screenType + "】失败");
                            }
                        }
                    }
                    break;
                case ConstantSys.USER_STRATEGY_GROUP_SYS_BLACK_WORDS_CHECK:
                    if (method.equals(METHOD.ADD) || method.equals(METHOD.UPDATE) || method.equals(METHOD.RELOAD)) {
                        if (content == null || content.isEmpty()) {
                            SmsCache.GROUP_RELEASE_WORDS_SCREENTYPE.remove(groupId);
                            loadGroupReleaseWords1();
                            log.info("重新加载审核屏蔽词策略组【" + groupId + "】");
                        } else {
                            CopyOnWriteArrayList<SmsBlackWords> list = SmsCache.GROUP_RELEASE_WORDS_SCREENTYPE.get(groupId);
                            if (list == null) {
                                list = new CopyOnWriteArrayList<SmsBlackWords>();
                            }
                            String[] arrays = content.split(",");
                            for (String item : arrays) {
                                if (list.isEmpty()) {
                                    SmsBlackWords smsBlackWords = new SmsBlackWords();
                                    smsBlackWords.setWords(item);
                                    smsBlackWords.setScreentype(screenType);
                                    smsBlackWords.setGroup_id(groupId);
                                    list.add(smsBlackWords);
                                    SmsCache.GROUP_RELEASE_WORDS_SCREENTYPE.put(groupId, list);
                                    log.info("加载审核屏蔽词策略组:{},屏蔽词:{},屏蔽类型:{}成功", groupId, item, screenType);
                                } else {
                                    int falg = 1;
                                    for (SmsBlackWords blackWords : list) {
                                        if (blackWords.getScreentype().equals(screenType) && StringUtils.equals(blackWords.getWords(), item) && blackWords.getGroup_id().equals(groupId)) {
                                            falg = -1;
                                            result = HttpSubmitServer.FAIL;
                                            log.info("加载审核屏蔽词策略组【" + groupId + "】屏蔽词【" + item + "】失败，屏蔽词已存在");
                                            break;
                                        }
                                    }
                                    if (falg == 1) {
                                        SmsBlackWords smsBlackWords = new SmsBlackWords();
                                        smsBlackWords.setWords(item);
                                        smsBlackWords.setScreentype(screenType);
                                        smsBlackWords.setGroup_id(groupId);
                                        list.add(smsBlackWords);
                                        SmsCache.GROUP_RELEASE_WORDS_SCREENTYPE.put(groupId, list);
                                        log.info("加载审核屏蔽词策略组:{},屏蔽词:{},屏蔽类型:{}成功", groupId, item, screenType);
                                    }
                                }
                            }
                        }
                    } else if (method.equals(METHOD.DELETE)) {
                        if (content == null || content.isEmpty()) {
                            SmsCache.GROUP_RELEASE_WORDS_SCREENTYPE.remove(groupId);
                            String groupIds = SmsCache.SYS_STRATEGY_GROUP.get(groupType);
                            if (StringUtils.contains(groupIds, "," + groupId)) {
                                groupIds = groupIds.replace("," + groupId, "");
                            } else if (StringUtils.contains(groupIds, groupId + "")) {
                                groupIds = groupIds.replace(groupId + ",", "");
                            }
                            SmsCache.SYS_STRATEGY_GROUP.put(groupType, groupIds);
                            log.info("删除审核屏蔽词策略组【" + groupId + "】");
                            log.info("系统审核屏蔽词策略移除:{}", groupId);
                        } else {
                            CopyOnWriteArrayList<SmsBlackWords> list = SmsCache.GROUP_RELEASE_WORDS_SCREENTYPE.get(groupId);
                            int flag = 1;
                            if (list != null && list.size() > 0) {
                                for (SmsBlackWords smsBlackWords : list) {
                                    if (smsBlackWords.getScreentype().equals(screenType) && StringUtils.equals(smsBlackWords.getWords(), content) && smsBlackWords.getGroup_id().equals(groupId)) {
                                        list.remove(smsBlackWords);
                                        SmsCache.GROUP_AUTO_WORDS_SCREENTYPE.put(groupId, list);
                                        flag = 0;
                                        log.info("删除审核屏蔽词策略组【" + groupId + "】屏蔽词【" + content + "】" + "】屏蔽类型【" + screenType + "】成功");
                                        break;
                                    }
                                }
                            }
                            if (flag == 1) {
                                log.info("删除审核屏蔽词策略组【" + groupId + "】屏蔽词【" + content + "】" + "】屏蔽类型【" + screenType + "】失败");
                            }
                        }
                    }
                    break;
                case ConstantSys.USER_STRATEGY_GROUP_SYS_BLACK_MOBILE:
                    if (method.equals(METHOD.ADD) || method.equals(METHOD.UPDATE) || method.equals(METHOD.RELOAD)) {
                        if (content == null || content.isEmpty()) {
                            SmsCache.GROUP_BLACK_MOBILE.remove(groupId);
                            loadGroupBlackMobile();
                            log.info("重新加载黑名单策略组【" + groupId + "】");
                        } else {
                            Map<String, List<Long>> groupMap = SmsCache.GROUP_BLACK_MOBILE.get(groupId);
                            if (groupMap == null) {
                                groupMap = new HashMap<String, List<Long>>();
                            }

                            String[] arrays = content.split(",");
                            for (String item : arrays) {
                                String key = item.substring(0, ConstantSys.MOBILE_POSITION);
                                List<Long> mobileList = groupMap.get(key);
                                if (mobileList == null) {
                                    mobileList = new ArrayList<Long>();
                                }

                                Long mobile = Long.parseLong(item);
                                if (mobileList.isEmpty() || !mobileList.contains(mobile)) {
                                    mobileList.add(mobile);
                                }

                                groupMap.put(key, mobileList);
                            }
                            SmsCache.GROUP_BLACK_MOBILE.put(groupId, groupMap);
                            log.info("加载黑名单策略组【" + groupId + "】手机号码【" + content + "】");
                        }
                    } else if (method.equals(METHOD.DELETE)) {
                        if (content == null || content.isEmpty()) {
                            SmsCache.GROUP_BLACK_MOBILE.remove(groupId);
                            log.info("删除黑名单策略组【" + groupId + "】");
                        } else {
                            String key = content.substring(0, ConstantSys.MOBILE_POSITION);
                            Long mobile = Long.parseLong(content);
                            Map<String, List<Long>> groupMap = SmsCache.GROUP_BLACK_MOBILE.get(groupId);
                            if (groupMap != null && groupMap.containsKey(key)) {
                                List<Long> mobileList = groupMap.get(key);
                                if (mobileList != null && mobileList.contains(mobile)) {
                                    mobileList.remove(mobile);
                                }
                                groupMap.put(key, mobileList);
                            }
                            SmsCache.GROUP_BLACK_MOBILE.put(groupId, groupMap);
                            log.info("删除黑名单策略组【" + groupId + "】手机号码【" + content + "】");
                        }
                    }
                    break;
                case ConstantSys.USER_STRATEGY_GROUP_SYS_WHITE_MOBILE:
                    if (method.equals(METHOD.ADD) || method.equals(METHOD.UPDATE) || method.equals(METHOD.RELOAD)) {
                        if (content == null || content.isEmpty()) {
                            SmsCache.GROUP_WHITE_MOBILE.remove(groupId);
                            loadGroupBlackMobile();
                            log.info("重新加载白名单策略组【" + groupId + "】");
                        } else {
                            Map<String, List<Long>> groupMap = SmsCache.GROUP_WHITE_MOBILE.get(groupId);
                            if (groupMap == null) {
                                groupMap = new HashMap<String, List<Long>>();
                            }
                            String[] arrays = content.split(",");
                            for (String item : arrays) {
                                String key = item.substring(0, ConstantSys.MOBILE_POSITION);
                                List<Long> mobileList = groupMap.get(key);
                                if (mobileList == null) {
                                    mobileList = new ArrayList<Long>();
                                }

                                Long mobile = Long.parseLong(item);
                                if (mobileList.isEmpty() || !mobileList.contains(mobile)) {
                                    mobileList.add(mobile);
                                }

                                groupMap.put(key, mobileList);
                            }
                            SmsCache.GROUP_WHITE_MOBILE.put(groupId, groupMap);
                            log.info("加载白名单策略组【" + groupId + "】手机号码【" + content + "】");
                        }
                    } else if (method.equals(METHOD.DELETE)) {
                        if (content == null || content.isEmpty()) {
                            SmsCache.GROUP_WHITE_MOBILE.remove(groupId);
                            log.info("删除白名单策略组【" + groupId + "】");
                        } else {
                            String key = content.substring(0, ConstantSys.MOBILE_POSITION);
                            Long mobile = Long.parseLong(content);
                            Map<String, List<Long>> groupMap = SmsCache.GROUP_WHITE_MOBILE.get(groupId);
                            if (groupMap != null && groupMap.containsKey(key)) {
                                List<Long> mobileList = groupMap.get(key);
                                if (mobileList != null && mobileList.contains(mobile)) {
                                    mobileList.remove(mobile);
                                }
                                groupMap.put(key, mobileList);
                            }
                            SmsCache.GROUP_WHITE_MOBILE.put(groupId, groupMap);
                            log.info("删除白名单策略组【" + groupId + "】手机号码【" + content + "】");
                        }
                    }
                    break;
                case ConstantSys.USER_STRATEGY_GROUP_SYS_WHITE_SIGN:
                    if (method.equals(METHOD.ADD) || method.equals(METHOD.UPDATE) || method.equals(METHOD.RELOAD)) {
                        if (content == null || content.isEmpty()) {
                            SmsCache.GROUP_WHITE_SIGN.remove(groupId);
                            loadGroupWhiteSign();
                            log.info("重新加载白签名策略组【" + groupId + "】");
                        } else {
                            List<String> list = SmsCache.GROUP_WHITE_SIGN.get(groupId);
                            if (list == null) {
                                list = new ArrayList<String>();
                            }
                            String[] arrays = content.split(",");
                            for (String item : arrays) {
                                if (list.isEmpty() || !list.contains(item)) {
                                    list.add(item);
                                }
                            }
                            SmsCache.GROUP_WHITE_SIGN.put(groupId, list);
                            log.info("加载白签名策略组【" + groupId + "】签名【" + content + "】");
                        }
                    } else if (method.equals(METHOD.DELETE)) {
                        if (content == null || content.isEmpty()) {
                            SmsCache.GROUP_WHITE_SIGN.remove(groupId);
                            log.info("删除白签名策略组【" + groupId + "】");
                        } else {
                            List<String> list = SmsCache.GROUP_WHITE_SIGN.get(groupId);
                            if (list != null && list.contains(content)) {
                                list.remove(content);
                                SmsCache.GROUP_WHITE_SIGN.put(groupId, list);
                                log.info("删除白签名策略组【" + groupId + "】签名【" + content + "】");
                            }
                        }
                    }
                    break;
                case ConstantSys.USER_STRATEGY_GROUP_SYS_BLACK_SIGN:
                    if (method.equals(METHOD.ADD) || method.equals(METHOD.UPDATE) || method.equals(METHOD.RELOAD)) {
                        if (content == null || content.isEmpty()) {
                            SmsCache.GROUP_BLACK_SIGN.remove(groupId);
                            loadGroupBlackSign();
                            log.info("重新加载黑签名策略组【" + groupId + "】");
                        } else {
                            List<String> list = SmsCache.GROUP_BLACK_SIGN.get(groupId);
                            if (list == null) {
                                list = new ArrayList<String>();
                            }

                            String[] arrays = content.split(",");
                            for (String item : arrays) {
                                if (list.isEmpty() || !list.contains(item)) {
                                    list.add(item);
                                }
                            }
                            SmsCache.GROUP_BLACK_SIGN.put(groupId, list);
                            log.info("加载黑签名策略组【" + groupId + "】签名【" + content + "】");
                        }
                    } else if (method.equals(METHOD.DELETE)) {
                        if (content == null || content.isEmpty()) {
                            SmsCache.GROUP_BLACK_SIGN.remove(groupId);
                            log.info("删除黑签名策略组【" + groupId + "】");
                        } else {
                            List<String> list = SmsCache.GROUP_BLACK_SIGN.get(groupId);
                            if (list != null && list.contains(content)) {
                                list.remove(content);
                                SmsCache.GROUP_BLACK_SIGN.put(groupId, list);
                                log.info("删除黑签名策略组【" + groupId + "】签名【" + content + "】");
                            }
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("修改策略组缓存信息异常",
                    "[StrategyGroupCache.excuteStrategyGroup(" + method + "," + groupType + "," + groupId + "," + content + ") ]" + LogInfo.getTrace(e));
            result = HttpSubmitServer.FAIL;
        }
        return result;
    }

    public void excuteUserStrategyGroup(Integer method, Integer uid) {
        try {
            if (method.equals(METHOD.ADD) || method.equals(METHOD.UPDATE) || method.equals(METHOD.RELOAD)) {
                deleteUserStrategyGroup(uid);
                loadUserStrategyGroup(uid);
            } else if (method.equals(METHOD.DELETE)) {
                deleteUserStrategyGroup(uid);
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("修改用户策略组缓存信息异常", "[StrategyGroupCache.excuteUserStrategyGroup(" + method + "," + uid + ") ]" + LogInfo.getTrace(e));
        }
    }

    /**
     * 获取系统策略组
     *
     * @return
     */
    public Map<Integer, String> getSysStrategyGroup() {
        try {
            return SmsCache.SYS_STRATEGY_GROUP;
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("获取用户的策略组缓存信息异常", "[StrategyGroupCache.getUserStrategyGroup ]" + LogInfo.getTrace(e));
        }
        return null;
    }

    /**
     * 加载系统策略组
     */
    public void loadSysStrategyGroup() {
        try {
            List<Map<String, Object>> userStrategyGroupList = SysCacheDao.getInstance().findSmsStrategyGroup();
            Map<Integer, String> currentMap = new ConcurrentHashMap<>();
            if (null != userStrategyGroupList) {
                int i = 1;
                for (Map<String, Object> map : userStrategyGroupList) {
                    Integer type = (Integer) map.get("type");
                    String id = map.get("id").toString();
                    if (currentMap == null) {
                        currentMap = new HashMap<>();
                    }
                    if (currentMap.containsKey(type)) {
                        String currentId = currentMap.get(type);
                        currentMap.put(type, currentId + "," + id);
                    } else {
                        currentMap.put(type, id);
                    }
                    i++;
                }
                SmsCache.SYS_STRATEGY_GROUP = currentMap;
                log.info("系统策略组加载【" + i + "】个");
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("加载系统策略组缓存信息异常", "[StrategyGroupCache.loadUserStrategyGroup]" + LogInfo.getTrace(e));
        }
    }

    /****
     * 加载用户策略组
     *
     * @param uid
     */
    public void loadUserStrategyGroup(Integer uid) {
        try {
            List<Map<String, Object>> userStrategyGroupList = SysCacheDao.getInstance().findSmsUserStrategyRelation(uid);
            Map<Integer, Map<Integer, String>> currentMap = new ConcurrentHashMap<Integer, Map<Integer, String>>();
            if (null != userStrategyGroupList) {
                Map<Integer, String> subMap = null;
                int i = 1;
                for (Map<String, Object> map : userStrategyGroupList) {
                    Integer key = (Integer) map.get("uid");
                    Integer type = (Integer) map.get("type");
                    String groupId = map.get("group_id").toString();
                    subMap = currentMap.get(key);
                    if (subMap == null) {
                        subMap = new HashMap<Integer, String>();
                    }

                    if (subMap.containsKey(type)) {
                        String currentGroupId = subMap.get(type);
                        subMap.put(type, currentGroupId + "," + groupId);
                    } else {
                        subMap.put(type, groupId);
                    }
                    i++;
                    currentMap.put(key, subMap);
                }
                if (uid == null || uid == 0) {
                    SmsCache.USER_STRATEGY_GROUP = currentMap;
                } else {
                    SmsCache.USER_STRATEGY_GROUP.put(uid, currentMap.get(uid));
                }
                log.info("用户策略组加载【" + i + "】个");
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("加载用户策略组缓存信息异常", "[StrategyGroupCache.loadUserStrategyGroup(" + uid + ") ]" + LogInfo.getTrace(e));
        }
    }

    public void reloadUserStrategyGroup() {
        this.loadUserStrategyGroup(0);
    }

    /***
     * 根据UID获取用户策略组
     *
     * @param uid
     * @return
     */
    public Map<Integer, String> getUserStrategyGroup(Integer uid) {
        try {
            return SmsCache.USER_STRATEGY_GROUP.get(uid);
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("获取用户的策略组缓存信息异常", "[StrategyGroupCache.getUserStrategyGroup(" + uid + ") ]" + LogInfo.getTrace(e));
        }
        return null;
    }

    public void deleteUserStrategyGroup(Integer uid) {
        try {
            if (SmsCache.USER_STRATEGY_GROUP.containsKey(uid)) {
                SmsCache.USER_STRATEGY_GROUP.remove(uid);
                log.info("删除用户策略组; uid:" + uid);
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("删除用户策略组缓存信息异常", "[StrategyGroupCache.deleteUserStrategyGroup(" + uid + ") ]" + LogInfo.getTrace(e));
        }
    }

    public void loadStrategyGroup() {
        try {
            /**
             * 加载系统策略
             */
            SmsCache.SYS_STRATEGY_GROUP.clear();
            loadSysStrategyGroup();

            SmsCache.GROUP_WHITE_MOBILE.clear();
            loadGroupWhiteMobile();

            SmsCache.GROUP_BLACK_MOBILE.clear();
            loadGroupBlackMobile();

            SmsCache.GROUP_WHITE_SIGN.clear();
            loadGroupWhiteSign();

            SmsCache.GROUP_BLACK_SIGN.clear();
            loadGroupBlackSign();

            /**
             * 加载审核屏蔽词
             */
            SmsCache.GROUP_RELEASE_WORDS_SCREENTYPE.clear();
            loadGroupReleaseWords1();

            /**
             * 加载自动屏蔽词
             */
            SmsCache.GROUP_AUTO_WORDS_SCREENTYPE.clear();
            loadGroupAutoWords1();
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("加载策略组缓存信息异常", "[StrategyGroupCache.loadStrategyGroup() ]" + LogInfo.getTrace(e));
        }
    }

    public void reloadStrategyGroup() {
        try {
            loadGroupWhiteMobile();

            loadGroupBlackMobile();

            loadGroupWhiteSign();

            loadGroupBlackSign();

            loadGroupReleaseWords1();

            loadGroupAutoWords1();
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("重新加载策略组缓存信息异常", "[StrategyGroupCache.reloadStrategyGroup() ]" + LogInfo.getTrace(e));
        }
    }


    /**
     * 加载自动屏蔽词
     */
    private void loadGroupAutoWords1() {
        try {
            Map<Integer, CopyOnWriteArrayList<SmsBlackWords>> currentMap = null;
            if (SmsCache.GROUP_AUTO_WORDS_SCREENTYPE_INIT) {
                currentMap = (Map<Integer, CopyOnWriteArrayList<SmsBlackWords>>) EhcacheUtil.getInstance().get("group", "GROUP_AUTO_WORDS_SCREENTYPE");
                SmsCache.GROUP_AUTO_WORDS_SCREENTYPE_INIT = false;
            }
            if (currentMap == null || currentMap.size() == 0) {
                currentMap = new ConcurrentHashMap<>();
                List<Map<String, Object>> autoWordsList = SysCacheDao.getInstance().findStrategyGourp(ConstantSys.USER_STRATEGY_GROUP_SYS_BLACK_WORDS_AUTO);
                if (null != autoWordsList) {
                    CopyOnWriteArrayList<SmsBlackWords> smsBlackWordsList = null;
                    for (Map<String, Object> map : autoWordsList) {
                        int groupId = Integer.valueOf(map.get("group_id").toString());
                        String words = map.get("words").toString();
                        int screenType = Integer.valueOf(map.get("screenType") == null ? "0" : map.get("screenType").toString());
                        smsBlackWordsList = currentMap.get(groupId);
                        if (smsBlackWordsList != null) {
                            SmsBlackWords smsBlackWords = new SmsBlackWords();
                            smsBlackWords.setGroup_id(groupId);
                            smsBlackWords.setScreentype(screenType);
                            smsBlackWords.setWords(words);
                            smsBlackWordsList.add(smsBlackWords);
                            currentMap.put(groupId, smsBlackWordsList);
                        } else {
                            smsBlackWordsList = new CopyOnWriteArrayList<>();
                            SmsBlackWords smsBlackWords = new SmsBlackWords();
                            smsBlackWords.setGroup_id(groupId);
                            smsBlackWords.setScreentype(screenType);
                            smsBlackWords.setWords(words);
                            smsBlackWordsList.add(smsBlackWords);
                            currentMap.put(groupId, smsBlackWordsList);
                        }
                    }
                    SmsCache.GROUP_AUTO_WORDS_SCREENTYPE = currentMap;
                }

            } else {
                SmsCache.GROUP_AUTO_WORDS_SCREENTYPE = currentMap;

            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("加载策略组-自动屏蔽词缓存信息异常", "[StrategyGroupCache.loadGroupAutoWords() ]" + LogInfo.getTrace(e));
        }
    }


    /**
     * 加载审核屏蔽词
     */
    private void loadGroupReleaseWords1() {
        try {
            Map<Integer, CopyOnWriteArrayList<SmsBlackWords>> currentMap = null;
            if (SmsCache.GROUP_RELEASE_WORDS_SCREENTYPE_INIT) {
                currentMap = (Map<Integer, CopyOnWriteArrayList<SmsBlackWords>>) EhcacheUtil.getInstance().get("group", "GROUP_RELEASE_WORDS_SCREENTYPE");
                SmsCache.GROUP_RELEASE_WORDS_SCREENTYPE_INIT = false;
            }
            if (currentMap == null || currentMap.size() == 0) {
                currentMap = new ConcurrentHashMap<>();
                List<Map<String, Object>> releaseWordsList = SysCacheDao.getInstance().findStrategyGourp(ConstantSys.USER_STRATEGY_GROUP_SYS_BLACK_WORDS_CHECK);
                if (null != releaseWordsList) {
                    CopyOnWriteArrayList<SmsBlackWords> smsBlackWordsList = null;
                    for (Map<String, Object> map : releaseWordsList) {
                        int groupId = Integer.valueOf(map.get("group_id").toString());
                        String words = map.get("words").toString();
                        int screenType = Integer.valueOf(map.get("screenType") == null ? "0" : map.get("screenType").toString());
                        smsBlackWordsList = currentMap.get(groupId);
                        if (smsBlackWordsList != null) {
                            SmsBlackWords smsBlackWords = new SmsBlackWords();
                            smsBlackWords.setGroup_id(groupId);
                            smsBlackWords.setScreentype(screenType);
                            smsBlackWords.setWords(words);
                            smsBlackWordsList.add(smsBlackWords);
                            currentMap.put(groupId, smsBlackWordsList);
                        } else {
                            smsBlackWordsList = new CopyOnWriteArrayList<>();
                            SmsBlackWords smsBlackWords = new SmsBlackWords();
                            smsBlackWords.setGroup_id(groupId);
                            smsBlackWords.setScreentype(screenType);
                            smsBlackWords.setWords(words);
                            smsBlackWordsList.add(smsBlackWords);
                            currentMap.put(groupId, smsBlackWordsList);
                        }
                    }
                    SmsCache.GROUP_RELEASE_WORDS_SCREENTYPE = currentMap;
                }
            } else {
                SmsCache.GROUP_RELEASE_WORDS_SCREENTYPE = currentMap;

            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("加载策略组-审核屏蔽词缓存信息异常", "[StrategyGroupCache.loadGroupReleaseWords() ]" + LogInfo.getTrace(e));
        }
    }


    private void loadGroupBlackSign() {
        try {
            Map<Integer, List<String>> currentMap = null;
            if (SmsCache.GROUP_BLACK_SIGN_INIT) {
                currentMap = (Map<Integer, List<String>>) EhcacheUtil.getInstance().get("group", "GROUP_BLACK_SIGN");
                SmsCache.GROUP_BLACK_SIGN_INIT = false;
            }
            if (currentMap == null || currentMap.size() == 0) {
                currentMap = new ConcurrentHashMap<>();
                List<Map<String, Object>> blackSignList = SysCacheDao.getInstance().findStrategyGourp(ConstantSys.USER_STRATEGY_GROUP_SYS_BLACK_SIGN);
                if (null != blackSignList) {
                    List<String> signList = null;
                    for (Map<String, Object> map : blackSignList) {
                        Integer groupId = Integer.valueOf(map.get("group_id").toString());
                        String sign = map.get("sign").toString();
                        signList = currentMap.get(groupId);
                        if (signList == null) {
                            signList = new ArrayList<String>();
                        }
                        if (!signList.contains(sign)) {
                            signList.add(sign);
                        }
                        currentMap.put(groupId, signList);
                    }
                    SmsCache.GROUP_BLACK_SIGN = currentMap;
                }
            } else {
                SmsCache.GROUP_BLACK_SIGN = currentMap;
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("加载策略组-黑签名缓存信息异常", "[StrategyGroupCache.loadGroupBlackSign() ]" + LogInfo.getTrace(e));
        }
    }

    private void loadGroupWhiteSign() {
        try {
            Map<Integer, List<String>> currentMap = null;
            if (SmsCache.GROUP_WHITE_SIGN_INIT) {
                currentMap = (Map<Integer, List<String>>) EhcacheUtil.getInstance().get("group", "GROUP_WHITE_SIGN");
                SmsCache.GROUP_WHITE_SIGN_INIT = false;
            }
            if (currentMap == null || currentMap.size() == 0) {
                currentMap = new ConcurrentHashMap<>();
                List<Map<String, Object>> whiteSignList = SysCacheDao.getInstance().findStrategyGourp(ConstantSys.USER_STRATEGY_GROUP_SYS_WHITE_SIGN);
                if (null != whiteSignList) {
                    List<String> signList = null;
                    for (Map<String, Object> map : whiteSignList) {
                        Integer groupId = Integer.valueOf(map.get("group_id").toString());
                        String sign = map.get("sign").toString();
                        signList = currentMap.get(groupId);
                        if (signList == null) {
                            signList = new ArrayList<String>();
                        }
                        if (!signList.contains(sign)) {
                            signList.add(sign);
                        }
                        currentMap.put(groupId, signList);

                    }
                    SmsCache.GROUP_WHITE_SIGN = currentMap;
                }
            } else {
                SmsCache.GROUP_WHITE_SIGN = currentMap;
            }

        } catch (Exception e) {
            LogInfo.getLog().errorAlert("加载策略组-白签名缓存信息异常", "[StrategyGroupCache.loadGroupWhiteSign() ]" + LogInfo.getTrace(e));
        }
    }

    private void loadGroupBlackMobile() {
        try {
            Map<Integer, Map<String, List<Long>>> currentMap = null;
            if (SmsCache.GROUP_BLACK_MOBILE_INIT) {
                currentMap = (Map<Integer, Map<String, List<Long>>>) EhcacheUtil.getInstance().get("group", "GROUP_BLACK_MOBILE");
                SmsCache.GROUP_BLACK_MOBILE_INIT = false;
            }
            if (currentMap == null || currentMap.size() == 0) {
                currentMap = new ConcurrentHashMap<>();
                List<Map<String, Object>> balckMobileList = SysCacheDao.getInstance().findStrategyGourp(ConstantSys.USER_STRATEGY_GROUP_SYS_BLACK_MOBILE);
                if (null != balckMobileList) {
                    Map<String, List<Long>> subMap = null;
                    List<Long> mobileList = null;
                    for (Map<String, Object> map : balckMobileList) {
                        int groupId = Integer.valueOf(map.get("group_id").toString());
                        String mobile = map.get("mobile").toString();
                        String key = mobile.substring(0, ConstantSys.MOBILE_POSITION);
                        subMap = currentMap.get(groupId);
                        if (subMap == null) {
                            subMap = new HashMap<String, List<Long>>();
                        }
                        mobileList = subMap.get(key);
                        if (mobileList == null) {
                            mobileList = new ArrayList<Long>();
                        }

                        if (!mobileList.contains(Long.valueOf(mobile))) {
                            mobileList.add(Long.valueOf(mobile));
                        }

                        subMap.put(key, mobileList);
                        currentMap.put(groupId, subMap);
                    }
                    SmsCache.GROUP_BLACK_MOBILE = currentMap;
                }
            } else {
                SmsCache.GROUP_BLACK_MOBILE = currentMap;

            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("加载策略组-黑名单缓存信息异常", "[StrategyGroupCache.loadGroupBlackMobile() ]" + LogInfo.getTrace(e));
        }
    }

    private void loadGroupWhiteMobile() {
        try {
            Map<Integer, Map<String, List<Long>>> currentMap = null;
            if (SmsCache.GROUP_WHITE_MOBILE_INIT) {
                currentMap = (Map<Integer, Map<String, List<Long>>>) EhcacheUtil.getInstance().get("group", "GROUP_WHITE_MOBILE");
                SmsCache.GROUP_WHITE_MOBILE_INIT = false;
            }
            if (currentMap == null || currentMap.size() == 0) {
                currentMap = new ConcurrentHashMap<>();
                List<Map<String, Object>> whiteMobileList = SysCacheDao.getInstance().findStrategyGourp(ConstantSys.USER_STRATEGY_GROUP_SYS_WHITE_MOBILE);
                if (null != whiteMobileList) {
                    Map<String, List<Long>> subMap = null;
                    List<Long> mobileList = null;
                    for (Map<String, Object> map : whiteMobileList) {
                        int groupId = Integer.valueOf(map.get("group_id").toString());
                        String mobile = map.get("mobile").toString();
                        String key = mobile.substring(0, ConstantSys.MOBILE_POSITION);
                        subMap = currentMap.get(groupId);
                        if (subMap == null) {
                            subMap = new HashMap<String, List<Long>>();
                        }
                        mobileList = subMap.get(key);
                        if (mobileList == null) {
                            mobileList = new ArrayList<Long>();
                        }

                        if (!mobileList.contains(Long.valueOf(mobile))) {
                            mobileList.add(Long.valueOf(mobile));
                        }
                        subMap.put(key, mobileList);
                        currentMap.put(groupId, subMap);
                    }
                    SmsCache.GROUP_WHITE_MOBILE = currentMap;
                }
            } else {
                SmsCache.GROUP_WHITE_MOBILE = currentMap;
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("加载策略组-白名单缓存信息异常", "[StrategyGroupCache.loadGroupWhiteMobile() ]" + LogInfo.getTrace(e));
        }
    }

    public List<String> getGroupWhiteSign(Integer type) {
        try {
            return SmsCache.GROUP_WHITE_SIGN.get(type);
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("获取策略组-白签名信息异常", "[StrategyGroupCache.getGroupWhiteSign(" + type + ") ]" + LogInfo.getTrace(e));
        }
        return null;
    }

    public List<String> getGroupBlackSign(Integer type) {
        try {
            return SmsCache.GROUP_BLACK_SIGN.get(type);
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("获取策略组-黑签名信息异常", "[StrategyGroupCache.getGroupBlackSign(" + type + ") ]" + LogInfo.getTrace(e));
        }
        return null;
    }

    public List<Long> getGroupWhiteMobile(Integer type, String key) {
        try {
            if (SmsCache.GROUP_WHITE_MOBILE.get(type) == null) {
                return null;
            }
            return SmsCache.GROUP_WHITE_MOBILE.get(type).get(key);
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("获取策略组-白名单信息异常", "[StrategyGroupCache.getGroupWhiteMobile(" + type + "," + key + ") ]" + LogInfo.getTrace(e));
        }
        return null;
    }

    public List<Long> getGroupBlackMobile(Integer type, String key) {
        try {
            if (SmsCache.GROUP_BLACK_MOBILE.get(type) == null) {
                return null;
            }
            return SmsCache.GROUP_BLACK_MOBILE.get(type).get(key);
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("获取策略组-黑名单信息异常", "[StrategyGroupCache.getGroupBlackMobile(" + type + "," + key + ") ]" + LogInfo.getTrace(e));
        }
        return null;
    }

    public List<SmsBlackWords> getGroupReleaseWords1(Integer groupId) {
        try {
            if (SmsCache.GROUP_RELEASE_WORDS_SCREENTYPE.get(groupId) == null) {
                return null;
            }
            return SmsCache.GROUP_RELEASE_WORDS_SCREENTYPE.get(groupId);
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("获取策略组-审核屏蔽词信息异常", "[StrategyGroupCache.getGroupReleaseWords(" + groupId + ") ]" + LogInfo.getTrace(e));
        }
        return null;
    }

    /**
     * 获得缓存自动屏蔽词
     *
     * @param groupId
     * @return
     */
    public List<SmsBlackWords> getGroupAutoWords1(Integer groupId) {
        try {
            if (SmsCache.GROUP_AUTO_WORDS_SCREENTYPE.get(groupId) == null) {
                return null;
            }
            return SmsCache.GROUP_AUTO_WORDS_SCREENTYPE.get(groupId);
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("获取策略组-自动屏蔽词信息异常", "[StrategyGroupCache.getGroupAutoWords(" + groupId + ") ]" + LogInfo.getTrace(e));
        }
        return null;
    }


    /****
     * 校验白签名
     */
    public boolean checkStrategyWhiteSign(int uid, String sign) {
        try {
            Map<Integer, String> userStrategyGroupMap = getUserStrategyGroup(uid);
            if (userStrategyGroupMap != null && userStrategyGroupMap.containsKey(ConstantSys.USER_STRATEGY_GROUP_SYS_WHITE_SIGN)) {
                String groupIds = userStrategyGroupMap.get(ConstantSys.USER_STRATEGY_GROUP_SYS_WHITE_SIGN);
                if (StringUtils.isBlank(groupIds)) {
                    return false;
                }

                String[] groupIdArray = groupIds.split(",");
                for (String groupId : groupIdArray) {
                    List<String> list = getGroupWhiteSign(Integer.valueOf(groupId));
                    if (list != null && list.contains(sign)) {
                        log.info("触发策略组-白签名; uid:" + uid + " ,sign:" + sign);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("校验策略组-白签名信息异常", "[StrategyGroupCache.checkStrategyWhiteSign(" + uid + "," + sign + ") ]" + LogInfo.getTrace(e));
        }
        return false;
    }

    /***
     * 校验白名单
     */
    public boolean checkStrategyWhiteMobile(Integer uid, Long mobile) {
        try {
            Map<Integer, String> userStrategyGroupMap = getUserStrategyGroup(uid);
            if (userStrategyGroupMap != null && userStrategyGroupMap.containsKey(ConstantSys.USER_STRATEGY_GROUP_SYS_WHITE_MOBILE)) {
                String groupIds = userStrategyGroupMap.get(ConstantSys.USER_STRATEGY_GROUP_SYS_WHITE_MOBILE);
                if (StringUtils.isBlank(groupIds)) {
                    return false;
                }

                String[] groupIdArray = groupIds.split(",");
                for (String groupId : groupIdArray) {
                    String key = mobile.toString().substring(0, ConstantSys.MOBILE_POSITION);
                    List<Long> list = getGroupWhiteMobile(Integer.valueOf(groupId), key);
                    if (list != null && list.contains(mobile)) {
                        log.info("触发策略组-白名单; uid:" + uid + " ,mobile:" + mobile);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("校验策略组-白名单信息异常", "[StrategyGroupCache.checkStrategyWhiteMobile(" + uid + "," + mobile + ") ]" + LogInfo.getTrace(e));
        }
        return false;
    }

    /***
     * 校验黑名单
     */
    public boolean checkStrategyBlackMobile(Integer uid, Long mobile, Map<Integer, String> userStrategyGroupMap) {
        try {
            if (userStrategyGroupMap != null && userStrategyGroupMap.containsKey(ConstantSys.USER_STRATEGY_GROUP_SYS_BLACK_MOBILE)) {
                String groupIds = userStrategyGroupMap.get(ConstantSys.USER_STRATEGY_GROUP_SYS_BLACK_MOBILE);
                if (StringUtils.isBlank(groupIds)) {
                    return false;
                }

                String[] groupIdArray = groupIds.split(",");
                for (String groupId : groupIdArray) {
                    String key = mobile.toString().substring(0, ConstantSys.MOBILE_POSITION);
                    List<Long> list = getGroupBlackMobile(Integer.valueOf(groupId), key);
                    if (list != null && list.contains(mobile)) {
                        log.info("触发策略组-黑名单; uid:" + uid + " ,mobile:" + mobile);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("校验策略组-黑名单信息异常", "[StrategyGroupCache.checkStrategyBlackMobile(" + uid + "," + mobile + ") ]" + LogInfo.getTrace(e));
        }
        return false;
    }


    /***
     * 校验自动屏蔽词
     */
    public String checkStrategyAutoWords1(Integer uid, String content, Map<Integer, String> userStrategyGroupMap, SendingVo vo) {
        try {
            if (userStrategyGroupMap != null && userStrategyGroupMap.containsKey(ConstantSys.USER_STRATEGY_GROUP_SYS_BLACK_WORDS_AUTO)) {
                String groupIds = userStrategyGroupMap.get(ConstantSys.USER_STRATEGY_GROUP_SYS_BLACK_WORDS_AUTO);
                if (StringUtils.isBlank(groupIds)) {
                    return null;
                }
                String[] groupIdArray = groupIds.split(",");
                String words = null;
                for (String groupId : groupIdArray) {
                    List<SmsBlackWords> list = getGroupAutoWords1(Integer.valueOf(groupId));
                    if (list == null || list.isEmpty()) {
                        continue;
                    }
                    for (SmsBlackWords s : list) {
                        words = MatchUtil.getInstance().match(content, s.getWords());
                        if (words != null) {
                            log.info("触发自动屏蔽词:{};mobile:{};uid:{},类型:{},content:{}", words, vo.getMobile(), uid, s.getScreentype(), vo.getContent());
                            return words;
                        }
                    }
                }
                return words;
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("校验策略组-自动屏蔽词信息异常", "[StrategyGroupCache.checkStrategyAutoWords(" + uid + "," + content + ") ]" + LogInfo.getTrace(e));
        }
        return null;
    }


    /***
     * 校验审核屏蔽词
     */
    public String checkStrategyReleaseWords1(Integer uid, String content, Map<Integer, String> userStrategyGroupMap, SendingVo vo) {
        try {
            if (userStrategyGroupMap != null && userStrategyGroupMap.containsKey(ConstantSys.USER_STRATEGY_GROUP_SYS_BLACK_WORDS_CHECK)) {
                String groupIds = userStrategyGroupMap.get(ConstantSys.USER_STRATEGY_GROUP_SYS_BLACK_WORDS_CHECK);
                if (StringUtils.isBlank(groupIds)) {
                    return null;
                }
                if (!NightUtil.day()) {
                    groupIds = groupIds + "," + ConstantSys.USER_WORDS_CHECK_NIGHT;
                }
                String[] groupIdArray = groupIds.split(",");
                String words = null;
                for (String groupId : groupIdArray) {
                    List<SmsBlackWords> list = getGroupReleaseWords1(Integer.valueOf(groupId));
                    if (list == null || list.isEmpty()) {
                        continue;
                    }
                    for (SmsBlackWords s : list) {
                        words = MatchUtil.getInstance().match(content, s.getWords());
                        if (words != null) {
                            vo.setScreenType(s.getScreentype());
                            log.info("触发审核屏蔽词:{};mobile:{};uid:{},类型:{},content:{}", words, vo.getMobile(), uid, s.getScreentype(), vo.getContent());
                            return words;
                        }
                    }
                }
                return words;
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("校验策略组-审核屏蔽词信息异常", "[StrategyGroupCache.checkStrategyReleaseWords(" + uid + "," + content + ") ]" + LogInfo.getTrace(e));
        }
        return null;
    }


    /****
     * 校验黑签名
     */
    public String checkStrategyBlackSign(int uid, String sign, Long mobile) {
        try {
            Map<Integer, String> userStrategyGroupMap = getUserStrategyGroup(uid);
            if (userStrategyGroupMap != null && userStrategyGroupMap.containsKey(ConstantSys.USER_STRATEGY_GROUP_SYS_BLACK_SIGN)) {
                String groupIds = userStrategyGroupMap.get(ConstantSys.USER_STRATEGY_GROUP_SYS_BLACK_SIGN);
                if (StringUtils.isBlank(groupIds)) {
                    return null;
                }

                String[] groupIdArray = groupIds.split(",");
                for (String groupId : groupIdArray) {
                    List<String> list = getGroupBlackSign(Integer.valueOf(groupId));
                    if (list == null || list.isEmpty()) {
                        continue;
                    }
                    for (String s : list) {
                        if (s.equalsIgnoreCase(sign)) {
                            log.info("触发策略组-黑签名:uid:{},sign:{},mobile:{}", uid, sign, mobile);
                            return sign;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("校验策略组-黑签名信息异常", "[StrategyGroupCache.checkStrategyBlackSign(" + uid + "," + sign + ") ]" + LogInfo.getTrace(e));
        }
        return null;
    }

}
