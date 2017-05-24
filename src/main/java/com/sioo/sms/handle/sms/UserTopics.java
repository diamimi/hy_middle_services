package com.sioo.sms.handle.sms;

import java.util.Map;

import com.sioo.util.*;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sioo.cache.UserSmsAlertCache;
import com.sioo.cache.UserSmsCache;
import com.sioo.dao.SmsUserDao;
import com.sioo.hy.cmpp.vo.SendingVo;
import com.sioo.log.LogInfo;

public class UserTopics {
    private static Logger log = Logger.getLogger(UserTopics.class);

    public static SendingVo kouSms(SendingVo sendingVo, Map<String, Object> user_map) {
        try {
            // 计费
            int kousms = 0;
            kousms = getCharging(sendingVo);

            // 余额不足自动处理成失败
            if (kousms == -1) {
                sendingVo.setStat(ConstantSys.SUBMIT_RESULT_SUCCESS);
                sendingVo.setRptStat(ConstantStatus.USER_STATUS_NOBLANCE);
                sendingVo.setAutoFlag(ConstantSys.AUTO_FLAG_SMS);
                return sendingVo;
            } else {
                int uid = sendingVo.getUid();
                // 余额提醒，修改为放入到队列中发送
                Map<String, Object> mapAlert = UserSmsAlertCache.getInstance().getSmsUserAlert(uid);
                if (mapAlert != null) {
                    int now = DateUtils.getDay();
                    if (mapAlert.get("senddate") != null&& !mapAlert.get("senddate").toString().equals("") && ((Integer) mapAlert.get("senddate")) == now) {
                        return sendingVo;
                    }
                    int num = (Integer) mapAlert.get("num");// 提醒条数
                    int userSms = UserSmsCache.getInstance().getUserSms(uid);
                    if (userSms < num) {
                        mapAlert.put("senddate", now);
                        UserSmsAlertCache.getInstance().setSmsUserAlert(uid, mapAlert);
                       // String tempAuth = new MD5().getMD5ofStr((String) user_map.get("username") + (String) user_map.get("pwd"));
                        log.info(uid + ",余额提醒:[userSms=" + userSms + ",alertNum=" + num + "],mapAlert=" + mapAlert);
                        String alertMobile = (String) mapAlert.get("mobile");
                        String content = "尊敬的用户[" + uid + "],您当前剩余短信[" + userSms + "]条,已少于[" + num + "]条,请及时联系[4008887686]进行充值,以免耽误您的正常工作.";
                        //content = java.net.URLEncoder.encode(content, "utf-8");
                        //String url = "http://210.5.158.31:9011/hy/?uid=" + uid + "&auth=" + tempAuth + "&msg=" + content + "&mobile=" + alertMobile + "&expid=0&encode=utf-8";
                        RabbitMQProducerUtil rabbitMQProducerUtil = RabbitMQProducerUtil.getProducerInstance();
                        SendingVo vo = new SendingVo();
                        vo.setUid(uid);
                        vo.setMobile(Long.valueOf(alertMobile));
                        vo.setContent(content);
                        vo.setSenddate(DateUtils.getTime());
                        vo.setMtype(MyUtils.checkMobileType(alertMobile));
                        vo.setExpid("0");
                        vo.setPid(vo.getPid());
                        rabbitMQProducerUtil.send("SUBMIT_QUEUE_"+String.valueOf(uid).substring(0,1), vo);
                        rabbitMQProducerUtil.close();
                        log.info("添加余额提醒{uid:" + uid + ",剩余条数:" + userSms + ",提醒条数:" + num + "}");
                    }
                }
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("扣费异常", "[UserTopics.kouSms() Exception]; data: " + JSON.toJSONString(sendingVo) + LogInfo.getTrace(e));
        }
        return sendingVo;
    }

    /****
     * 预扣用户短信条数
     *
     * @param sendingVo
     * @return
     */
    private static synchronized int getCharging(SendingVo sendingVo) {
        int reSms = 0;
        try {
            int uid = sendingVo.getUid();
            int sms = sendingVo.getContentNum();
            if (sms < 1) {
                log.error("短信条数小于1 \r\n" + JSONObject.toJSON(sendingVo));
            }

            // 从缓存中查找用户预扣条数
            Integer smsObj = UserSmsCache.getInstance().getUserSmsYukou(uid);
            if (null == smsObj) {
                // 如果缓存中不存在，则查询数据库
                smsObj = SmsUserDao.getInstance().findSmsById(uid);
                UserSmsCache.getInstance().putUserSmsYukou(uid, smsObj);
            }

            if (null != smsObj) {
                int smsCache = smsObj;
                int left = smsCache - sms;
                // 余额不足的判断
                if (left < 0) {
                    reSms = -1;
                } else {
                    // 设置用户预扣条数
                    UserSmsCache.getInstance().setSmsKou(uid, sms);
                }
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("预扣用户余额异常", "[UserTopics.getCharging() Exception]; data: " + JSON.toJSONString(sendingVo) + LogInfo.getTrace(e));
        }
        return reSms;
    }
}
