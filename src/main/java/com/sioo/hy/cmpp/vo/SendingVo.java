package com.sioo.hy.cmpp.vo;

import java.io.Serializable;

public class SendingVo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long id;
	private int mtype; // 运营商
	private long senddate; // 提交时间
	private int uid; // 用户id
	private Long mobile; // 接收号码
	private int channel; // 发送通道
	private String content; // 发送内容
	private int contentNum; // 内条数
	private int stat; // 状态
	private String mtStat; // 屏蔽词
	private int pid; // 发送包ID
	private int grade;
	private String expid; // 发送扩展号
	private String rptStat; // 发送状态
	private Integer release; // 是否通过审核
	private String msgid; // 网关返回消息ID
	private String location; // 号码归属地
	private int autoFlag; // 处理标记,-1自动处理
	private int handStat; // 审核表使用字段，默认值-1，审核通过1，审核驳回0，审核清除2
	private int hisids;
	private String source; // 来源
	private int succ;
	private int fail;
	private int arrive_fail;
	private String mdstr;

	/**
	 * 省id
	 */
	private int provinceCode;

	/**
	 * 市id
	 */
	private int cityCode;
	/**
	 * 屏蔽词类型
	 */
	private Integer screenType;

	public int getProvinceCode() {
		return provinceCode;
	}

	public void setProvinceCode(int provinceCode) {
		this.provinceCode = provinceCode;
	}

	public Integer getCityCode() {
		return cityCode;
	}

	public void setCityCode(int cityCode) {
		this.cityCode = cityCode;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getMtype() {
		return mtype;
	}

	public void setMtype(int mtype) {
		this.mtype = mtype;
	}

	public long getSenddate() {
		return senddate;
	}

	public void setSenddate(long senddate) {
		this.senddate = senddate;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public Long getMobile() {
		return mobile;
	}

	public void setMobile(Long mobile) {
		this.mobile = mobile;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getContentNum() {
		return contentNum;
	}

	public void setContentNum(int contentNum) {
		this.contentNum = contentNum;
	}

	public int getStat() {
		return stat;
	}

	public void setStat(int stat) {
		this.stat = stat;
	}

	public String getMtStat() {
		return mtStat;
	}

	public void setMtStat(String mtStat) {
		this.mtStat = mtStat;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getGrade() {
		return grade;
	}

	public void setGrade(int grade) {
		this.grade = grade;
	}

	public String getExpid() {
		return expid;
	}

	public void setExpid(String expid) {
		this.expid = expid;
	}

	public String getRptStat() {
		return rptStat;
	}

	public void setRptStat(String rptStat) {
		this.rptStat = rptStat;
	}

	public Integer getRelease() {
		return release;
	}

	public void setRelease(Integer release) {
		this.release = release;
	}

	public String getMsgid() {
		return msgid;
	}

	public void setMsgid(String msgid) {
		this.msgid = msgid;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public int getAutoFlag() {
		return autoFlag;
	}

	public void setAutoFlag(int autoFlag) {
		this.autoFlag = autoFlag;
	}

	public int getHandStat() {
		return handStat;
	}

	public void setHandStat(int handStat) {
		this.handStat = handStat;
	}

	public int getHisids() {
		return hisids;
	}

	public void setHisids(int hisids) {
		this.hisids = hisids;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public int getFail() {
		return fail;
	}

	public void setFail(int fail) {
		this.fail = fail;
	}

	public int getSucc() {
		return succ;
	}

	public void setSucc(int succ) {
		this.succ = succ;
	}

	public int getArrive_fail() {
		return arrive_fail;
	}

	public void setArrive_fail(int arrive_fail) {
		this.arrive_fail = arrive_fail;
	}

	public String getMdstr() {
		return mdstr;
	}

	public void setMdstr(String mdstr) {
		this.mdstr = mdstr;
	}

	public Integer getScreenType() {
		return screenType;
	}

	public void setScreenType(Integer screenType) {
		this.screenType = screenType;
	}
}

