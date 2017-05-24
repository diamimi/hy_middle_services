package com.sioo.cache;

import com.sioo.dao.SysCacheDao;
import com.sioo.hy.cmpp.vo.ReleaseTemplateVo;
import com.sioo.log.LogInfo;
import com.sioo.util.DateUtils;
import com.sioo.util.SmsCache;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/****
 * 审核模板缓存操作类
 * @author OYJM
 * @date 2017年2月9日
 *
 */
public class ReleaseTemplateCache {
    private static Logger log = Logger.getLogger(ReleaseTemplateCache.class);
    private static ReleaseTemplateCache releaseTemplateCache = null;

    public static ReleaseTemplateCache getInstance() {
        if (releaseTemplateCache != null) {
            return releaseTemplateCache;
        }
        synchronized (ReleaseTemplateCache.class) {
            if (releaseTemplateCache == null) {
                releaseTemplateCache = new ReleaseTemplateCache();
            }
        }
        return releaseTemplateCache;
    }


    /***
     * 加载审核模板
     * @param uid
     */
    public void loadReleaseTemplate(Integer uid) {
        try {
            List<Map<String, Object>> releaseTemplateList = SysCacheDao.getInstance().findReleaseTemplate(uid);
            Map<Integer, List<ReleaseTemplateVo>> currentMap = new ConcurrentHashMap<Integer, List<ReleaseTemplateVo>>();
            if (null != releaseTemplateList) {
                List<ReleaseTemplateVo> subList = null;
                ReleaseTemplateVo templateVo = null;
                for (Map<String, Object> map : releaseTemplateList) {
                    try {
                        Integer key = (Integer) map.get("uid");
                        subList = currentMap.get(key);
                        if (subList == null) {
                            subList = new ArrayList<ReleaseTemplateVo>();
                        }
                        templateVo = new ReleaseTemplateVo((Integer) map.get("type"),map.get("effectivetime") == null ? null : (Long) map.get("effectivetime"),map.get("content").toString());
                        if (!subList.contains(templateVo)) {
                            subList.add(templateVo);
                        }
                        currentMap.put(key, subList);
                    } catch (Exception e) {
                       log.error(e.getMessage(),e);
                    }
                }
                if (uid != null && uid != 0) {
                    SmsCache.RELEASE_TEMPLATE.put(uid, currentMap.get(uid));
                } else {
                    SmsCache.RELEASE_TEMPLATE = currentMap;
                }
                log.info("用户" + uid + "审核模板加载【" + releaseTemplateList.size() + "】个");
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("加载用户审核模板缓存异常", "[ReleaseTemplateCache.loadReleaseTemplate(" + uid + ") ]" + LogInfo.getTrace(e));
        }
    }


    /***
     * 校验是否为审核模板
     * @param uid
     * @param content
     * @return
     */
    public boolean isReleaseTemplate(Integer uid, String content,long mobile) {
        try {
            List<ReleaseTemplateVo> releaseTemplateList = SmsCache.RELEASE_TEMPLATE.get(uid);
            if (null != releaseTemplateList && releaseTemplateList.size() > 0) {
                for (ReleaseTemplateVo templateVo : releaseTemplateList) {
                    if (templateVo.getType() == 0 || (templateVo.getType() == 1 && templateVo.getEffectivetime() > DateUtils.getTime())) {
                        if (templateVo.getPattern().matcher(content).matches()) {
                            log.info("触发审核模板; uid:" + uid + ", mobile:" + mobile);
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("校验审核模板异常", "[ReleaseTemplateCache.isReleaseTemplate(" + uid + "," + content + ") ]" + LogInfo.getTrace(e));
        }
        return false;
    }


    public void excute(Integer uid) {
        try {
            loadReleaseTemplate(uid);
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("修改审核模板缓存异常", "[ReleaseTemplateCache.excute(" + uid + ") ]" + LogInfo.getTrace(e));
        }
    }
}
