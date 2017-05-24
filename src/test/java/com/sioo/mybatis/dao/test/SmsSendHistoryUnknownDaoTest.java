package com.sioo.mybatis.dao.test;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sioo.dao.SmsSendHistoryUnknownDao;
import com.sioo.db.druid.DBUtils;
import com.sioo.db.mongo.MongoManager;
import com.sioo.hy.cmpp.vo.SendingVo;
import com.sioo.util.DateUtils;
import com.sioo.util.SmsCache;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SmsSendHistoryUnknownDaoTest {
    MongoManager mongo = MongoManager.getInstance();

    private static ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void testSaveSmsSendHistoryUnknown() throws Exception {
        SendingVo vo = new SendingVo();
        vo.setId(12356L);
        vo.setMtype(1);
        vo.setSenddate(20160919103100L);
        vo.setUid(83535000);
        vo.setChannel(1);
        vo.setMobile(18621009986L);
        vo.setContent("亲爱的华视会员，您好：您在我公司八婆购买的隐形眼镜即将到使用期限，建议您抽空来我公司进行复查。回T退订【华视眼镜】");
        vo.setContentNum(1);
        vo.setStat(-4);
        vo.setPid(156);
        vo.setMtStat("八婆");
        vo.setLocation("1");
        SmsSendHistoryUnknownDao.getInstance().saveSmsSendHistoryUnknown(vo, mongo);
    }

    @Test
    public void test() throws Exception {
        SmsCache.mongoHost = "101.227.68.21";
        SmsCache.mongoPort = 27017;
        SmsCache.mongoDbName = "SMS";
        List<DBObject> dbObjList = null;
        String sql = "select `id`,  `mtype`,  `senddate`,   `uid`,  `mobile`,  `channel`,  `content`,  `contentNum`,  `succ`"
                + ",`fail`,  `mtstat`,  `pid`,`grade`, `expid`,`location`,  `arrive_fail`, `stat` from smshy.`sms_send_history_unknown` where `mobile`=13501683367 and `senddate`>20170424000000 and `arrive_fail`>0";
        List<Map<String, Object>> smsSendHistoryList = DBUtils.getInstance().findModelRows(sql);
        if (smsSendHistoryList != null && smsSendHistoryList.size() > 0) {
            // 转换为mongodb数据并保存
            dbObjList = new ArrayList<DBObject>();
            for (Map<String, Object> map : smsSendHistoryList) {
                String msg = JSONObject.toJSONString(map);
                msg=msg.replace("mtstat","mtStat");
                SendingVo vo = MAPPER.readValue(msg, SendingVo.class);
                dbObjList.add(convertBean(vo));
            }
            mongo.batchInsert("sms_send_history_unknown", dbObjList);
        }
    }

    private DBObject convertBean(SendingVo vo) {
        DBObject smsSendHistoryUnknown = new BasicDBObject();
        smsSendHistoryUnknown.put("a", vo.getId()); // id
        smsSendHistoryUnknown.put("b", 0); // stype
        smsSendHistoryUnknown.put("c", vo.getMtype()); // mtype
        smsSendHistoryUnknown.put("d", DateUtils.getTime()); // senddate
        smsSendHistoryUnknown.put("e", vo.getUid()); // uid
        smsSendHistoryUnknown.put("f", Long.valueOf(vo.getMobile()));// mobile
        smsSendHistoryUnknown.put("g", vo.getChannel());// channel
        smsSendHistoryUnknown.put("h", vo.getContent());// content
        smsSendHistoryUnknown.put("i", vo.getContentNum());// contentNum
        smsSendHistoryUnknown.put("j", vo.getSucc());// succ
        smsSendHistoryUnknown.put("k", vo.getFail());// fail
        smsSendHistoryUnknown.put("l", vo.getMtStat() == null ? 0 : vo.getMtStat());// mtstat
        smsSendHistoryUnknown.put("m", vo.getPid());// pid
        smsSendHistoryUnknown.put("n", vo.getGrade());// grade
        smsSendHistoryUnknown.put("o", vo.getExpid());// expid
        smsSendHistoryUnknown.put("p", vo.getLocation());// location
        smsSendHistoryUnknown.put("q", 0);// arrive_succ
        smsSendHistoryUnknown.put("r", vo.getArrive_fail());// arrive_fail
        smsSendHistoryUnknown.put("s",0);// stat
        return smsSendHistoryUnknown;
    }
}
