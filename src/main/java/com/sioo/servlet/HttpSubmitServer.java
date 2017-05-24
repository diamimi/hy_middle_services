package com.sioo.servlet;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sioo.cache.*;
import com.sioo.log.LogInfo;
import com.sioo.service.StopService;
import com.sioo.util.AESTool;
import com.sioo.util.SignatureUtil;
import com.sioo.util.UpdateCacheConstant.METHOD;
import com.sioo.util.UpdateCacheConstant.TYPE;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

public class HttpSubmitServer extends HttpServlet {
    private static Logger log = Logger.getLogger(HttpSubmitServer.class);

    /**
     *
     */
    private static final long serialVersionUID = -7522881704237308549L;

    private SignatureUtil signatureUtil = new SignatureUtil();

    private AESTool aes = new AESTool();

    public static String SUCC = "1";
    public static String FAIL = "-1";

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) {
        PrintWriter out = null;
        try {
            /** jetty默认UTF-8编码，如果修改成GBK，在这里修改。 */
            request.setAttribute("org.eclipse.jetty.server.Request.queryEncoding", "utf-8");
            response.setContentType("text/html;charset=utf-8");
            out = response.getWriter();

            log.info("visit UpdateCache service");

            // Get request parameters.
            String signature = request.getParameter("s");
            if (StringUtils.isEmpty(signature)) {
                log.info("UpdateCache parameter error, signature is null!");
                return;
            }
            String appid = request.getParameter("a");
            if (StringUtils.isEmpty(appid)) {
                log.info("UpdateCache parameter error, appid is null!");
                return;
            }
            String timestamp = request.getParameter("t");
            if (StringUtils.isEmpty(timestamp)) {
                log.info("UpdateCache parameter error, timestamp is null!");
                return;
            }
            String lol = request.getParameter("l");
            if (StringUtils.isEmpty(lol)) {
                log.info("UpdateCache parameter error, lol is null!");
                return;
            }

            String data = request.getParameter("data");
            if (StringUtils.isEmpty(data)) {
                log.info("UpdateCache parameter error, data is null!");
                return;
            }
            String digest = signatureUtil.digest(data, "MD5");
            if (StringUtils.isEmpty(digest)) {
                log.info("UpdateCache parameter error, digest is null!");
                return;
            }

            long millis = Long.valueOf(timestamp);
            // Check signature and digest.
            if (StringUtils.equals(digest, lol)) {
                if (signatureUtil.isValid(signature, appid, lol, millis)) {
                    try {
                        data = aes.decrypt(data, aes.findKeyById(appid));
                        log.debug("received data:" + data);
                        out.print(doSomeThing(data));
                        out.flush();
                    } catch (Exception e) {
                        log.error(e, e);
                    }
                } else {
                    log.error("invalid signature");
                }
            } else {
                log.error("invalid digest.");
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("接收缓存修改接口消息异常", "[HttpSubmitServer.service() Exception]" + LogInfo.getTrace(e));
        } finally {
            out.close();
        }
    }

    private String doSomeThing(String data) {
        String result = "";
        try {
            log.info("update cache parmeter: " + data);
            JSONObject jsonObject = JSON.parseObject(data);
            Integer type = jsonObject.getInteger("type");
            Integer method = jsonObject.getInteger("method");
            JSONObject json = null;
            JSONArray array = null;
            switch (type) {
                case TYPE.CONTROL:
                    json = jsonObject.getJSONObject("data");
                    result = StopService.getInstance().excute(method, json == null ? null : json.getBooleanValue("control"));
                    break;
                case TYPE.CHANNEL:
                    json = jsonObject.getJSONObject("data");
                    if (json.isEmpty()) {
                        break;
                    }
                    ChannelCache.getInstance().excuteChannel(method, json.getInteger("channelId"), json.getInteger("stat"));
                    break;
                case TYPE.CHANNEL_GROUP:
                    json = jsonObject.getJSONObject("data");
                    ChannelCache.getInstance().excuteChannelGroup(method, json.getInteger("channelId"));
                    break;
                case TYPE.CHANNEL_BLACK_SIGN:
                    json = jsonObject.getJSONObject("data");
                    ChannelBlackSignCache.getInstance().excute(method, json.getInteger("uid"), json.getString("store"));
                    break;
                case TYPE.USER:
                    json = jsonObject.getJSONObject("data");
                    result = UserCache.getInstance().excute(method, json.getInteger("uid"));
                    break;
                case TYPE.USER_ROUTE:
                    json = jsonObject.getJSONObject("data");
                    UserRouteCache.getInstance().excute(method, json.getInteger("uid"), json.getString("content"));
                    break;
                case TYPE.USER_SIGN:
                    json = jsonObject.getJSONObject("data");
                    UserSignCache.getInstance().excute(method, json.getInteger("uid"), json.getString("store"), json.getString("expend"), json.getString("expends"),
                            json.getInteger("type"));
                    break;
                case TYPE.USER_SMS:
                    json = jsonObject.getJSONObject("data");
                    result = UserSmsCache.getInstance().excute(method, json.getInteger("uid"), json.getInteger("sms"));
                    break;
                case TYPE.USER_ALERT:
                    json = jsonObject.getJSONObject("data");
                    UserSmsAlertCache.getInstance().excute(method, json.getInteger("uid"));
                    break;
                case TYPE.USER_WHITE_MOBILE:
                    array = jsonObject.getJSONArray("data");
                    UserWhiteMobileCache.getInstance().excute(method, array);
                    break;
                case TYPE.USER_BLACK_MOBILE:
                    array = jsonObject.getJSONArray("data");
                    UserBlackMobileCache.getInstance().excute(method, array);
                    break;
                case TYPE.USER_WHITE_SIGN:
                    array = jsonObject.getJSONArray("data");
                    UserWhiteSignCache.getInstance().excute(method, array);
                    break;
                case TYPE.USER_BLACK_WORDS:
                    array = jsonObject.getJSONArray("data");
                    UserBlackWordsCache.getInstance().excute(method, array);
                    break;
                case TYPE.USER_MSG_TEMPLATE:
                    array = jsonObject.getJSONArray("data");
                    UserMsgTemplateCache.getInstance().excute(method, array);
                    break;
                case TYPE.USER_BLACK_AREA:
                    json = jsonObject.getJSONObject("data");
                    UserBlackLocationCache.getInstance().excute(method, json.getInteger("uid"), json.getInteger("channelId"));
                    break;
                case TYPE.USER_STRATEGY_GROUP:
                    json = jsonObject.getJSONObject("data");
                    StrategyGroupCache.getInstance().excuteUserStrategyGroup(method, json.getInteger("uid"));
                    break;
                case TYPE.STRATEGY_GROUP:
                    json = jsonObject.getJSONObject("data");
                    result = StrategyGroupCache.getInstance().excuteStrategyGroup(method, json.getInteger("groupType"), json.getInteger("groupId"),
                            json.getString("content"), json.get("screenType") == null ? 0 : json.getInteger("screenType"));
                    break;
                case TYPE.MOBILE_AREA:
                    json = jsonObject.getJSONObject("data");
                    MobileAreaCache.getInstance().excute(method, json.getInteger("mobile"), json.getInteger("id"));
                    break;
                case TYPE.RELEASE_TEMPLATE:
                    json = jsonObject.getJSONObject("data");
                    ReleaseTemplateCache.getInstance().excute(json.getInteger("uid"));
                    break;
                case TYPE.CHANGE_USER_LINE:
                    json = jsonObject.getJSONObject("data");
                    ChangeUserLine.getInstance().excute(json.getInteger("uid"), json.getInteger("line"));
                    break;
                case TYPE.SYS_STRATEGY_GOURP:
                    json = jsonObject.getJSONObject("data");
                    result = StrategyGroupCache.getInstance().excute(method, json.getInteger("groupType"), json.getInteger("groupId"));
                    break;
                case TYPE.REPEAT_MOBILE:
                    if (method == METHOD.GET) {
                        json = jsonObject.getJSONObject("data");
                        if (json == null) {
                            result = RepeatMobileCache.getInstance().getRepeatMobile(null, null, null);
                        } else {
                            result = RepeatMobileCache.getInstance().getRepeatMobile(json.getInteger("page"), json.getInteger("pageSize"), json.getLong("mobile"));
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("处理缓存修改接口信息异常", "[HttpSubmitServer.doSomeThing(" + data + ") Exception]" + LogInfo.getTrace(e));
        }
        return result;
    }
}
