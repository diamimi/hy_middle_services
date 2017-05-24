package com.sioo.sms.handle.sms;

import com.alibaba.fastjson.JSON;
import com.sioo.cache.*;
import com.sioo.hy.cmpp.vo.SendingVo;
import com.sioo.log.LogInfo;
import com.sioo.service.model.UserSign;
import com.sioo.util.*;
import org.apache.log4j.Logger;

import java.util.Calendar;
import java.util.Map;

public class SmsHandle {
    private SmsHandle() {
    }

    private static SmsHandle smsHandle;

    public static SmsHandle getInstance() {
        if (smsHandle != null) {
            return smsHandle;
        }
        synchronized (SmsHandle.class) {
            smsHandle = new SmsHandle();
        }
        return smsHandle;
    }

    private static Logger log = Logger.getLogger(SmsHandle.class);

    // 校验标志.如果为true,直接返回,不对再做下面的判断
    private boolean checkFlg = false;

    public SendingVo checkSms(SendingVo vo, Map<String, Object> user_map, boolean isRepeatMobile) {
        try {
            checkFlg = false;
            // 校验用户白名单

            vo = checkUserWhiteMobile(vo);
            if (checkFlg) {
                log.info(vo.getMobile() + " is UserWhiteMobile");
                vo.setGrade(1);
                return vo;
            }


            // 校验用户黑名单
            vo = checkUserBlackMobile(vo);
            if (checkFlg) {
                log.info(vo.getMobile() + " is UserBlackMobile");
                return vo;
            }

            //校验全量用户黑名单
            vo = checkAllUserBlackMobile(vo, (Integer) user_map.get("blackAll"));
            if (checkFlg) {
                log.info(vo.getMobile() + " is AllUserBlackMobile");
                return vo;
            }
            //校验系统黑名单
            vo = checkUserStrategyGroupBlackMobile(vo);
            if (checkFlg) {
                log.info(vo.getMobile() + " is StrategyGroupBlackMobile");
                return vo;
            }
            // 校验用户屏蔽地区
            vo = checkUserBlackLocation(vo);
            if (checkFlg) {
                log.info(vo.getContent() + " is UserBlackLocation");
                return vo;
            }
            //如果是审核模板中，不校验下面操作
            if (!ReleaseTemplateCache.getInstance().isReleaseTemplate(vo.getUid(), vo.getContent(), vo.getMobile())) {
                // 校验用户模板


                vo = checkUserMsgTemplet(vo);
                if (checkFlg) {
                    log.info(vo.getContent() + " is UserMsgTemplet");
                    return vo;
                }

                /**
                 * 校验用户自动屏蔽词
                 */
                vo = checkUserBlackWordsAuto(vo);
                if (checkFlg) {
                    return vo;
                }

                // 校验用户审核屏蔽词
                vo = checkUserBlackWords(vo);
                if (checkFlg) {
                    return vo;
                }
                // 校验用户策略组
                /**
                 * 审核系统屏蔽词等
                 */
                vo = checkUserStrategyGroup(vo);
                if (checkFlg) {
                    return vo;
                }

                // 校验用户白签名
                vo = checkUserWhiteSign((Integer) user_map.get("signPosition"), vo);
                if (checkFlg) {
                    return vo;
                }

                // 校验夜间审核,补发等操作不需要校验
                if (isRepeatMobile) {
                    boolean checkRelease = checkRelease(vo, (Integer) user_map.get("isRelease"), (Integer) user_map.get("releaseNum"));
                    if (checkRelease) {
                       // log.info(vo.getMobile() + " is release");
                        vo.setRelease(1);
                        vo.setAutoFlag(ConstantSys.AUTO_FLAG_RELEASE);
                    }
                }
            }

            // 校验重号过滤,补发等操作不需要校验
            if (isRepeatMobile) {
                vo = checkRepeatMobile((Integer) user_map.get("repeatNum"), (Integer) user_map.get("repeatFilter"), vo);
                if (checkFlg) {
                    log.info(vo.getMobile() + " is RepeatMobile");
                    return vo;
                }
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("校验短信相关信息异常", "[SmsHandle.checkSms() Exception]; data: " + JSON.toJSONString(vo) + LogInfo.getTrace(e));
        }
        return vo;
    }

    /****
     * 校验用户白名单
     *
     * @param vo
     * @return
     */
    private SendingVo checkUserWhiteMobile(SendingVo vo) {
        try {
            // 校验用户白名单
            boolean isUserWhiteMobile = UserWhiteMobileCache.getInstance().isUserWhiteMobile(vo.getUid(), vo.getMobile());

            if (isUserWhiteMobile) {
                // 号码在用户白名单中,不需要审核
                checkFlg = true; // 将标志置为返回
                return vo;
            } else {
                isUserWhiteMobile = StrategyGroupCache.getInstance().checkStrategyWhiteMobile(vo.getUid(), vo.getMobile());
                if (isUserWhiteMobile) {
                    // 号码在用户策略白名单中,不需要审核
                    checkFlg = true; // 将标志置为返回
                    return vo;
                }

                // 号码不在用户白名单中
                // whitetype=1全限白用户不能发送
                // if (whitetype == 1) {
                // checkFlg = true; // 将标志置为返回
                // vo.setStat(ConstantSys.SUBMIT_RESULT_SUCCESS);
                // vo.setRptStat(ConstantStatus.USER_STATUS_NOWHITE);
                // vo.setAutoFlag(ConstantSys.AUTO_FLAG_HISTORY);
                // return vo;
                // }
                return vo;
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("校验白名单异常", "[SmsHandle.checkUserWhiteMobile() Exception]; data: " + JSON.toJSONString(vo) + LogInfo.getTrace(e));
        }
        return vo;
    }

    /***
     * 判断是否存在签名
     *
     * @param vo
     * @return
     */
    private SendingVo existSign(SendingVo vo) {
        try {
            String contentTemp = vo.getContent();
            if (!(contentTemp.startsWith("【") && contentTemp.contains("】")) && !(contentTemp.endsWith("】") && contentTemp.contains("【"))) {
                UserSign userSign = UserSignCache.getInstance().getUserSign(vo.getUid(), vo.getUid() + "");
                if (userSign == null) {
                    log.info("签名不存在，id: " + vo.getId());
                    checkFlg = true;
                    vo.setStat(ConstantSys.SUBMIT_RESULT_SUCCESS);
                    vo.setRptStat(ConstantStatus.USER_STATUS_NOSIGN);
                    vo.setAutoFlag(ConstantSys.AUTO_FLAG_HISTORY);
                } else {
                    vo.setContent(vo.getContent() + userSign.getStore());
                    vo.setExpid(userSign.getExpend());
                }
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("校验是否存在签名异常", "[ChannelHandle.existSign() Exception]; data: " + JSON.toJSONString(vo) + LogInfo.getTrace(e));
        }
        return vo;
    }

    /****
     * 校验用户白签名
     *
     * @param signposition
     *            签名位置
     * @param vo
     * @return
     */
    private SendingVo checkUserWhiteSign(int signposition, SendingVo vo) {
        try {
            String content = vo.getContent();
            int uid = vo.getUid();
            String temp_sign = null;
            String temp_sign2 = null;

            vo = existSign(vo);
            if (checkFlg) {
                return vo;
            }
//			if (content.indexOf("】") == -1 || content.lastIndexOf("【") == -1) {
//				checkFlg = true; // 将标志置为返回
//				vo.setStat(ConstantSys.SUBMIT_RESULT_SUCCESS);
//				vo.setRptStat(ConstantStatus.USER_STATUS_NOSIGN);
//				vo.setAutoFlag(ConstantSys.AUTO_FLAG_HISTORY);
//				return vo;
//			}

//			if (signposition == 1) {
//				// 签名在头部
//				temp_sign = content.substring(0, content.indexOf("】") + 1);
//			} else if (signposition == 2) {
//				// 签名在尾部
//				temp_sign = content.substring(content.lastIndexOf("【"));
//			} else {
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
//			}

            // 验证用户白签名
            // 校验签名1
            boolean isUserWhiteSign = UserWhiteSignCache.getInstance().isUserWhiteSign(uid, temp_sign);
            if (isUserWhiteSign) {
                checkFlg = true; // 将标志置为返回
                return vo;
            }
            // 校验签名2
            if (temp_sign2 != null) {
                isUserWhiteSign = UserWhiteSignCache.getInstance().isUserWhiteSign(uid, temp_sign2);
                if (isUserWhiteSign) {
                    checkFlg = true; // 将标志置为返回
                    return vo;
                }
            }

            // 验证用户策略白签名
            isUserWhiteSign = StrategyGroupCache.getInstance().checkStrategyWhiteSign(uid, temp_sign);
            if (isUserWhiteSign) {
                checkFlg = true; // 将标志置为返回
                return vo;
            }
            if (temp_sign2 != null) {
                isUserWhiteSign = StrategyGroupCache.getInstance().checkStrategyWhiteSign(uid, temp_sign2);
                if (isUserWhiteSign) {
                    checkFlg = true; // 将标志置为返回
                    return vo;
                }
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("校验白/黑签名异常", "[SmsHandle.checkUserWhiteSign() Exception]; data: " + JSON.toJSONString(vo) + LogInfo.getTrace(e));
        }
        return vo;
    }

    /***
     * 校验用户黑名单
     *
     * @param vo
     * @return
     */
    private SendingVo checkUserBlackMobile(SendingVo vo) {
        try {
            boolean isUserBlackMobile = UserBlackMobileCache.getInstance().isUserBlackMobile(vo.getUid(), vo.getMobile());
            if (isUserBlackMobile) {
                checkFlg = true; // 将标志置为返回
                vo.setStat(ConstantSys.SUBMIT_RESULT_SUCCESS);
                vo.setRptStat(ConstantStatus.USER_STATUS_BLACKMOBILE);
                vo.setAutoFlag(ConstantSys.AUTO_FLAG_HISTORY);
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("校验用户黑名单异常", "[SmsHandle.checkUserBlackMobile() Exception]; data: " + JSON.toJSONString(vo) + LogInfo.getTrace(e));
        }
        return vo;
    }

    /***
     * 校验全量用户黑名单
     *
     * @param vo
     * @return
     */
    private SendingVo checkAllUserBlackMobile(SendingVo vo, int blackAll) {
        try {
            if (blackAll == 1) {
                boolean isUserBlackMobile = UserBlackMobileCache.getInstance().isAllUserBlackMobile(vo.getMobile());
                if (isUserBlackMobile) {
                    checkFlg = true; // 将标志置为返回
                    vo.setStat(ConstantSys.SUBMIT_RESULT_SUCCESS);
                    vo.setRptStat(ConstantStatus.USER_STATUS_BLACKMOBILE);
                    vo.setAutoFlag(ConstantSys.AUTO_FLAG_HISTORY);
                }
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("校验全量用户黑名单异常", "[SmsHandle.checkUserBlackMobile() Exception]; data: " + JSON.toJSONString(vo) + LogInfo.getTrace(e));
        }
        return vo;
    }

    /***
     * 校验用户屏蔽地区
     *
     * @param vo
     * @return
     */
    private SendingVo checkUserBlackLocation(SendingVo vo) {
        try {
            // 校验屏蔽地区
            if (vo.getLocation() != null && !vo.getLocation().equals("全国,-1")) {
                String provincecode = vo.getLocation().split(",")[1];
                // 用户屏蔽地区
                if (UserBlackLocationCache.getInstance().isBlackLocation(vo.getUid(), provincecode)) {
                    checkFlg = true; // 将标志置为返回
                    vo.setStat(ConstantSys.SUBMIT_RESULT_SUCCESS);
                    vo.setRptStat(ConstantStatus.SYS_STATUS_BLACKLOCATION);
                    vo.setAutoFlag(ConstantSys.AUTO_FLAG_HISTORY);
                    return vo;
                }
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("校验屏蔽地区异常", "[SmsHandle.checkUserBlackLocation() Exception]; data: " + JSON.toJSONString(vo) + LogInfo.getTrace(e));
        }
        return vo;
    }

    /***
     * 校验重号过滤
     *
     * @param repeatNum
     *            重复次数
     * @param repeatFilter
     *            过滤类型：1.10分钟 2.1小时 3.24小时
     * @param vo
     * @return
     */
    private SendingVo checkRepeatMobile(int repeatNum, int repeatFilter, SendingVo vo) {
        try {
            int uid = vo.getUid();
            long mobile = vo.getMobile();
            boolean filter = false;
            try {
                filter = RepeatMobileCache.getInstance().isRepeatMobile(uid, mobile, repeatFilter, repeatNum);
            } catch (Exception e) {
                log.info("checkRepeat Exception]uid:" + uid + ";mobile:" + mobile + ";Msg:" + e.getMessage());
            }
            // log.debug("filter=" + filter + ",repeatfilter=" + repeatFilter);

            if (filter) {
                checkFlg = true; // 将标志置为返回
                vo.setStat(ConstantSys.SUBMIT_RESULT_SUCCESS);
                vo.setRptStat(ConstantStatus.SYS_STATUS_REPEATMOBILE);
                vo.setAutoFlag(ConstantSys.AUTO_FLAG_HISTORY);
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("校验重号过滤异常", "[SmsHandle.checkRepeatMobile() Exception]; data: " + JSON.toJSONString(vo) + LogInfo.getTrace(e));
        }
        return vo;
    }

    /****
     * 校验用户模板
     *
     * @param vo
     * @return
     */
    private SendingVo checkUserMsgTemplet(SendingVo vo) {
        try {
            boolean isUserMsgTemplet = UserMsgTemplateCache.getInstance().isUserMsgTemplate(vo.getUid(), vo.getContent());
            if (isUserMsgTemplet) {
                log.debug("[HttpSmsHandle]msgTemplet,uid=" + vo.getUid() + ",expend=" + vo.getExpid() + ",mobile=" + vo.getMobile() + ",content=" + vo.getContent()
                        + ",isUserMsgTemplet=" + isUserMsgTemplet);
                checkFlg = true; // 将标志置为返回
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("校验用户短信模板异常", "[SmsHandle.checkUserMsgTemplet() Exception]; data: " + JSON.toJSONString(vo) + LogInfo.getTrace(e));
        }
        return vo;
    }

    /****
     * 校验用户自动屏蔽词
     *
     * @param vo
     * @return
     */
    private SendingVo checkUserBlackWordsAuto(SendingVo vo) {
        try {
            String words = UserBlackWordsCache.getInstance().isUserBlackWordsAuto(vo.getUid(), vo.getContent(), vo.getMobile());
            if (null != words) {
                checkFlg = true; // 将标志置为返回
                vo.setStat(ConstantSys.SUBMIT_RESULT_SUCCESS);
                vo.setRptStat(ConstantStatus.SYS_STATUS_AUTOBLACKWORD);
                vo.setMtStat(words);
                vo.setAutoFlag(ConstantSys.AUTO_FLAG_HISTORY);
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("校验用户屏蔽词异常", "[SmsHandle.checkUserBlackWords() Exception]; data: " + JSON.toJSONString(vo) + LogInfo.getTrace(e));
        }
        return vo;
    }

    /****
     * 校验用户审核屏蔽词
     *
     * @param vo
     * @return
     */
    private SendingVo checkUserBlackWords(SendingVo vo) {
        try {
            String words = UserBlackWordsCache.getInstance().isUserBlackWords(vo.getUid(), vo.getContent(), vo.getMobile());
            if (null != words) {
                checkFlg = true; // 将标志置为返回
                vo.setMtStat(words);
                vo.setAutoFlag(ConstantSys.AUTO_FLAG_RELEASE);
                vo.setRelease(1);
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("校验用户屏蔽词异常", "[SmsHandle.checkUserBlackWords() Exception]; data: " + JSON.toJSONString(vo) + LogInfo.getTrace(e));
        }
        return vo;
    }

    /**
     * 校验系统黑名单
     * @param vo
     * @return
     */
    private SendingVo checkUserStrategyGroupBlackMobile(SendingVo vo) {
        try {
            int uid = vo.getUid();
            // 获取用户策略组
            Map<Integer, String> userStrategyGroupMap =NightUtil.day()==true? StrategyGroupCache.getInstance().getUserStrategyGroup(uid):
                    StrategyGroupCache.getInstance().getSysStrategyGroup();
            if (userStrategyGroupMap == null || userStrategyGroupMap.isEmpty()) {
                return vo;
            }

            // 校验黑名单
            if (StrategyGroupCache.getInstance().checkStrategyBlackMobile(uid, vo.getMobile(), userStrategyGroupMap)) {
                checkFlg = true; // 将标志置为返回
                vo.setStat(ConstantSys.SUBMIT_RESULT_SUCCESS);
                vo.setRptStat(ConstantStatus.SYS_STATUS_BLACKMOBILE);
                vo.setAutoFlag(ConstantSys.AUTO_FLAG_HISTORY);
                log.info("[HttpSmsHandle]uid=" + uid + ",content=" + vo.getContent() + ",BlackMobile=" + vo.getMobile());
                return vo;
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("校验用户策略组异常", "[SmsHandle.checkUserStrategyGroup() Exception]; data: " + JSON.toJSONString(vo) + LogInfo.getTrace(e));
        }
        return vo;
    }

    /****
     * 校验校验系统自动屏蔽词和审核词
     *
     * @param vo
     * @return
     */
    private SendingVo checkUserStrategyGroup(SendingVo vo) {
        try {
            int uid = vo.getUid();
            // 获取用户策略组

            /**
             * 如果是白天,就获取用户策略组,否则获取系统策略组
             */
            Map<Integer, String> userStrategyGroupMap = StrategyGroupCache.getInstance().getUserStrategyGroup(uid);

            if (userStrategyGroupMap == null || userStrategyGroupMap.isEmpty()) {
                return vo;
            }

            // 校验黑名单
//			if (StrategyGroupCache.getInstance().ch
// eckStrategyBlackMobile(uid, vo.getMobile(), userStrategyGroupMap)) {
//				checkFlg = true; // 将标志置为返回
//				vo.setStat(ConstantSys.SUBMIT_RESULT_SUCCESS);
//				vo.setRptStat(ConstantStatus.SYS_STATUS_BLACKMOBILE);
//				vo.setAutoFlag(ConstantSys.AUTO_FLAG_HISTORY);
//				log.info("[HttpSmsHandle]uid=" + uid + ",content=" + vo.getContent() + ",BlackMobile=" + vo.getMobile());
//				return vo;
//			}

            // 校验自动屏蔽词
            String result = StrategyGroupCache.getInstance().checkStrategyAutoWords1(uid, vo.getContent(), userStrategyGroupMap, vo);
            if (result != null) {
                checkFlg = true; // 将标志置为返回
                vo.setStat(ConstantSys.SUBMIT_RESULT_SUCCESS);
                vo.setRptStat(ConstantStatus.SYS_STATUS_AUTOBLACKWORD);
                vo.setMtStat(result);
                vo.setAutoFlag(ConstantSys.AUTO_FLAG_HISTORY);
                return vo;
            }

            // 校验审核屏蔽词
            result = StrategyGroupCache.getInstance().checkStrategyReleaseWords1(uid, vo.getContent(), userStrategyGroupMap, vo);
            if (result != null) {
                checkFlg = true; // 将标志置为返回
                vo.setMtStat(result);
                vo.setRelease(1);
                vo.setAutoFlag(ConstantSys.AUTO_FLAG_RELEASE);
                return vo;
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("校验用户策略组异常", "[SmsHandle.checkUserStrategyGroup() Exception]; data: " + JSON.toJSONString(vo) + LogInfo.getTrace(e));
        }
        return vo;
    }

    /****
     * 校验是夜间审核机制
     *
     * @param vo
     * @return
     */
    private static boolean checkRelease(SendingVo vo, int release, int releaseNum) {
        // 先判断用户审核，用户审核不满足，在进入时间段审核
        boolean isRelease = false;
        try {
            int c = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

            // 每晚22点,早上8点清除审核缓存
            if (c == 22 || c == 8) {
                if (SmsCache.isClean == 0) {
                    for (Map.Entry<String, Integer> entry : SmsCache.contentCount.entrySet()) {
                        SmsCache.contentCount.remove(entry.getKey());
                    }
                    Logger.getLogger(SmsHandle.class).info("...clean SmsCache.contentCount,SmsCache.isClean=" + SmsCache.isClean);
                    SmsCache.isClean = 1;
                }
            } else {
                SmsCache.isClean = 0;
            }
            // 22点-8点，全部进行审核判断，之外时间，用户设置了审核在判断,isrelease=1是不审核，=0审核
            boolean isJudge = false;
            if (c >= 22 || c < 8) {
                isJudge = true;
            } else if (release == 0) {
                isJudge = true;
            }

            if (isJudge) {
                String md5 = Md5Util.getMD5(java.net.URLEncoder.encode(vo.getContent(), "utf-8"));
                int count = 0;
                if (SmsCache.contentCount != null && SmsCache.contentCount.containsKey(md5) && SmsCache.contentCount.get(md5) != null) {
                    count = SmsCache.contentCount.get(md5);
                    count++;
                    //SmsCache.contentCount.remove(md5);
                    SmsCache.contentCount.put(md5, count);
                } else {
                    SmsCache.contentCount.put(md5, 1);
                    count = 1;
                }
                // log.debug("md5:[" + md5 + "]content:" + vo.getContent() +
                // ";size:" + count);
                // 判断用户审核
                if (c >= 22 || c < 8) {// 每天的早上8点前或者晚上10后,如果相同内容的数量超过10条,最小数量进入审核
                    if (release == 1 || releaseNum > 10) {
                        releaseNum = 10;
                    }
                }
                if (count > releaseNum) {
                    isRelease = true;
                }
            }
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("校验夜间审核异常", "[SmsHandle.checkRelease() Exception]; data: " + JSON.toJSONString(vo) + LogInfo.getTrace(e));
        }
        return isRelease;
    }
}
