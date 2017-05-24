package com.sioo.cache;

import com.alibaba.fastjson.JSONObject;
import com.sioo.util.MiddleServiceClient;
import com.sioo.util.SmsCache;
import com.sioo.util.UpdateCacheConstant.METHOD;
import com.sioo.util.UpdateCacheConstant.TYPE;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/4/7.
 * 切换用户线路,同步余额
 */
public class ChangeUserLine {

    private static HttpClient client = null;

    private static Logger log = Logger.getLogger(ChangeUserLine.class);
    private static ChangeUserLine changeUserLine = null;

    private static MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

    static {
        client = new HttpClient(connectionManager);
        client.getHttpConnectionManager().getParams().setDefaultMaxConnectionsPerHost(20);
        client.getHttpConnectionManager().getParams().setMaxTotalConnections(48);
        client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
        client.getHttpConnectionManager().getParams().setSoTimeout(30000);
    }

    public static ChangeUserLine getInstance() {
        if (changeUserLine != null) {
            return changeUserLine;
        }
        synchronized (ChangeUserLine.class) {
            if (changeUserLine == null) {
                changeUserLine = new ChangeUserLine();
            }
        }
        return changeUserLine;
    }

    /**
     * 接口切换用户线路
     *
     * @param uid
     * @param line
     */
    public static void excute(Integer uid, Integer line) {
        int serverType = SmsCache.SERVER_TYPE;
        /**
         * 如果切的线路不是当前线路,获取用户目前的余额
         */
        if (serverType != line) {
            SmsCache.USER_LINE_SYNC_STATE.put(uid,1);
            Map<String, Object> map = new HashMap<>();
            map.put("sms", SmsCache.USER_SMS_YUKOU.get(uid));
            map.put("uid", uid);
            /**
             * 发送请求给新线路同步余额
             */

            MiddleServiceClient.getInstance().excuteClient(TYPE.USER_SMS, METHOD.RELOAD, JSONObject.toJSONString(map), SmsCache.LINE.get(line));
            SmsCache.USER_LINE.put(uid, 1);
            log.info("切换线路成功");
        }

    }
}
