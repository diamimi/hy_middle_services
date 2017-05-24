package com.sioo.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.BasicDBObject;
import com.sioo.cache.LocalQueueCache;
import com.sioo.cache.MobileAreaCache;
import com.sioo.cache.UserCache;
import com.sioo.db.mongo.MongoManager;
import com.sioo.hy.cmpp.vo.ResultVo;
import com.sioo.hy.cmpp.vo.SendingVo;
import com.sioo.log.LogInfo;
import com.sioo.mq.SendChannelProducer;
import com.sioo.sms.handle.channel.ChannelHandle;
import com.sioo.sms.handle.channel.SignHandle;
import com.sioo.sms.handle.sms.SmsHandle;
import com.sioo.sms.handle.sms.UserTopics;
import com.sioo.util.*;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class SmsService {
    private SmsService() {
    }

    private static SmsService smsService;

    public static SmsService getInstance() {
        if (smsService != null) {
            return smsService;
        }
        synchronized (SmsService.class) {
            if (smsService == null) {
                smsService = new SmsService();
            }
        }
        return smsService;
    }

    private static String defaultLocation = "全国,-1"; // 默认归属地
    private static Logger log = Logger.getLogger(SmsService.class);

    public boolean smsHandle(int type, SendingVo vo, RabbitMQProducerUtil util) {
        try {
            LocalQueueCache localQueueCache = LocalQueueCache.getInstance();
            //定义用户日报表
            ResultVo resultVo = new ResultVo();
            resultVo.setUid(vo.getUid());
            resultVo.setSenddate(DateUtils.getDay());

            //Step1：校验用户是否存在
            Map<String, Object> user_map = UserCache.getInstance().getUser(vo.getUid());
            if (null == user_map || !user_map.get("stat").toString().equals("1")) {
                //如果用户为空或者用户被禁用，不处理送消息
                log.error("[SmsService] smsHandle, user is not find or stat!=1. uid:" + vo.getUid());
                LogInfo.getLog().errorData(JSON.toJSONString(vo));
                return false;
            }

            //Step2：更新用户属性
            vo = updateProperty(vo, user_map);

            //Step3：校验用户签名和拓展
            vo = SignHandle.getInstance().checkSignAndExpend(vo, user_map, util);
            //重新设置条数
            vo.setContentNum(calcContentNum(vo.getContent()));

            if (vo.getAutoFlag() == ConstantSys.AUTO_FLAG_HISTORY) {
                //校验签名失败
                log.info("[SmsService] signHandle, check sign and expend fail. mobile:" + vo.getMobile());
                //重新设置用户日报表提交条数
                resultVo.setSubmitTotal(vo.getContentNum());
                //签名校验失败也需要扣费
                vo = UserTopics.kouSms(vo, user_map);
                if (vo.getAutoFlag() == ConstantSys.AUTO_FLAG_SMS) {
                    //不计费的不统计到用户报表中，往消费记录未计费条数添加
                    log.error("[SmsService] smsHandle, user balance is not enough. uid:" + vo.getUid());
                    LocalQueueCache.getInstance().putUnkouSms(vo);
                    this.fail(vo, null, localQueueCache, null);
                } else {
                    this.fail(vo, resultVo, localQueueCache, null);
                }
                return false;
            }

            //Step4：计费
            vo = UserTopics.kouSms(vo, user_map);
            if (vo.getAutoFlag() == ConstantSys.AUTO_FLAG_SMS) {
                //不计费的不统计到用户报表中，往消费记录未计费条数添加
                log.error("[SmsService] smsHandle, user balance is not enough. uid:" + vo.getUid());
                LocalQueueCache.getInstance().putUnkouSms(vo);
                this.fail(vo, null, localQueueCache, null);
                return false;
            }

            //Step5：校验用户和系统控制信息
            return smsBasicCheck(type, vo, util, null, user_map, localQueueCache, resultVo);
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("校验短信异常", "[SmsService.smsHandle() Exception]; data: " + JSON.toJSONString(vo) + LogInfo.getTrace(e));
        }
        return false;
    }

    /****
     * 校验用户和系统控制信息
     * @param type
     * @param vo
     * @param util
     * @param user_map
     * @param localQueueCache
     * @param resultVo
     * @return
     */
    public boolean smsBasicCheck(int type, SendingVo vo, RabbitMQProducerUtil util, MongoManager mongo, Map<String, Object> user_map, LocalQueueCache localQueueCache, ResultVo resultVo) {
        //初始化审核标志
        vo.setRelease(0);
        if (vo.getMdstr() == null) {
            try {
                vo.setMdstr(MyUtils.getMD5Str(vo.getUid(), vo.getChannel(), vo.getMtype(), vo.getContent(), 16));
            } catch (UnsupportedEncodingException e) {
                log.info("加密失败！");
            }
        }
        /**
         * 各种校验,包括审核屏蔽词,自动屏蔽词,用户屏蔽词,等
         */
        vo = SmsHandle.getInstance().checkSms(vo, user_map, resultVo == null ? false : true);

        if (vo.getAutoFlag() == ConstantSys.AUTO_FLAG_HISTORY) {
            log.debug("[SmsService] on checkSms is auto sms");
            // 自动处理进历史记录表，并插入状态回执队列消息，不继续执行
            this.fail(vo, resultVo, localQueueCache, mongo);
            return false;
        }

        // 校验通道签名等通道信息
        vo = ChannelHandle.getInstance().channelHandle(vo, user_map);
        // 将归属地的编号去掉,用户屏蔽地区和通道屏蔽地区都已经校验完
        if (vo.getLocation() != null) {
            vo.setLocation(vo.getLocation().split(",")[0]);
        }
        if (vo.getAutoFlag() == ConstantSys.AUTO_FLAG_HISTORY) {
            log.debug("[SmsService] on channelHandle is auto sms");
            // 自动处理进历史记录表，并插入状态回执队列消息，不继续执行
            this.fail(vo, resultVo, localQueueCache, mongo);
            return false;
        } else if (vo.getAutoFlag() == ConstantSys.AUTO_FLAG_NONE) {//校验不通过,放到队列管理表
            log.debug("[SmsService] smsHandle, channel is enable \r\n" + JSON.toJSONString(vo));
            if (resultVo != null) {
                localQueueCache.putSendingHistory(vo);
                resultVo.setSubmitTotal(vo.getContentNum());
                resultVo.setSubmitSuccess(vo.getContentNum());
                localQueueCache.putResult(resultVo);
            }
            localQueueCache.putSending(vo);
            return false;
        }

        //最后判断是否审核
        if (vo.getRelease() == 1) {
            //log.info("[SmsService] on checkSms is release sms");
            // 审核进审核表并记录历史记录，不继续执行
            if (resultVo != null) {
                localQueueCache.putSendingHistory(vo);
                resultVo.setSubmitTotal(vo.getContentNum());
                resultVo.setSubmitSuccess(vo.getContentNum());
                localQueueCache.putResult(resultVo);
            }
            //vo.setContent(StringEscapeUtils.escapeHtml4(vo.getContent()));
            localQueueCache.putSendingRelease(vo);
            return false;
        }

        // 获取优先级，如果为优先队列为0，普通队列从用户获取优先级
        int priority = 0;
        if (type == 0) {
            priority = user_map.get("userkind") == null ? 3 : Integer.parseInt(user_map.get("userkind").toString());
        }
        SendChannelProducer.send(vo, util, priority);//校验通过
        if (resultVo != null) {
            // 放入消息队列中
            localQueueCache.putSendingHistory(vo);
            resultVo.setSubmitTotal(vo.getContentNum());
            resultVo.setSubmitSuccess(vo.getContentNum());
            localQueueCache.putResult(resultVo);
        }
        return true;
    }

    /***
     * 重新计算条数
     * @param content
     * @return
     */
    private int calcContentNum(String content) {
        // 重新计算条数
        int contentLength = content.length();
        int cCount = contentLength > 70 ? 67 : 70;
        if (contentLength % cCount != 0) {
            cCount = (contentLength / cCount) + 1;
        } else {
            cCount = contentLength / cCount;
        }
        return cCount;
    }

    /***
     * 处理短信被拦截时返回结果
     *
     * @param vo
     * @param resultVo
     * @param localQueueCache
     */
    private void fail(SendingVo vo, ResultVo resultVo, LocalQueueCache localQueueCache, MongoManager mongo) {
        try {
            if (vo.getLocation() != null && vo.getLocation().indexOf(",") != -1) {
                vo.setLocation(vo.getLocation().split(",")[0]);
            }
            vo.setArrive_fail(vo.getContentNum());
            vo.setSucc(vo.getContentNum());
            vo.setStat(1);
            localQueueCache.putReport(vo);// 准备入库
            if (resultVo != null) {
                resultVo.setSubmitTotal(vo.getContentNum());
                resultVo.setSubmitFail(vo.getContentNum());
                localQueueCache.putResult(resultVo);
            }

            //失败补发，队列的短信   拦截的短信需要修改历史记录状态
            if (mongo != null) {
                //修改历史记录状态
                BasicDBObject where = new BasicDBObject();
                where.put("a", vo.getId());

                BasicDBObject set = new BasicDBObject();
                set.put("j", vo.getContentNum()); // succ
                set.put("k", 0); // fail
                set.put("q", 0);// arrive_succ
                set.put("r", vo.getArrive_fail());// arrive_fail
                set.put("s", 1);// stat

                mongo.update2("sms_send_history_unknown", set, where);
            } else {
                localQueueCache.putSendingHistory(vo);
            }

        } catch (Exception e) {
            LogInfo.getLog().errorAlert("处理失败短信异常", "[SmsService.fail() Exception]; data: " + JSON.toJSONString(vo) + LogInfo.getTrace(e));
        }
    }

    /***
     * 更新相关属性值
     *
     * @param vo
     * @return
     * @throws UnsupportedEncodingException
     */
    private SendingVo updateProperty(SendingVo vo, Map<String, Object> user_map) {
        try {
            Map<String, Object> map = MobileAreaCache.getInstance().getMobileArea(vo.getMobile());
            if (map != null && !map.isEmpty()) {
                vo.setLocation(map.get("province").toString() + "," + map.get("provincecode") + "&" + map.get("citycode"));
                vo.setProvinceCode(Integer.valueOf(map.get("provincecode").toString()));
                vo.setCityCode(Integer.valueOf(map.get("citycode").toString()));
            } else {
                vo.setLocation(defaultLocation);
                vo.setProvinceCode(0);
                vo.setCityCode(0);
            }
            if (vo.getExpid() != null && vo.getExpid().length() > 12) {
                vo.setExpid(vo.getExpid().substring(0, 12));
            }
            vo.setContent(vo.getContent().trim().replace("\\r\\n", "\\n").replace("'", "‘").replace("\\", "/"));
            vo.setId(SmsCache.getHisID());
            vo.setStat(0);
            vo.setMdstr(MyUtils.getMD5Str(vo.getUid(), vo.getChannel(), vo.getMtype(), vo.getContent(), 16));

            if (user_map == null) {
                return vo;
            }

            // 回T退订
            vo.setContent(checkReplyn(user_map.get("replyn") == null ? 0 : Integer.valueOf(user_map.get("replyn").toString()), vo.getContent()));

            //设置用户通道
            if (vo.getMtype() == 1 && user_map.get("mobile") != null) {
                vo.setChannel((Integer) user_map.get("mobile"));
            } else if (vo.getMtype() == 2 && user_map.get("unicom") != null) {
                vo.setChannel((Integer) user_map.get("unicom"));
            } else if (vo.getMtype() == 4 && user_map.get("telecom") != null) {
                vo.setChannel((Integer) user_map.get("telecom"));
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("修改消息属性异常", "[SmsService.updateProperty() Exception]; data: " + JSON.toJSONString(vo) + LogInfo.getTrace(e));
        }
        return vo;
    }

    /***
     * 是否加了回T退订
     * @param reply
     * @param content
     * @return
     */
    private String checkReplyn(int reply, String content) {
        if (reply == 1 && !content.toUpperCase().contains("回T退订")) {
            if (content.contains("【") && content.endsWith("】")) {
                String sign = content.substring(content.lastIndexOf("【"));
                content = content.replace(sign, "回T退订" + sign);
            } else {
                content = content + "回T退订";
            }
        }
        return content;
    }
}
