package com.sioo.client.cmpp.vo;

import java.io.Serializable;

public class DeliverVo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int id;
	
	/**
	 * 网关消息编号
	 */
	private Long msgId;
	
	/**
	 * 号码
	 */
	private String mobile;
	
	private String rpt_code;
	
	private String rpt_time;
	
	private int channel;
	
	/**
	 * 历史记录编号
	 */
	private long hisId;
	
	/**
	 * 日期(格式yyMMdd)
	 */
	private int channelDays;
	
	/**
	 * 日期(格式yyMMdd)
	 */
	private int userDays;

	/**
	 * 用户编号
	 */
	private int uid;
	
	/**
	 * 批次编号
	 */
	private long pid;

	/**
	 * id
	 * @return id id
	 */
	public int getId() {
		return id;
	}

	/**
	 * id
	 * @param id id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * channel
	 * @return channel channel
	 */
	public int getChannel() {
		return channel;
	}

	/**
	 * channel
	 * @param channel channel
	 */
	public void setChannel(int channel) {
		this.channel = channel;
	}

	/**
	 * 网关消息编号
	 * @return msgId 网关消息编号
	 */
	public Long getMsgId() {
		return msgId;
	}

	/**
	 * 网关消息编号
	 * @param msgId 网关消息编号
	 */
	public void setMsgId(Long msgId) {
		this.msgId = msgId;
	}

	/**
	 * 号码
	 * @return mobile 号码
	 */
	public String getMobile() {
		return mobile;
	}

	/**
	 * 号码
	 * @param mobile 号码
	 */
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	/**
	 * rpt_code
	 * @return rpt_code rpt_code
	 */
	public String getRpt_code() {
		return rpt_code;
	}

	/**
	 * rpt_code
	 * @param rpt_code rpt_code
	 */
	public void setRpt_code(String rpt_code) {
		this.rpt_code = rpt_code;
	}

	/**
	 * rpt_time
	 * @return rpt_time rpt_time
	 */
	public String getRpt_time() {
		return rpt_time;
	}

	/**  
	 * 历史记录编号  
	 * @return hisId 历史记录编号  
	 */
	public long getHisId() {
		return hisId;
	}

	/**  
	 * 历史记录编号  
	 * @return hisId 历史记录编号  
	 */
	public void setHisId(long hisId) {
		this.hisId = hisId;
	}

	/**  
	 * 用户编号  
	 * @return uid 用户编号  
	 */
	public int getUid() {
		return uid;
	}

	/**  
	 * 用户编号  
	 * @return uid 用户编号  
	 */
	public void setUid(int uid) {
		this.uid = uid;
	}

	/**  
	 * 批次编号  
	 * @return pid 批次编号  
	 */
	public long getPid() {
		return pid;
	}

	/**  
	 * 批次编号  
	 * @return pid 批次编号  
	 */
	public void setPid(long pid) {
		this.pid = pid;
	}

	/**  
	 * 日期(格式yyMMdd)  
	 * @return channelDays 日期(格式yyMMdd)  
	 */
	public int getChannelDays() {
		return channelDays;
	}

	/**  
	 * 日期(格式yyMMdd)  
	 * @return channelDays 日期(格式yyMMdd)  
	 */
	public void setChannelDays(int channelDays) {
		this.channelDays = channelDays;
	}

	/**  
	 * 日期(格式yyMMdd)  
	 * @return userDays 日期(格式yyMMdd)  
	 */
	public int getUserDays() {
		return userDays;
	}

	/**  
	 * 日期(格式yyMMdd)  
	 * @return userDays 日期(格式yyMMdd)  
	 */
	public void setUserDays(int userDays) {
		this.userDays = userDays;
	}

	/**
	 * rpt_time
	 * @param rpt_time rpt_time
	 */
	public void setRpt_time(String rpt_time) {
		this.rpt_time = rpt_time;
	}

	public DeliverVo(int channel, Long msgId, String mobile, String rpt_code, String rpt_time) {
		super();
		this.channel = channel;
		this.msgId = msgId;
		this.mobile = mobile;
		this.rpt_code = rpt_code;
		this.rpt_time = rpt_time;
	}

	public DeliverVo() {
		super();
		// TODO Auto-generated constructor stub
	}

}
