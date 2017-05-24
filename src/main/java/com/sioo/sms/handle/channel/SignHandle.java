package com.sioo.sms.handle.channel;

import com.alibaba.fastjson.JSON;
import com.sioo.cache.*;
import com.sioo.dao.SmsUserSignDao;
import com.sioo.hy.cmpp.vo.SendingVo;
import com.sioo.log.LogInfo;
import com.sioo.service.model.UserSign;
import com.sioo.util.*;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class SignHandle {
    private SignHandle() {
    }

    private static Logger log = Logger.getLogger(SignHandle.class);
    private static SignHandle signHandle;

    public static SignHandle getInstance() {
        if (signHandle != null) {
            return signHandle;
        }
        synchronized (SignHandle.class) {
            if (signHandle == null) {
                signHandle = new SignHandle();
            }
        }
        return signHandle;
    }

    /***
     * 校验签名和拓展
     * @param vo
     * @param user_map
     * @return
     */
    public SendingVo checkSignAndExpend(SendingVo vo, Map<String, Object> user_map, RabbitMQProducerUtil util) {
        //签名类型：0强制签名  1自定义拓展
        int expidSign = (Integer) user_map.get("expidSign");
        //用户移动通道
        int userMobileChannel = (Integer) user_map.get("mobile");
        //用户签名位置
        int signposition = (Integer) user_map.get("signPosition");

        /**
         * 1:渠道用户,2:终端客户
         */
        int usertype = (Integer) user_map.get("usertype");
        //没有签名的先追加签名，无签名不处理，防止路由到其他通道
        vo = addSign(vo, expidSign, userMobileChannel, signposition, usertype);
        /**
         * 自定义,渠道,无签名,直接失败
         */
        if (!vo.getContent().startsWith("【") && !vo.getContent().endsWith("】") && usertype == 1 && expidSign == 1) {
            log.info("content is not find sign,content: " + vo.getContent());
            //如果没有签名直接返回失败
            vo.setStat(ConstantSys.SUBMIT_RESULT_SUCCESS);
            vo.setRptStat(ConstantStatus.USER_STATUS_NOSIGN);
            vo.setAutoFlag(ConstantSys.AUTO_FLAG_HISTORY);
            return vo;
        }
        //校验通道黑签名，防止重复记录
        vo = checkBlackSign(vo);
        if (vo.getAutoFlag() == ConstantSys.AUTO_FLAG_HISTORY) {
            return vo;
        }

        // 先判断用户路由，防止信息被路由到移动通道
        vo = setRoute(vo, (Integer) user_map.get("userkind"));
        //判断屏蔽地区路由
        vo = setLocationRoute(vo);
        //判断是否是通道组
        vo = checkGroupChannel(vo, util);
        //替换双签名
        vo.setContent(replaceDoubleSign(vo.getContent(), signposition, vo.getChannel()));

        //签名判断2中情况：1，找签名，2，记录签名。
        //只有强制签名和无签名，需要找签名
        if (expidSign == 0 || (!vo.getContent().startsWith("【") && !vo.getContent().endsWith("】"))) {// 强制签名
            //找签名
            /**
             * 校验签名,判断签名正不正确
             */
            vo = this.findSign(vo, userMobileChannel, signposition, expidSign);
            if (vo.getAutoFlag() == ConstantSys.AUTO_FLAG_HISTORY) {
                return vo;
            }
            /**
             * 自定义签名
             */
        } else {
            //自定义拓展
            //判断流程有问题，现金卡不发送签名上来
//			if(!vo.getContent().startsWith("【")&&!vo.getContent().endsWith("】")){
//				log.info("content is not find sign,content: "+vo.getContent());
//				//如果没有签名直接返回失败
//				vo.setStat(ConstantSys.SUBMIT_RESULT_SUCCESS);
//				vo.setRptStat(ConstantStatus.USER_STATUS_NOSIGN);
//				vo.setAutoFlag(ConstantSys.AUTO_FLAG_HISTORY);
//				return vo;
//			}else{
            // 匹配签名和拓展
            String[] signArray = this.fetchContentSign(vo, signposition);
            //记录重复签名条数，0条为不重复
            int repeatSignNum = (user_map.get("repeatSign") == null || (Integer) user_map.get("repeatSign") == 0) ? 0 : (user_map.get("repeatSignNum") == null ? 0 : (Integer) user_map.get("repeatSignNum"));
            vo = matchSignAndExpend(vo, signArray, userMobileChannel, user_map.get("usertype") == null ? 2 : (Integer) user_map.get("usertype"), repeatSignNum);
            if (vo.getAutoFlag() == ConstantSys.AUTO_FLAG_HISTORY) {
                return vo;
            }
//			}
        }

        //校验通道签名位置
        vo = checkChannelSignPosition(vo);

        return vo;
    }

    /***
     * 校验黑qian
     * @param vo
     * @return
     */
    public SendingVo checkBlackSign(SendingVo vo) {
        String content = vo.getContent();
        String temp_sign = null;
        String temp_sign2 = null;
        if (content.startsWith("【") && !content.endsWith("】")) {
            // 只有头部有签名
            temp_sign = content.substring(0, content.indexOf("】") + 1);
        } else if (!content.startsWith("【") && content.endsWith("】")) {
            // 只有尾部有签名
            temp_sign = content.substring(content.lastIndexOf("【"));
        } else if (content.startsWith("【") && content.endsWith("】")) {
            // 头部尾部都有签名
            temp_sign = content.substring(0, content.indexOf("】") + 1);// 前面的签名
            temp_sign2 = content.substring(content.lastIndexOf("【"));// 后面的签名
        }

        // 校验黑签名
        String result = StrategyGroupCache.getInstance().checkStrategyBlackSign(vo.getUid(), temp_sign,vo.getMobile());
        if (result != null && result.length() > 0) {
            vo.setStat(ConstantSys.SUBMIT_RESULT_SUCCESS);
            vo.setRptStat(ConstantStatus.SYS_STATUS_BLACKSIGN);
            vo.setMtStat(result);
            vo.setAutoFlag(ConstantSys.AUTO_FLAG_HISTORY);
            return vo;
        }

        if (temp_sign2 != null) {
            result = StrategyGroupCache.getInstance().checkStrategyBlackSign(vo.getUid(), temp_sign2,vo.getMobile());
            if (result != null && result.length() > 0) {
                vo.setStat(ConstantSys.SUBMIT_RESULT_SUCCESS);
                vo.setRptStat(ConstantStatus.SYS_STATUS_BLACKSIGN);
                vo.setMtStat(result);
                vo.setAutoFlag(ConstantSys.AUTO_FLAG_HISTORY);
                return vo;
            }
        }
        return vo;
    }

    /***
     * 校验通道签名位置
     * @param vo
     * @return
     */
    private SendingVo checkChannelSignPosition(SendingVo vo) {
        Map<String, Object> channelInfo = ChannelCache.getInstance().getChannel(vo.getChannel());
        if (channelInfo != null) {
            int signposition = channelInfo.get("sign_position") == null ? -1 : (Integer) channelInfo.get("sign_position");
            if (signposition != -1) {
                String sign = null;
                if (vo.getContent().startsWith("【") && !vo.getContent().endsWith("】")) {
                    // 内容中签名为前置
                    sign = vo.getContent().substring(0, vo.getContent().indexOf("】") + 1);
                    if (signposition == 2 && sign != null) {
                        // 通道名位置为后置，将签名移到后面
                        vo.setContent(vo.getContent().replace(sign, "") + sign);
                    }

                } else if (!vo.getContent().startsWith("【") && vo.getContent().endsWith("】")) {
                    // 内容中签名为后置
                    sign = vo.getContent().substring(vo.getContent().lastIndexOf("【"));
                    if (signposition == 1 && sign != null) {
                        // 用户签名位置为前置，将签名移到前面
                        vo.setContent(sign + vo.getContent().replace(sign, ""));
                    }
                }
            }
        }
        return vo;
    }

    /***
     * 匹配签名和拓展
     *
     * @param vo
     * @param signArray
     * @return
     */
    public SendingVo matchSignAndExpend(SendingVo vo, String[] signArray, int userMobileChannel, int userType, int repeatSignNum) {
        try {
            // 获取用户推送拓展号并初始化
            String expid = vo.getExpid();
            boolean isValidate = true;
            // 221通道需要校验签名报备(不管实际发送通道是否为211)
            if (userMobileChannel == 1 || vo.getChannel() == 1) {
                boolean isExist = false;
                // 如果为自定义拓展，判断拓展是否存在
                if (null != signArray[0]) {
                    String t = UserSignCache.getInstance().isSignReport(vo.getUid(), expid, signArray[0], repeatSignNum);
                    if (t != null) {
                        isExist = true;
                        if (vo.getExpid() == null || !vo.getExpid().startsWith(t)) {
                            //拓展向后兼容，如果发送拓展为签名库拓展开头的 ，不修改拓展发送
                            vo.setExpid(t);
                        }
                    }
                }
                if (!isExist && signArray.length > 1) {
                    String t = UserSignCache.getInstance().isSignReport(vo.getUid(), expid, signArray[1], repeatSignNum);
                    if (t != null) {
                        isExist = true;
                        if (vo.getExpid() == null || !vo.getExpid().startsWith(t)) {
                            //拓展向后兼容，如果发送拓展为签名库拓展开头的 ，不修改拓展发送
                            vo.setExpid(t);
                        }
                    }
                }
                //没找到报备的拓展，校验失败
                if (!isExist) {
                    isValidate = false;
                }
            } else {
                // 获取用户签名对应拓展信息
                UserSign userSign = null;
                if (null != signArray[0]) {
                    userSign = UserSignCache.getInstance().getUserSign(vo.getUid(), vo.getExpid(), signArray[0], repeatSignNum);
                }
                if (userSign == null && signArray.length > 1) {
                    userSign = UserSignCache.getInstance().getUserSign(vo.getUid(), vo.getExpid(), signArray[1], repeatSignNum);
                }

                Map<String, Object> mpChannel = ChannelCache.getInstance().getChannel(vo.getChannel());
                // 通道签名报备方式 0为无,1为先报备后发,2为先发后报备
                int reportChannelSign = (Integer) mpChannel.get("record_type");
                String currentExpend = null;
                // 判断拓展是否存在
                if (userSign != null) {
                    currentExpend = userSign.getExpend();
                    int signStatus = userSign.getStatus();
                    // 通道是先报备后发，签名未报备，处理为失败状态
                    if (reportChannelSign == 1 && signStatus == 0) {
                        isValidate = false;
                    } else {
                        // 如果消息拓展是已用户签名对应拓展
                        if (vo.getExpid() != null && vo.getExpid().startsWith(currentExpend)) {
                            isValidate = true;
                        } else {
                            vo.setExpid(currentExpend);
                        }
                    }
                } else {
                    if (vo.getMtype() == 1) {
                        // 如果为移动，生成拓展并保存，校验是否需要报备
                        // 获取用户类型:1渠道用户 2普通用户

                        if (signArray[0] != null) {
                            currentExpend = saveSmsSignChannel(vo, signArray[0], userType, repeatSignNum);
                        }
                        if (signArray.length > 1) {
                            currentExpend = saveSmsSignChannel(vo, signArray[1], userType, repeatSignNum);
                        }
                        vo.setExpid(currentExpend);

                        // 第一次生成的拓展，肯定是未备的，如果通道要求先报备后发，校验失败
                        if (reportChannelSign == 1) {
                            isValidate = false;
                        }
                    } else {
                        // 如果为联通或电信，设置拓展为UID+拓展号.如果拓展为空，推送用户ID,如果拓展不等于用户ID,推送用户ID+拓展
                        if (vo.getExpid() == null || vo.getExpid().trim().length() == 0) {
                            vo.setExpid(vo.getUid() + "");
                        }
                    }

                }
            }

            if (!isValidate) {
                log.info("签名拓展校验失败，uid: " + vo.getUid() + " expend: " + vo.getExpid());
                vo.setStat(ConstantSys.SUBMIT_RESULT_SUCCESS);
                vo.setRptStat(ConstantStatus.SYS_STATUS_NOREPORTSIGN);
                vo.setAutoFlag(ConstantSys.AUTO_FLAG_HISTORY);
                return vo;
            }

            // TODO 20161010 如果拓展是空的，添加uid
            if (vo.getExpid() != null && vo.getExpid().trim().length() == 0) {
                vo.setExpid(String.valueOf(vo.getUid()));
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert(
                    "用户签名和拓展匹配校验异常",
                    "[ChannelHandle.matchSignAndExpend() Exception]; data: "
                            + JSON.toJSONString(vo) + LogInfo.getTrace(e));
        }
        return vo;
    }

    /***
     * 保存用户签名拓展
     *
     * @param vo
     * @param sign
     * @param userType
     * @return
     */
    private synchronized String saveSmsSignChannel(SendingVo vo, String sign,
                                                   int userType, int repeatSignNum) {
        String maxExpend = "";
        try {
            // 这里重新找一遍，防止插入重复记录
            UserSign userSign = UserSignCache.getInstance().getUserSign(
                    vo.getUid(), vo.getExpid(), sign, repeatSignNum);
            if (userSign != null) {
                return userSign.getExpend();
            }

            userSign = new UserSign();
            userSign.setUid(vo.getUid());
            userSign.setChannel(0);
            userSign.setStore(sign);
            userSign.setStatus(0);
            userSign.setAddtime(new Date());
            userSign.setType(2);
            userSign.setUserstat(1);
            // 用户推送拓展：UID+拓展号
            if (!vo.getExpid().startsWith(vo.getUid() + "")) {
                userSign.setUserexpend(vo.getUid() + "" + vo.getExpid());
            } else {
                userSign.setUserexpend(vo.getExpid());
            }

            if (vo.getExpid() != null
                    && !vo.getExpid().isEmpty()
                    && !UserSignCache.getInstance().isCurrentExpend(vo.getUid() + "" + vo.getExpid())
                    && userType == 1) {
                // 如果推送expid在缓存中不存在则直接保存
                userSign.setExpend(vo.getUid() + "" + vo.getExpid());

                int saveResult = SmsUserSignDao.getInstance().insertUserSign(
                        userSign);
                // 如果保存成功，将签名放入缓存中,并返回拓展。否则继续生成
                if (saveResult > 0) {
                    log.info("添加用户签名：" + sign + ",拓展：" + vo.getExpid());
                    UserSignCache.getInstance().addUserSign(userSign);
                    return userSign.getExpend();
                }
            }

            maxExpend = UserSignCache.getInstance().getMaxExpend(vo.getUid(),
                    userType);

            if (maxExpend != null && !maxExpend.isEmpty()) {
                int saveResult = 0; // 保存签名结果
                int i = 0;// 循环次数，如果大于3次不执行，防止死循环
                do {
                    // 判断是否存在
                    String expend = maxExpend;
                    if (userType == 1) {
                        if (!expend.startsWith(vo.getUid() + "")) {
                            expend = vo.getUid() + "" + expend;
                        }
                    }

                    boolean isCurrent = UserSignCache.getInstance()
                            .isCurrentExpend(expend);
                    if (isCurrent || i > 0) {
                        long currentExpend = Long.parseLong(maxExpend);

                        if (userType == 2) {
                            // 普通用户从缓存中取
                            currentExpend = SmsCache.MAX_EXPEND2
                                    .incrementAndGet();
                        } else {
                            // 渠道用户增长200
                            currentExpend = currentExpend + 200;
                        }

                        String currentExpendStr = currentExpend + "";
                        int size = maxExpend.length()
                                - currentExpendStr.length();
                        if (size > 0) {
                            for (int j = 0; j < size; j++) {
                                currentExpendStr = "0" + currentExpendStr;
                            }
                        }
                        maxExpend = currentExpendStr;
                        if (userType == 1) {
                            expend = vo.getUid() + "" + maxExpend;
                        }
                    }

                    userSign.setExpend(expend);
                    if (userType == 1) {
                        userSign.setExpendqd(maxExpend);
                    } else {
                        userSign.setExpend2(maxExpend);
                    }
                    saveResult = SmsUserSignDao.getInstance().insertUserSign(
                            userSign);
                    i++;
                } while (saveResult < 1 && i < 3); // 防止死循环，只尝试添加3次

                // 如果保存成功，将签名放入缓存中
                if (saveResult > 0) {
                    log.info("添加用户签名：" + sign + ",拓展：" + userSign.getExpend());
                    maxExpend = userSign.getExpend();
                    UserSignCache.getInstance().addUserSign(userSign);
                } else {
                    // 如果失败，并且尝试了五次，记录错误日志
                    if (i >= 3) {
                        log.error("生成拓展失败; data: "
                                + JSON.toJSONString(userSign));
                        return userSign.getExpend();
                    }
                }
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert(
                    "保存用户签名异常",
                    "[ChannelHandle.saveSmsSignChannel() Exception]; data: "
                            + JSON.toJSONString(vo) + LogInfo.getTrace(e));
        }
        return maxExpend;
    }

    /****
     * 获取信息签名
     * @param vo
     * @return
     */
    private String[] fetchContentSign(SendingVo vo, int signposition) {
        // 获取签名个数，双签名只可能在头尾
        int signSize = 1;
        if (vo.getContent().startsWith("【")
                && vo.getContent().endsWith("】")) {
            signSize = 2;
        }

        // 获取签名
        String[] signArray = new String[signSize];
        if (vo.getContent().startsWith("【") && !vo.getContent().endsWith("】")) {
            // 内容中签名为前置
            signArray[0] = vo.getContent().substring(0, vo.getContent().indexOf("】") + 1);
            if (signposition == 2) {
                // 用户签名位置为后置，将签名移到后面
                vo.setContent(vo.getContent().replace(signArray[0], "") + signArray[0]);
            }

        } else if (!vo.getContent().startsWith("【") && vo.getContent().endsWith("】")) {
            // 内容中签名为后置
            signArray[0] = vo.getContent().substring(vo.getContent().lastIndexOf("【"));
            if (signposition == 1) {
                // 用户签名位置为前置，将签名移到前面
                vo.setContent(signArray[0] + vo.getContent().replace(signArray[0], ""));
            }
        } else if (vo.getContent().startsWith("【") && vo.getContent().endsWith("】")) {
            // 如果是双签名，不需要判断签名位置直接获取签名
            signArray[0] = vo.getContent().substring(0, vo.getContent().indexOf("】") + 1);
            signArray[1] = vo.getContent().substring(vo.getContent().lastIndexOf("【"));
        }
        return signArray;
    }

    /***
     * 追加签名
     * @param vo
     * @param expidSign
     * @param userMobileChannel
     * @param signposition
     * @return
     */
    private SendingVo addSign(SendingVo vo, int expidSign, int userMobileChannel, int signposition, int usertype) {

        //如果为强制签名,并且确实提交了签名,并且是渠道用户
        if (expidSign == 0 && hasStore(vo.getContent()) && usertype == 1) {
            log.info("user expidSing=0 and content has sign, uid:" + vo.getUid() + ", content:" + vo.getContent());
            return vo;
        }
        UserSign userSign = null;
        //如果不存在签名或为强制签名都先找签名
        if ((!hasStore(vo.getContent()) && usertype == 2) || expidSign == 0) {
            List<UserSign> signs = null;
            /**
             * 221移动的通道
             */
            if (userMobileChannel == 1 || vo.getChannel() == 1) {
                signs = SmsCache.CHANNEL_SIGN.get(vo.getUid());
            } else {
                signs = SmsCache.USER_SIGN.get(vo.getUid());
            }

            //没找到签名返回
            if (signs == null || signs.size() == 0) {
                return vo;
            }

            //通道在移动221上或者用户移动通道在221
            if (userMobileChannel == 1 || vo.getChannel() == 1) {
                // 只有一个签名
                if (signs.size() == 1) {
                    userSign = signs.get(0);
                } else {
                    // 找到多个签名
                    for (UserSign us : signs) {
                        if (us.getExpend().equals(vo.getUid() + "")) {
                            userSign = us;
                            break;
                        }
                    }
                }
            } else {
                //先找签名库是否有对应签名
                for (UserSign us : signs) {
                    if (vo.getContent().startsWith(us.getStore()) || vo.getContent().endsWith(us.getStore())) {
                        userSign = us;
                        break;
                    }
                }
                //否则拿uid等于拓展的那个签名
                if (userSign == null) {
                    for (UserSign us : signs) {
                        if (us.getExpend().equals(vo.getUid() + "")) {
                            userSign = us;
                            break;
                        }
                    }
                }
                // 如果没找到uid=expid对应的签名，从签名列表中拿一个
                if (userSign == null) {
                    userSign = signs.get(0);
                }
            }

            //如果找到签名
            if (userSign != null) {
                //签名为空或强制签名没找到并且是终端用户
                if ((!hasStore(vo.getContent()) || (expidSign == 0 && vo.getContent().indexOf(userSign.getStore()) == -1)) && usertype == 2) {
                    if (signposition == 1 || signposition == 3) {// 前置
                        vo.setContent(userSign.getStore() + vo.getContent());
                    } else if (signposition == 2) {
                        vo.setContent(vo.getContent() + userSign.getStore());
                    }
                    if (vo.getExpid() == null || !vo.getExpid().startsWith(userSign.getExpend())) {
                        //拓展向后兼容，如果发送拓展为签名库拓展开头的 ，不修改拓展发送
                        vo.setExpid(userSign.getExpend());
                    }
                } else {
                    if (signposition == 1 && !vo.getContent().startsWith(userSign.getStore())) {// 前置
                        vo.setContent(userSign.getStore() + vo.getContent().replace(userSign.getStore(), ""));
                    } else if (signposition == 2 && !vo.getContent().endsWith(userSign.getStore())) {
                        vo.setContent(vo.getContent().replace(userSign.getStore(), "") + userSign.getStore());
                    }
                }
            }
        }
        return vo;
    }

    /****
     *
     * @param vo
     * @param userMobileChannel
     * @param signposition
     * @return
     */
    private SendingVo findSign(SendingVo vo, int userMobileChannel, int signposition, int expidSign) {
        //找签名列表
        List<UserSign> signs = null;
        if (userMobileChannel == 1 || vo.getChannel() == 1) {
            signs = SmsCache.CHANNEL_SIGN.get(vo.getUid());
        } else {
            signs = SmsCache.USER_SIGN.get(vo.getUid());
        }
        //没找到签名列表直接就失败
        if (signs == null || signs.size() == 0) {
            log.info("not found sign list, uid:" + vo.getUid());
            vo.setStat(ConstantSys.SUBMIT_RESULT_SUCCESS);
            vo.setRptStat(ConstantStatus.SYS_STATUS_NOREPORTSIGN);
            vo.setAutoFlag(ConstantSys.AUTO_FLAG_HISTORY);
            return vo;
        }
        UserSign userSign = null;
        //如果是221通道
        if (userMobileChannel == 1 || vo.getChannel() == 1) {
            //如果不存在签名
            if (!hasStore(vo.getContent())) {
                // 只有一个签名
                if (signs.size() == 1) {
                    userSign = signs.get(0);
                } else {
                    // 找到多个签名
                    for (UserSign us : signs) {
                        if (us.getExpend().equals(vo.getUid() + "")) {
                            userSign = us;
                            break;
                        }
                    }
                }
                // 如果签名未报备，处理失败
                if (userSign == null || userSign.getStatus() == 0) {
                    log.info("content is not sign, not found expend eq uid channel sign, uid:" + vo.getUid());
                    vo.setStat(ConstantSys.SUBMIT_RESULT_SUCCESS);
                    vo.setRptStat(ConstantStatus.SYS_STATUS_NOREPORTSIGN);
                    vo.setAutoFlag(ConstantSys.AUTO_FLAG_HISTORY);
                    return vo;
                }

                if (signposition == 1 || signposition == 3) {// 前置
                    vo.setContent(userSign.getStore() + vo.getContent());
                } else if (signposition == 2) {
                    vo.setContent(vo.getContent() + userSign.getStore());
                }
                vo.setExpid(userSign.getExpend());
            } else {
                //96822拓展为0的weit
                for (UserSign us : signs) {
                    if (vo.getContent().startsWith(us.getStore()) || vo.getContent().endsWith(us.getStore())) {
                        userSign = us;
                        break;
                    }
                }

                // 如果签名未报备，处理失败
                if (userSign == null || userSign.getStatus() == 0) {
                    log.info("content is not sign, not found channel sign by sign, uid:" + vo.getUid());
                    vo.setStat(ConstantSys.SUBMIT_RESULT_SUCCESS);
                    vo.setRptStat(ConstantStatus.SYS_STATUS_NOREPORTSIGN);
                    vo.setAutoFlag(ConstantSys.AUTO_FLAG_HISTORY);
                    return vo;
                }

                //设置为前置后位置未改变
                if (userSign != null) {
                    if (signposition == 1 || signposition == 3) {// 前置
                        if (!vo.getContent().startsWith(userSign.getStore())) {
                            vo.setContent(userSign.getStore() + vo.getContent().replace(userSign.getStore(), ""));
                        }
                    } else if (signposition == 2) {// 后置
                        if (!vo.getContent().endsWith(userSign.getStore())) {
                            vo.setContent(vo.getContent().replace(userSign.getStore(), "") + userSign.getStore());
                        }
                    }
                }

                if (vo.getExpid() == null || !vo.getExpid().startsWith(userSign.getExpend())) {
                    //拓展向后兼容，如果发送拓展为签名库拓展开头的 ，不修改拓展发送
                    vo.setExpid(userSign.getExpend());
                }
            }
        } else {
            //先找签名库是否有对应签名
            /**
             * 系统的签名和客户提交的签名做匹配
             */
            for (UserSign us : signs) {
                if (vo.getContent().startsWith(us.getStore()) || vo.getContent().endsWith(us.getStore())) {
                    userSign = us;
                    break;
                }
            }

            //如果是强制签名，并且签名在签名库中不存在，返回失败，不追加
            /**
             * 提交的签名不匹配,直接失败
             */
            if (expidSign == 0 && userSign == null) {
                log.info("user expidSign=0 and not find sign, uid:" + vo.getUid());
                vo.setStat(ConstantSys.SUBMIT_RESULT_SUCCESS);
                vo.setRptStat(ConstantStatus.SYS_STATUS_NOREPORTSIGN);
                vo.setAutoFlag(ConstantSys.AUTO_FLAG_HISTORY);
                return vo;
            }

            //否则拿uid等于拓展的那个签名
            if (userSign == null) {
                for (UserSign us : signs) {
                    if (us.getExpend().equals(vo.getUid() + "")) {
                        userSign = us;
                        break;
                    }
                }
            }

            // 如果没找到uid=expid对应的签名，从签名列表中拿一个
            if (userSign == null) {
                userSign = signs.get(0);
            }


            //如果不存在签名,先追加一个签名
            if (!vo.getContent().startsWith("【") && !vo.getContent().endsWith("】")) {
                vo.setContent(userSign.getStore() + vo.getContent());
            }

            if (signposition == 1 || signposition == 3) {// 前置
                if (!vo.getContent().startsWith(userSign.getStore())) {
                    vo.setContent(userSign.getStore() + vo.getContent().replace(userSign.getStore(), ""));
                }
            } else if (signposition == 2) {// 后置
                if (!vo.getContent().endsWith(userSign.getStore())) {
                    vo.setContent(vo.getContent().replace(userSign.getStore(), "") + userSign.getStore());
                }
            }

            if (vo.getExpid() == null || !vo.getExpid().startsWith(userSign.getExpend())) {
                //拓展向后兼容，如果发送拓展为签名库拓展开头的 ，不修改拓展发送
                vo.setExpid(userSign.getExpend());
            }

            // 通道签名报备方式 0为无,1为先报备后发,2为先发后报备
            Map<String, Object> mpChannel = ChannelCache.getInstance().getChannel(vo.getChannel());
            int reportChannelSign = (Integer) mpChannel.get("record_type");

            // 通道是先报备后发，签名未报备，处理为失败状态
            if (reportChannelSign == 1 && userSign.getStatus() == 0) {
                log.info("user sign is not report, uid:" + vo.getUid() + " ,sign:" + userSign.getStore());
                vo.setStat(ConstantSys.SUBMIT_RESULT_SUCCESS);
                vo.setRptStat(ConstantStatus.SYS_STATUS_NOREPORTSIGN);
                vo.setAutoFlag(ConstantSys.AUTO_FLAG_HISTORY);
            }
        }
        return vo;
    }


    /***
     * 替换双签名
     *
     * @param content
     * @param signposition
     * @return
     */
    private String replaceDoubleSign(String content, int signposition,
                                     int channel) {
        try {
            Map<String, Object> channelInfo = ChannelCache.getInstance()
                    .getChannel(channel);
            int isSigns = 3;
            if (channelInfo != null) {
                isSigns = (Integer) channelInfo.get("is_signs");
            }
            // 替換双签名
            String signTemp = null;
            if (isSigns == 1) {// 不支持双签名
                if (signposition == 1) {
                    signTemp = content.substring(0, content.indexOf("】") + 1);
                    content = content.replace(signTemp, "");
                    content = content.replace("【", "[");
                    content = content.replace("】", "]");
                    content = signTemp + content;
                } else if (signposition == 2) {
                    signTemp = content.substring(content.lastIndexOf("【"));
                    content = content.replace(signTemp, "");
                    content = content.replace("【", "[");
                    content = content.replace("】", "]");
                    content += signTemp;
                }
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert(
                    "替换双签名异常",
                    "[ChannelHandle.replaceDoubleSign(" + content + ","
                            + signposition + "," + channel + ") Exception]"
                            + LogInfo.getTrace(e));
        }
        return content;
    }

    /***
     * 校验通道组
     *
     * @param vo
     * @param util
     * @return
     */
    private SendingVo checkGroupChannel(SendingVo vo, RabbitMQProducerUtil util) {
        try {
            int channel = vo.getChannel();
            Map<String, Object> lastChannelInfo = ChannelCache.getInstance().getChannel(channel);
            // 通道组判断
            int isgroup = 0;
            if (lastChannelInfo != null) {
                isgroup = lastChannelInfo.get("is_group") != null ? Integer.parseInt(String.valueOf(lastChannelInfo.get("is_group"))) : 0;
            }
            // 为通道组，
            if (isgroup == 1) {
                // 获取该通道组下所有子通道ID
                List<Map<String, Object>> channelList = ChannelCache.getInstance().getGroupChannel(channel);
                // 如果通道组列表为空，放入通道队列中
                if (channelList != null && !channelList.isEmpty()) {
                    // 队列大小
                    int minQueueSize = 999999999;
                    // 实际发送的通道ID
                    int minChannelId = Integer.valueOf(channelList.get(0).get("channel_id").toString());
                    int channelId = 0;
                    // 循环取出通道组内容
                    for (Map<String, Object> map : channelList) {
                        // 优先级大于0的子通道
                        if (!map.get("priority").toString().equals("0")) {
                            channelId = Integer.valueOf(map.get("channel_id").toString());
                            Map<String, Object> channelItem = ChannelCache.getInstance().getChannel(channelId);
                            // 获取当前通道流速
                            int pkgnum = Integer.parseInt(String.valueOf(channelItem.get("local_rate")));
                            // 获取当前通道消息队列的大小
                            int queueSize = util.getQueueListSize("SEND_CHANNEL_" + channelId);
                            // 如果通道组中有通道消息队列大小<流速*2直接放入该通道，
                            if (queueSize < pkgnum * 2) {
                                minChannelId = channelId;
                                break;
                            }
                            // 找出通道消息队列大小最小值
                            if (minQueueSize > queueSize) {
                                minQueueSize = queueSize;
                                minChannelId = channelId;
                            }
                        }
                    }
                    vo.setChannel(minChannelId);
                }
            }

        } catch (Exception e) {
            LogInfo.getLog().errorAlert(
                    "校验通道组异常",
                    "[ChannelHandle.checkGroupChannel() Exception]; data: "
                            + JSON.toJSONString(vo) + LogInfo.getTrace(e));
        }
        return vo;
    }

    private SendingVo setLocationRoute(SendingVo vo) {
        if (vo.getLocation() != null && !vo.getLocation().equals("全国,-1")) {
            String provincecode = vo.getLocation().split(",")[1];
            //屏蔽地区路由
            Integer route = UserBlackLocationCache.getInstance().getChannelBlackLocationRoute(vo.getChannel(), provincecode);
            if (route != null && route.intValue() > 0) {
                if (canRoute(vo.getMtype(), route)) {
                    vo.setChannel(route);
                }
            }
        }
        return vo;
    }

    /***
     * 判断用户路由
     *
     * @param vo
     * @return
     */
    private SendingVo setRoute(SendingVo vo, int userkind) {
        try {
            // 判断用户路由,如果是全网可以全部路由，如果只是移动，只路由移动的
            Map<String, Object> routeMap = UserRouteCache.getInstance().getUserRouteChannel(vo.getUid(), vo.getMtype(), vo.getContent(),vo.getMobile()
            ,vo.getProvinceCode(),vo.getCityCode());
            if (routeMap != null) {
                // 获取目的路由的通道ID
                int routeChannelId = Integer.parseInt(String.valueOf(routeMap.get("routechannel")));
                if (canRoute(vo.getMtype(), routeChannelId)) {
                    vo.setChannel(routeChannelId);
                }
            }

            // 判断通道路由
            Map<String, Object> channelInfo = ChannelCache.getInstance()
                    .getChannel(vo.getChannel());
            if (channelInfo == null || channelInfo.get("route_type") == null
                    || channelInfo.get("route_channel") == null) {
                return vo;
            }
            // 获取通道路由类型 0为无路由,1为关键词路由,2为验证码路由,3为通知路由,4为营销路由
            int routeType = (Integer) channelInfo.get("route_type");
            int routeChannel = (Integer) channelInfo.get("route_channel");

            // 判断通道路由
            if (routeType == 0 || routeChannel == 0) {
                // 如果路由类型为0，则不走路由
                return vo;
            } else if (routeType == 1 && routeChannel != 0) {
                // 如果路由类型为1，判断是否含有关键字，然后再路由，
                if (channelInfo.get("route_require") == null) {
                    return vo;
                }
                String routeRequire = channelInfo.get("route_require")
                        .toString();
                String[] routeRequireArray = routeRequire.split(",");
                String content = vo.getContent();
                boolean flg = false;
                for (String words : routeRequireArray) {
                    if (content.contains(words)) {
                        flg = true;
                        break;
                    }
                }
                if (flg) {
                    if (canRoute(vo.getMtype(), routeChannel)) {
                        vo.setChannel(routeChannel);
                    }
                }
            } else if (routeType == 2 && userkind == 1) {
                // 如果路由类型为2，并且用户类别为验证码组
                if (canRoute(vo.getMtype(), routeChannel)) {
                    vo.setChannel(routeChannel);
                }
            } else if (routeType == 3 && userkind == 2) {
                // 如果路由类型为3，并且用户类别为通知组
                if (canRoute(vo.getMtype(), routeChannel)) {
                    vo.setChannel(routeChannel);
                }
            } else if (routeType == 4 && userkind == 3) {
                // 如果路由类型为4，并且用户类别为营销组
                if (canRoute(vo.getMtype(), routeChannel)) {
                    vo.setChannel(routeChannel);
                }
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert(
                    "通道路由校验异常",
                    "[ChannelHandle.setRoute() Exception]; data: "
                            + JSON.toJSONString(vo) + LogInfo.getTrace(e));
        }
        return vo;
    }

    /***
     * 判断通道状态是否可以路由
     *
     * @param mtype
     * @param routeChannelId
     * @return
     */
    private boolean canRoute(int mtype, int routeChannelId) {
        boolean result = false;
        try {
            // 获取目的路由通道信息
            Map<String, Object> routeChannelInfo = ChannelCache.getInstance()
                    .getChannel(routeChannelId);
            if (routeChannelInfo == null) {
                return result;
            }
            // 获取目的路由通道状态
            int status = routeChannelInfo.get("status") == null ? 1 : Integer.valueOf(routeChannelInfo.get("status").toString());
            // 目的路由通道状态是正常则继续往下走
            if (status == 0) {
                // 获取目的路由通道支持网络类型
                if (routeChannelInfo.get("support_network") == null) {
                    return result;
                }
                int support = (Integer) routeChannelInfo.get("support_network");
                // 判断是否可以路由，可以则修改通道ID
                if (isRoute(mtype, support)) {
                    result = true;
                }
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert(
                    "根据目的通道校验是否可以路由校验异常",
                    "[ChannelHandle.canRoute(" + mtype + "," + routeChannelId
                            + ") Exception]" + LogInfo.getTrace(e));
        }
        return result;
    }

    /***
     * 根据运营商类型判断是否支持路由
     *
     * @param mtype
     * @param support
     * @return
     */
    private boolean isRoute(int mtype, int support) {
        boolean isRoute = false;
        try {
            // 路由通道类型
            // support 1全网，2移动。3联通。5电信
            // mtype 1移动。2联通。4电信
            if (support == 1) {
                isRoute = true;
            } else if (support == 2) {
                if (mtype == 1) {
                    isRoute = true;
                }
            } else if (support == 3) {
                if (mtype == 2) {
                    isRoute = true;
                }
            } else if (support == 5) {
                if (mtype == 4) {
                    isRoute = true;
                }
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert(
                    "根据运营商类型是否可以路由校验异常",
                    "[ChannelHandle.isRoute(" + mtype + "," + support
                            + ") Exception]" + LogInfo.getTrace(e));
        }
        return isRoute;
    }

    private boolean hasStore(String content) {
        return (content.startsWith("【") || content.endsWith("】"));
    }

}
