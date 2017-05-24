package com.sioo.cache;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sioo.dao.SysCacheDao;
import com.sioo.log.LogInfo;
import com.sioo.util.ConstantSys;
import com.sioo.util.MatchUtil;
import com.sioo.util.SmsCache;
import com.sioo.util.UpdateCacheConstant.METHOD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/***
 * 用户屏蔽词缓存操作类
 *
 * @author OYJM
 * @date 2016年9月27日
 *
 */
public class UserBlackWordsCache {
    private static Logger log = LoggerFactory.getLogger(UserBlackWordsCache.class);
    private static UserBlackWordsCache userBlackWordsCache = null;

    public static UserBlackWordsCache getInstance() {
        if (userBlackWordsCache != null) {
            return userBlackWordsCache;
        }
        synchronized (UserBlackWordsCache.class) {
            if (userBlackWordsCache == null) {
                userBlackWordsCache = new UserBlackWordsCache();
            }
        }
        return userBlackWordsCache;
    }


    /**
     * 加载用户自动屏蔽词
     *
     * @param uid
     */
    public void loadUserBlackWordsAuto(Integer uid) {
        try {
            List<Map<String, Object>> wordsList = SysCacheDao.getInstance().findSmsWordsUser(uid, ConstantSys.USER_WORDS_AUTO);
            Map<Integer, CopyOnWriteArrayList<String>> currentMap = new ConcurrentHashMap<>();
            if (null != wordsList) {
                int i = 0;
                CopyOnWriteArrayList<String> list = null;
                for (Map<String, Object> map : wordsList) {
                    int key = (Integer) map.get("uid");
                    String word = map.get("words").toString();
                    if (word != null && word.length() > 0) {
                        list = currentMap.get(key);
                        if (list == null) {
                            list = new CopyOnWriteArrayList<>();
                        }
                        if (!list.contains(word)) {
                            list.add(word);
                            currentMap.put(key, list);
                            i++;
                        }
                    }
                }
                log.info("用户非法词加载【" + i + "】个");
                SmsCache.USER_BALCK_WORDS_AUTO = currentMap;
            }

        } catch (Exception e) {
            LogInfo.getLog().errorAlert("加载用户屏蔽词缓存异常", "[UserBlackWordsCache.loadUserBlackWords(" + uid + ") ]" + LogInfo.getTrace(e));
        }
    }


    /***
     * 加载用户审核屏蔽词
     *
     * @param uid
     */
    public void loadUserBlackWords(Integer uid) {
        try {
            List<Map<String, Object>> wordsList = SysCacheDao.getInstance().findSmsWordsUser(uid, ConstantSys.USER_WORDS_CHECK);
            Map<Integer, CopyOnWriteArrayList<String>> currentMap = new ConcurrentHashMap<>();
            if (null != wordsList) {
                int i = 0;
                CopyOnWriteArrayList<String> list = null;
                for (Map<String, Object> map : wordsList) {
                    int key = (Integer) map.get("uid");
                    String word = map.get("words").toString();
                    if (word != null && word.length() > 0) {
                        list = currentMap.get(key);
                        if (list == null) {
                            list = new CopyOnWriteArrayList<>();
                        }
                        if (!list.contains(word)) {
                            list.add(word);
                            currentMap.put(key, list);
                            i++;
                        }
                    }
                }
                log.info("用户非法词加载【" + i + "】个");
                SmsCache.USER_BALCK_WORDS = currentMap;
            }

        } catch (Exception e) {
            LogInfo.getLog().errorAlert("加载用户屏蔽词缓存异常", "[UserBlackWordsCache.loadUserBlackWords(" + uid + ") ]" + LogInfo.getTrace(e));
        }
    }

    /***
     * 重新加载用户屏蔽词
     */
    public void reloadUserBlackWords() {
        this.loadUserBlackWords(0);
        this.loadUserBlackWordsAuto(0);
    }

    /***
     * 添加用户屏蔽词
     *
     */
    public void addUserBlackWords(JSONArray array) {
        try {
            for (Object obj : array.toArray()) {
                JSONObject json = (JSONObject) obj;
                Integer uid = json.getInteger("uid");
                String words = json.getString("words");
                Integer type = json.getInteger("type") == null ? 0 : json.getInteger("type");
                /**
                 * 1 审核 2自动
                 */
                if (type == 2) {
                    if (SmsCache.USER_BALCK_WORDS_AUTO.containsKey(uid)) {
                        CopyOnWriteArrayList<String> list = SmsCache.USER_BALCK_WORDS_AUTO.get(uid);
                        if (!list.contains(words)) {
                            list.add(words);
                            SmsCache.USER_BALCK_WORDS_AUTO.put(uid, list);
                            log.info("添加用户自动屏蔽词; uid:{},words:{},type:{}", uid, words, type);
                        }
                    } else {
                        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
                        list.add(words);
                        SmsCache.USER_BALCK_WORDS_AUTO.put(uid, list);
                        log.info("添加用户自动屏蔽词; uid:{},words:{},type:{}", uid, words, type);
                    }

                } else {
                    if (SmsCache.USER_BALCK_WORDS.containsKey(uid)) {
                        CopyOnWriteArrayList<String> list = SmsCache.USER_BALCK_WORDS.get(uid);
                        if (!list.contains(words)) {
                            list.add(words);
                            SmsCache.USER_BALCK_WORDS.put(uid, list);
                            log.info("添加用户审核屏蔽词; uid:{},words:{},type:{}", uid, words, type);
                        }
                    } else {
                        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
                        list.add(words);
                        SmsCache.USER_BALCK_WORDS.put(uid, list);
                        log.info("添加用户审核屏蔽词; uid:{},words:{},type:{}", uid, words, type);
                    }
                }
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("添加用户屏蔽词缓存异常", "[UserBlackWordsCache.addUserBlackWords(" + array.toJSONString() + ") ]" + LogInfo.getTrace(e));
        }
    }

    /****
     * 删除用户屏蔽词
     *
     */
    public void deleteUserBlackWords(JSONArray array) {
        try {
            for (Object obj : array.toArray()) {
                JSONObject json = (JSONObject) obj;
                Integer uid = json.getInteger("uid");
                String words = json.getString("words");
                Integer type = json.getInteger("type") == null ? 0 : json.getInteger("type");
                if (type == 2) {
                    if (SmsCache.USER_BALCK_WORDS_AUTO.containsKey(uid) && SmsCache.USER_BALCK_WORDS_AUTO.get(uid).contains(words)) {
                        CopyOnWriteArrayList<String> list = SmsCache.USER_BALCK_WORDS_AUTO.get(uid);
                        list.remove(words);
                        SmsCache.USER_BALCK_WORDS_AUTO.put(uid, list);
                        log.info("删除用户屏蔽词; uid:" + uid + ", words:" + words);
                    }
                } else {
                    if (SmsCache.USER_BALCK_WORDS.containsKey(uid) && SmsCache.USER_BALCK_WORDS.get(uid).contains(words)) {
                        CopyOnWriteArrayList<String> list = SmsCache.USER_BALCK_WORDS.get(uid);
                        list.remove(words);
                        SmsCache.USER_BALCK_WORDS.put(uid, list);
                        log.info("删除用户屏蔽词; uid:" + uid + ", words:" + words);
                    }
                }
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("删除用户屏蔽词缓存异常", "[UserBlackWordsCache.deleteUserBlackWords(" + array.toJSONString() + ") ]" + LogInfo.getTrace(e));
        }
    }


    /**
     * 验证用户自动屏蔽词
     * @param uid
     * @param content
     * @param mobile
     * @return
     */
    public String isUserBlackWordsAuto(Integer uid, String content,long mobile) {
        try {
            List<String> words = SmsCache.USER_BALCK_WORDS_AUTO.get(uid);
            if (null != words && words.size() > 0) {
                for (String word : words) {
                    String match = MatchUtil.getInstance().match(content, word);
                    if(match!=null){
                        log.info("触发用户自动屏蔽词; uid:{},mobile:{},words:{},content:{}",uid,mobile,word,content );
                        return match;
                    }
                }
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("校验用户屏蔽词缓存异常", "[UserBlackWordsCache.isUserBlackWords(" + uid + "," + content + ") ]" + LogInfo.getTrace(e));
        }
        return null;
    }

    /***
     * 验证用户审核屏蔽词
     *
     * @param uid
     * @param content
     * @return true为校验通过
     */
    public String isUserBlackWords(Integer uid, String content,long mobile) {
        try {
            List<String> words = SmsCache.USER_BALCK_WORDS.get(uid);
            if (null != words && words.size() > 0) {

                for (String word : words) {
                    String match = MatchUtil.getInstance().match(content, word);
                    if(match!=null){
                        log.info("触发用户审核屏蔽词; uid:{},mobile:{},words:{},content:{}",uid,mobile,word ,content);
                        return match;
                    }
                }
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("校验用户屏蔽词缓存异常", "[UserBlackWordsCache.isUserBlackWords(" + uid + "," + content + ") ]" + LogInfo.getTrace(e));
        }
        return null;
    }

    /***
     * 接口修改用户屏蔽词入口
     *
     * @param method
     * @param array
     */
    public void excute(Integer method, JSONArray array) {
        try {
            if (method == null || array.isEmpty()) {
                return;
            }
            if (method == METHOD.ADD || method == METHOD.UPDATE) {
                addUserBlackWords(array);
            } else if (method == METHOD.DELETE) {
                deleteUserBlackWords(array);
            } else if (method == METHOD.RELOAD) {
                loadUserBlackWords(0);
                loadUserBlackWordsAuto(0);
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("修改用户屏蔽词缓存异常", "[UserBlackWordsCache.excute(" + method + "," + array.toJSONString() + ") ]" + LogInfo.getTrace(e));
        }
    }

}
