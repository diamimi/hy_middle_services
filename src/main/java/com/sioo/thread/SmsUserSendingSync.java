package com.sioo.thread;

import com.alibaba.fastjson.JSON;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sioo.cache.LocalQueueCache;
import com.sioo.dao.SmsUserSendingDao;
import com.sioo.db.mongo.MongoManager;
import com.sioo.hy.cmpp.vo.SendingVo;
import com.sioo.log.LogInfo;
import com.sioo.util.ConstantStatus;
import com.sioo.util.RabbitMQProducerUtil;
import com.sioo.util.SmsCache;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by morrigan on 2017/5/16.
 */
public class SmsUserSendingSync implements Runnable {

    private RabbitMQProducerUtil util = null;
    private MongoManager mongo = null;

    public SmsUserSendingSync(RabbitMQProducerUtil util, MongoManager mongo) {
        this.util = util;
        this.mongo = mongo;
    }

    private static Logger log = Logger.getLogger(SmsSendReleaseSync.class);

    public void run() {
        SmsCache.QUEUE_CHECK_SENDING_FLG = false;
        LocalQueueCache localQueueCache = LocalQueueCache.getInstance();
        List<SendingVo> passList = null;
        while (true) {
            if (SmsCache.CONTROL) {
                try {
                    passList = SmsUserSendingDao.getInstance().getSmsUserSending();
                    if (passList == null || passList.isEmpty()) {
                        // log.info("release is empty,sleep...");
                        SmsCache.QUEUE_CHECK_SENDING_FLG = true;
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            log.error("sleep error!");
                        }
                    } else {
                        // log.debug("get release size:" + passList.size());
                        SmsCache.QUEUE_CHECK_SENDING_FLG = false;
                        List<Long> ids = new ArrayList<Long>();
                        StringBuffer stb = new StringBuffer();
                        DBObject where = null;
                        DBObject set = null;
                        for (SendingVo vo : passList) {
                            try {
                                if (vo.getHandStat() == 1) {
                                    stb.append("切换通道,mobile:"+vo.getMobile());
                                    util.send("EXAMINE_QUEUE_TEMP", vo);
                                } else if (vo.getHandStat() == 2) {
                                    long id = vo.getId();
                                    stb.append("队列驳回。mobile:"+vo.getMobile());
                                    vo.setId(vo.getHisids());
                                    // 返回状态报告
                                    vo.setRptStat(ConstantStatus.SYS_STATUS_REJECT);
                                    localQueueCache.putReport(vo);

                                    where = new BasicDBObject();
                                    where.put("a", vo.getHisids());

                                    set = new BasicDBObject();
                                    set.put("r", vo.getContentNum());
                                    set.put("j", vo.getContentNum());
                                    set.put("s", 1);
                                    mongo.update2("sms_send_history_unknown", set, where);

                                    ids.add(id);
                                    BasicDBObject set0 = new BasicDBObject();
                                    set0.put("e", (int)0);//Arrive_succ
                                    set0.put("f", vo.getContentNum());//Arrive_fail
                                    BasicDBObject where0 = new BasicDBObject();
                                    where0.put("b",vo.getUid());//uid
                                    where0.put("a", Long.parseLong((vo.getSenddate()+"").substring(0, 8)));//time
                                    mongo.update3("sms_user_day_count", set0, where0);
                                } else {
                                    stb.append("队列状态不正确。mobile:"+vo.getMobile()+",handStat:"+vo.getHandStat());
                                }

                                log.info(stb.toString());
                                stb.setLength(0);

                            } catch (Exception e) {
                                LogInfo.getLog().errorAlert("处理队列信息异常", "[SmsSendReleaseSync.run() Exception]; data=" + JSON.toJSONString(vo) + LogInfo.getTrace(e));
                            }
                        }
                        passList.isEmpty();
                        passList = null;
                    }
                } catch (Exception e) {
                    // 异常消息打印
                    if (passList != null && !passList.isEmpty()) {
                        StringBuffer stb = new StringBuffer();
                        for (SendingVo vo : passList) {
                            stb.append(JSON.toJSONString(vo)).append("\r\n");
                        }
                        LogInfo.getLog().errorData(stb.toString());
                    }
                    LogInfo.getLog().errorAlert("获取审核信息异常", "[SmsSendReleaseSync.run() Exception,  release sms failed]" + LogInfo.getTrace(e));
                    log.error("[SmsSendReleaseSync error];Msg: " + e.getMessage(), e);
                } finally {
                    SmsCache.QUEUE_CHECK_RELEASE_FLG = true;
                }
            } else {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {

                }
            }
        }

    }
}
