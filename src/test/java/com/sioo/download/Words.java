package com.sioo.download;

import com.sioo.dao.SysCacheDao;
import com.sioo.hy.cmpp.vo.SendingVo;
import com.sioo.hy.cmpp.vo.SmsBlackWords;
import com.sioo.log.LogInfo;
import com.sioo.util.ConstantSys;
import com.sioo.util.EhcacheUtil;
import com.sioo.util.MatchUtil;
import com.sioo.util.SmsCache;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by morrigan on 2017/5/16.
 */
public class Words {

    private static Logger log = LoggerFactory.getLogger(Words.class);


    @Test
    public void test() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);// 每天0点2分开始执行
        cal.set(Calendar.MINUTE, 2);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Long diff = cal.getTimeInMillis();
        Date date = new Date(diff);
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(sd.format(date));
    }

    @Test
    public void test2(){
        String content="【山石玉&金海贷】您已收到还款￥150.93元，详情请登录平台账户查看。金海贷华丽转型山石玉，海外双人游抽奖火热进行中，100%中奖！";
        String match = MatchUtil.getInstance().match(content, "(【山石玉)&(金海贷】)&(抽奖)");
        System.out.println(match);

    }


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
                            log.info("触发自动屏蔽词:{};手机号:{};uid:{},类型:{}", words, vo.getMobile(), uid, s.getScreentype());
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
                            log.info("触发审核屏蔽词:{};手机号:{};uid:{},类型:{}", words, vo.getMobile(), uid, s.getScreentype());
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

}
