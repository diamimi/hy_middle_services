package com.sioo.sms.handle.channel;

import java.util.Map;
import org.apache.log4j.Logger;
import com.alibaba.fastjson.JSON;
import com.sioo.cache.ChannelBlackSignCache;
import com.sioo.cache.ChannelCache;
import com.sioo.cache.UserBlackLocationCache;
import com.sioo.hy.cmpp.vo.SendingVo;
import com.sioo.log.LogInfo;
import com.sioo.util.ConstantStatus;
import com.sioo.util.ConstantSys;
import com.sioo.util.RabbitMQProducerUtil;

/***
 * 通道处理
 *
 * @author OYJM
 * @date 2016年10月11日
 *
 */
public class ChannelHandle {

	private ChannelHandle() {
	}

	private static Logger log = Logger.getLogger(ChannelHandle.class);
	private static ChannelHandle channelHandle;

	public static ChannelHandle getInstance() {
		if (channelHandle != null) {
			return channelHandle;
		}
		synchronized (ChannelHandle.class) {
			if (channelHandle == null) {
				channelHandle = new ChannelHandle();
			}
		}
		return channelHandle;
	}

	// 校验标志.如果为true,直接返回,不对再做下面的判断
	private boolean checkFlg = false;

	public SendingVo channelHandle(SendingVo vo, Map<String, Object> user_map) {
		try {
			checkFlg = false;

			// 校验通道黑签名
			// vo = checkChannelBlackSign(vo, signArray);
			// if (checkFlg) {
			// return vo;
			// }

			// 通道屏蔽地区
			vo = checkBlackChannelArea(vo);
			if (checkFlg) {
				return vo;
			}

			// 检验通道状态
			vo = checkChannel(vo);
			if (checkFlg) {
				return vo;
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("校验通道相关信息异常","[ChannelHandle.channelHandle() Exception]; data: " + JSON.toJSONString(vo) + LogInfo.getTrace(e));
		}
		return vo;
	}

	/***
	 * 校验通道黑签名
	 *
	 * @param vo
	 * @param signArray
	 * @return
	 */
	private SendingVo checkChannelBlackSign(SendingVo vo, String[] signArray) {
		try {
			// 判断黑签名只针对移动通道
			if (vo.getMtype() == 1) {
				boolean isChannelBlackSign = false;
				if (null != signArray[0] && !signArray[0].isEmpty()) {
					isChannelBlackSign = ChannelBlackSignCache.getInstance()
							.isChannelBlackSign(vo.getUid(), signArray[0]);
					if (isChannelBlackSign) {
						log.info("通道黑签名，id: " + vo.getId() + " uid: " + vo.getUid() + " sign: " + signArray[0]);
						checkFlg = true;
						vo.setStat(ConstantSys.SUBMIT_RESULT_SUCCESS);
						vo.setRptStat(ConstantStatus.SYS_STATUS_BLACKSIGN);
						vo.setAutoFlag(ConstantSys.AUTO_FLAG_HISTORY);
						return vo;
					}

					if (signArray.length > 1 && null != signArray[1]
							&& !signArray[1].isEmpty()) {
						isChannelBlackSign = ChannelBlackSignCache
								.getInstance().isChannelBlackSign(vo.getUid(),
										signArray[1]);
						if (isChannelBlackSign) {
							log.info("通道黑签名，id: " + vo.getId() + " uid: " + vo.getUid() + " sign: " + signArray[1]);
							checkFlg = true;
							vo.setStat(ConstantSys.SUBMIT_RESULT_SUCCESS);
							vo.setRptStat(ConstantStatus.SYS_STATUS_BLACKSIGN);
							vo.setAutoFlag(ConstantSys.AUTO_FLAG_HISTORY);
							return vo;
						}
					}
				}
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert(
					"校验通道黑签名异常",
					"[ChannelHandle.checkChannelBlackSign() Exception]; data: "
							+ JSON.toJSONString(vo) + LogInfo.getTrace(e));
		}
		return vo;
	}

	/***
	 * 校验通道屏蔽地区
	 *
	 * @param vo
	 * @return
	 */
	public SendingVo checkBlackChannelArea(SendingVo vo) {
		if (vo.getLocation() != null && !vo.getLocation().equals("全国,-1")) {
			String provincecode = vo.getLocation().split(",")[1];
			if (UserBlackLocationCache.getInstance().isChannelBlackLocation(
					vo.getChannel(), provincecode)) {
				checkFlg = true; // 将标志置为返回
				vo.setStat(ConstantSys.SUBMIT_RESULT_SUCCESS);
				vo.setRptStat(ConstantStatus.SYS_STATUS_BLACKLOCATION);
				vo.setAutoFlag(ConstantSys.AUTO_FLAG_HISTORY);
				return vo;
			}
		}
		return vo;
	}

	/***
	 * 检验通道状态，以及分组通道
	 *
	 * @param vo
	 * @return
	 * @throws InterruptedException
	 */
	private SendingVo checkChannel(SendingVo vo) {
		try {
			int channel = vo.getChannel();
			Map<String, Object> lastChannelInfo = ChannelCache.getInstance().getChannel(channel);
			// 判断通道状态0为正常,1为暂停,2为停止
			int status = 1;
			if (lastChannelInfo != null && lastChannelInfo.get("status") != null) {
				status = Integer.parseInt(String.valueOf(lastChannelInfo.get("status").toString()));
			}

			if (status > 0) {
				log.info("通道已停止：channelId: " + channel);
				checkFlg = true;
				if(vo.getAutoFlag()!=ConstantSys.AUTO_FLAG_RELEASE){
					vo.setAutoFlag(ConstantSys.AUTO_FLAG_NONE);
				}
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("校验通道状态异常","[ChannelHandle.checkChannel() Exception]; data: " + JSON.toJSONString(vo) + LogInfo.getTrace(e));
		}
		return vo;
	}
}
