package com.sioo.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sioo.util.ConstantSys;
import com.sioo.util.EhcacheUtil;
import com.sioo.util.SmsCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/***
 * 重号过滤缓存操作类
 *
 * @author OYJM
 * @date 2016年9月27日
 *
 */
public class RepeatMobileCache {
    private static Logger log = LoggerFactory.getLogger(RepeatMobileCache.class);
    private static RepeatMobileCache repeatMobileCache = null;

    public static RepeatMobileCache getInstance() {
        if (repeatMobileCache != null) {
            return repeatMobileCache;
        }
        synchronized (RepeatMobileCache.class) {
            if (repeatMobileCache == null) {
                repeatMobileCache = new RepeatMobileCache();
            }
        }
        return repeatMobileCache;
    }

    /***
     * 清除重号过滤缓存，防止删除时重号过滤报错
     *
     */
    public void deleteRepeatMobile() {
        try {
            log.info("clear repeat mobile temp cache start");
            // 先清除到临时过滤的号码
            Map<Long, List<Long>> subMap = null;
            if (SmsCache.USER_REPEAT_MOBILE_TEMP != null && !SmsCache.USER_REPEAT_MOBILE_TEMP.isEmpty()) {
                for (Integer key : SmsCache.USER_REPEAT_MOBILE_TEMP.keySet()) {
                    subMap = SmsCache.USER_REPEAT_MOBILE_TEMP.get(key);
                    for (Long subKey : subMap.keySet()) {
                        subMap.remove(subKey);
                    }
                    SmsCache.USER_REPEAT_MOBILE_TEMP.remove(key);
                    subMap.clear();
                    subMap = null;
                }
                SmsCache.USER_REPEAT_MOBILE_TEMP.clear();
                SmsCache.USER_REPEAT_MOBILE_TEMP=null;
                // 等待缓存释放
                try {
                    Thread.sleep(1000000);
                } catch (InterruptedException e) {
                    log.error("sleep faild, exception: " + e.toString());
                }
            }
            log.info("clear repeat mobile temp cache success");

            // 再清除重号过滤号码
            log.info("clear repeat mobile cache start");

            if (SmsCache.USER_REPEAT_MOBILE != null && !SmsCache.USER_REPEAT_MOBILE.isEmpty()) {
                SmsCache.cleaningRepeatMobile = true;
                for (Integer key : SmsCache.USER_REPEAT_MOBILE.keySet()) {
                    subMap = SmsCache.USER_REPEAT_MOBILE.get(key);
                    for (Long subKey : subMap.keySet()) {
                        subMap.remove(subKey);
                    }
                    SmsCache.USER_REPEAT_MOBILE.remove(key);
                    subMap.clear();
                    subMap = null;
                }
                SmsCache.USER_REPEAT_MOBILE.clear();
                SmsCache.USER_REPEAT_MOBILE=null;
                // 等待缓存释放
                try {
                    Thread.sleep(1500000);
                } catch (InterruptedException e) {
                    log.error("sleep faild, exception: " + e.toString());
                }
                // SmsCache.USER_REPEAT_MOBILE.putAll(SmsCache.USER_REPEAT_MOBILE_TEMP);
                SmsCache.cleaningRepeatMobile = false;
            }

            //清除重号过滤大小
            if (SmsCache.REPEAT_MOBILE_SIZE != null && !SmsCache.REPEAT_MOBILE_SIZE.isEmpty()) {
                Set<Long> set = SmsCache.REPEAT_MOBILE_SIZE.keySet();
                for (Long key : set) {
                    SmsCache.REPEAT_MOBILE_SIZE.remove(key);
                }
                SmsCache.REPEAT_MOBILE_SIZE.clear();
                SmsCache.REPEAT_MOBILE_SIZE = new ConcurrentHashMap<Long, Integer>();
            }
            log.info("clear repeat mobile cache success");
        } catch (Exception e) {
            log.error("[RepeatMobileCache.deleteRepeatMobile() ]", e);
        }
    }


    /***
     * 获取重号过滤MAP
     *
     * @return
     */
    public Map<Integer, Map<Long, List<Long>>> getRepeatMobileMap() {
        try {
            if (SmsCache.cleaningRepeatMobile) {
                if (SmsCache.USER_REPEAT_MOBILE_TEMP_INIT) {
                    SmsCache.USER_REPEAT_MOBILE_TEMP_INIT = false;
                    Map<Integer, Map<Long, List<Long>>> USER_REPEAT_MOBILE_TEMP = (Map<Integer, Map<Long, List<Long>>>) EhcacheUtil.getInstance().get("repeat", "USER_REPEAT_MOBILE_TEMP");
                    if (USER_REPEAT_MOBILE_TEMP != null) {
                        SmsCache.USER_REPEAT_MOBILE_TEMP = USER_REPEAT_MOBILE_TEMP;
                        return SmsCache.USER_REPEAT_MOBILE_TEMP;
                    } else {
                        return SmsCache.USER_REPEAT_MOBILE_TEMP;
                    }
                } else {
                    return SmsCache.USER_REPEAT_MOBILE_TEMP;
                }
            } else {
                if (SmsCache.USER_REPEAT_MOBILE_INIT) {
                    SmsCache.USER_REPEAT_MOBILE_INIT = false;
                    Map<Integer, Map<Long, List<Long>>> USER_REPEAT_MOBILE = (Map<Integer, Map<Long, List<Long>>>) EhcacheUtil.getInstance().get("repeat", "USER_REPEAT_MOBILE");
                    if (USER_REPEAT_MOBILE != null) {
                        SmsCache.USER_REPEAT_MOBILE = USER_REPEAT_MOBILE;
                        return SmsCache.USER_REPEAT_MOBILE;
                    } else {
                        return SmsCache.USER_REPEAT_MOBILE;
                    }
                } else {
                    return SmsCache.USER_REPEAT_MOBILE;
                }
            }
        } catch (Exception e) {
            log.error("[RepeatMobileCache.getRepeatMobileMap() ]", e);
        }
        return null;
    }

    /***
     * 初始化重号过滤MAP
     *
     * @return
     */
    public Map<Integer, Map<Long, List<Long>>> initRepeatMobileMap() {
        try {
            if (SmsCache.cleaningRepeatMobile) {
                Map<Integer, Map<Long, List<Long>>> USER_REPEAT_MOBILE_TEMP = null;
                if (SmsCache.USER_REPEAT_MOBILE_TEMP_INIT) {
                    SmsCache.USER_REPEAT_MOBILE_TEMP_INIT = false;
                    USER_REPEAT_MOBILE_TEMP = (Map<Integer, Map<Long, List<Long>>>) EhcacheUtil.getInstance().get("repeat", "USER_REPEAT_MOBILE_TEMP");
                }
                if (USER_REPEAT_MOBILE_TEMP == null || USER_REPEAT_MOBILE_TEMP.size() == 0) {
                    SmsCache.USER_REPEAT_MOBILE_TEMP = new ConcurrentHashMap<Integer, Map<Long, List<Long>>>();
                }else {
                    SmsCache.USER_REPEAT_MOBILE_TEMP=USER_REPEAT_MOBILE_TEMP;
                }
                return SmsCache.USER_REPEAT_MOBILE_TEMP;
            } else {
                Map<Integer, Map<Long, List<Long>>> USER_REPEAT_MOBILE = null;
                if (SmsCache.USER_REPEAT_MOBILE_INIT) {
                    SmsCache.USER_REPEAT_MOBILE_INIT = false;
                    USER_REPEAT_MOBILE = (Map<Integer, Map<Long, List<Long>>>) EhcacheUtil.getInstance().get("repeat", "USER_REPEAT_MOBILE");
                }
                if (USER_REPEAT_MOBILE == null || USER_REPEAT_MOBILE.size() == 0) {
                    SmsCache.USER_REPEAT_MOBILE = new ConcurrentHashMap<Integer, Map<Long, List<Long>>>();
                } else {
                    SmsCache.USER_REPEAT_MOBILE = USER_REPEAT_MOBILE;
                }
                return SmsCache.USER_REPEAT_MOBILE;
            }
        } catch (Exception e) {
            log.error("[RepeatMobileCache.initRepeatMobileMap() ]", e);
        }
        return null;
    }

    /***
     * 添加重号过滤号码
     *
     * @param key
     * @param map
     */
    public void putRepeatMobileMap(Integer key, Map<Long, List<Long>> map) {
        try {
            if (SmsCache.cleaningRepeatMobile) {
                SmsCache.USER_REPEAT_MOBILE_TEMP.put(key, map);
            } else {
                SmsCache.USER_REPEAT_MOBILE.put(key, map);
            }
        } catch (Exception e) {
            log.error("[RepeatMobileCache.initRepeatMobileMap(" + key + "," + JSON.toJSONString(map) + ") ]", e);
        }
    }

    /****
     * 校验重号过滤
     *
     * @param uid
     * @param mobile
     * @param repeatfilter
     * @param repeatnum
     * @return
     */
    public boolean isRepeatMobile(Integer uid, Long mobile, Integer repeatfilter, Integer repeatnum) {
        boolean isNotRepeat = false;
        boolean isRepeat = false;
        try {
            // 参数不正确
            if (mobile == null || repeatnum == null) {
                log.info(mobile + "," + repeatnum + "触发重号过滤; 参数错误,不过滤");
                return isRepeat;
            }

            // 如果不过滤，则直接返回
            if (repeatfilter == null || repeatfilter.equals(ConstantSys.REPEATFILTER_NONE)) {
                log.info( "{},{},repeatfilter为null或为0,不过滤",mobile,repeatfilter);
                return isNotRepeat;
            }

            // key:手机号码前五位
            Integer key = Integer.parseInt(mobile.toString().substring(0, 5));
            // 获取当前时间
            long nowTimeLong = System.currentTimeMillis();

            // 获取重号过滤map，并定义子map和时间戳List
            Map<Integer, Map<Long, List<Long>>> repeatMobileMap = this.getRepeatMobileMap();
            Map<Long, List<Long>> subMap = null;
            List<Long> list = null;

            // 如果重号过滤列表为空,初始化重号过滤信息
            if (repeatMobileMap == null) {
                repeatMobileMap = initRepeatMobileMap();
            }

            // 如果重号过滤map中没有值，重建一个保存，并返回不重号
            if (repeatMobileMap==null||repeatMobileMap.isEmpty() || repeatMobileMap.get(key) == null || repeatMobileMap.get(key).isEmpty()) {
                subMap = new ConcurrentHashMap<Long, List<Long>>();
                list = new ArrayList<Long>();
                list.add(nowTimeLong);
                subMap.put(mobile, list);
                putRepeatMobileMap(key, subMap);
                return isNotRepeat;
            }

            // 获取号码段中对应的号码map
            subMap = repeatMobileMap.get(key);
            // 判断号码是否在号码段中
            if (subMap.isEmpty() || subMap.get(mobile) == null || subMap.get(mobile).isEmpty()) {
                list = new ArrayList<Long>();
                list.add(nowTimeLong);
                subMap.put(mobile, list);
                putRepeatMobileMap(key, subMap);
                return isNotRepeat;
            }

            // 判断发送时间列表是否存在
            list = subMap.get(mobile);
            if (list == null || list.isEmpty()) {
                list = new ArrayList<Long>();
                list.add(nowTimeLong);
                subMap.put(mobile, list);
                putRepeatMobileMap(key, subMap);
                return isNotRepeat;
            }

            int listSize = list.size();
            // 发送的时间集合大小小于设置的重号大小
            if (listSize < repeatnum) {
                list.add(nowTimeLong);
                subMap.put(mobile, list);
                putRepeatMobileMap(key, subMap);
                return isNotRepeat;
            }

            if (repeatfilter.equals(ConstantSys.REPEATFILTER_ONE_DAY)) {
                // 如果为按一天过滤
                list.add(nowTimeLong);
                subMap.put(mobile, list);
                putRepeatMobileMap(key, subMap);
                if (listSize >= repeatnum) {
                    incrementByMobile(mobile, listSize);
                    log.info(mobile + "触发重号过滤; 已发送:" + listSize + ", 重号大小: " + repeatnum);
                    return true;
                } else {
                    return isNotRepeat;
                }
            } else if (repeatfilter.equals(ConstantSys.REPEATFILTER_ONE_HOUR) || repeatfilter.equals(ConstantSys.REPEATFILTER_TEN_MINUTE)) {
                // 如果为按一小时或者十分钟过滤
                list.add(nowTimeLong);
                subMap.put(mobile, list);
                putRepeatMobileMap(key, subMap);
                long time = 0L;
                // 将一小时和一分钟转换为毫秒数
                if (repeatfilter.equals(ConstantSys.REPEATFILTER_ONE_HOUR)) {
                    time = 1000 * 60 * 60;
                } else if (repeatfilter.equals(ConstantSys.REPEATFILTER_TEN_MINUTE)) {
                    time = 1000 * 60 * 10;
                }
                // 获取列表下标
                int index = listSize - repeatnum;
                // 获取需要比较的值
                long lastTime = list.get(index);
                // 判断当前时间-倒数第repeatnum的发送时间，是否小于时间间隔
                if (nowTimeLong - lastTime < time) {
                    incrementByMobile(mobile, listSize);
                    log.info(mobile + "触发重号过滤; 已发送:" + listSize + ", 重号大小: " + repeatnum + ", 最后发送时间: " + lastTime);
                    return true;
                } else {
                    return isNotRepeat;
                }
            }
        } catch (Exception e) {
            log.error("[RepeatMobileCache.isRepeatMobile(" + uid + "," + mobile + "," + repeatfilter + "," + repeatnum + ") ]", e);
            return isRepeat;
      }
        return isRepeat;
    }


    public String getRepeatMobile(Integer page, Integer pageSize, Long mobile) {
        if (page == null && pageSize == null) {
            if (mobile == null) {
                return SmsCache.REPEAT_MOBILE_SIZE.size() + "";
            } else {
                return (SmsCache.REPEAT_MOBILE_SIZE.containsKey(mobile)) ? "1" : "0";
            }
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> subMap = null;
        if (mobile != null && mobile != 0) {
            subMap = new HashMap<String, Object>();
            subMap.put("mobile", mobile);
            subMap.put("size", 0);
            if (SmsCache.REPEAT_MOBILE_SIZE.containsKey(mobile)) {
                subMap.put("size", SmsCache.REPEAT_MOBILE_SIZE.get(mobile));
            }
            list.add(subMap);
        } else {
            Integer from = (page - 1) * pageSize + 1;
            Integer to = page * pageSize;
            Map<Long, Integer> map = SmsCache.REPEAT_MOBILE_SIZE;
            ValueComparator bvc = new ValueComparator(map);
            TreeMap<Long, Integer> treeMap = new TreeMap<Long, Integer>(bvc);
            treeMap.putAll(map);
            Set<Entry<Long, Integer>> set = treeMap.entrySet();
            Iterator<Entry<Long, Integer>> iter = set.iterator();
            int i = 1;
            while (iter.hasNext()) {
                if (i >= from && i <= to) {
                    Entry<Long, Integer> me = iter.next();
                    subMap = new HashMap<String, Object>();
                    subMap.put("mobile", me.getKey());
                    subMap.put("size", me.getValue());
                    list.add(subMap);
                } else {
                    iter.next();
                }
                i++;
            }
        }
        return JSONObject.toJSONString(list);
    }

    public void incrementByMobile(Long mobile, Integer size) {
        SmsCache.REPEAT_MOBILE_SIZE.put(mobile, size);
    }

    public static void main(String[] args) {
        RepeatMobileCache repeat = new RepeatMobileCache();
        repeat.incrementByMobile(13865895684L, 10);
        repeat.incrementByMobile(13865895682L, 4);
        repeat.incrementByMobile(13865895683L, 13);
        repeat.incrementByMobile(13865895685L, 9);
        repeat.incrementByMobile(13865895686L, 2);

        System.out.println(repeat.getRepeatMobile(2, 2, 0L));
    }
}

class ValueComparator implements Comparator<Long> {
    Map<Long, Integer> base;

    public ValueComparator(Map<Long, Integer> base) {
        this.base = base;
    }

    public int compare(Long a, Long b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        }
    }
}
