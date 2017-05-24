package com.sioo.service.model;

import java.util.Date;

public class SmsSendHistoryUnknown {
	private Long id;

	private Short stype;

	private Short mtype;

	private Long senddate;

	private Integer uid;

	private Long mobile;

	private Short channel;

	private String content;

	private Integer contentnum;

	private Integer oknum;

	private Integer errornum;

	private Short stat;

	private String mtstat;

	private Long pid;

	private Short grade;

	private String expid;

	private String rptstat;

	private Date rpttime;

	private String msgid;

	private String location;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Short getStype() {
		return stype;
	}

	public void setStype(Short stype) {
		this.stype = stype;
	}

	public Short getMtype() {
		return mtype;
	}

	public void setMtype(Short mtype) {
		this.mtype = mtype;
	}

	public Long getSenddate() {
		return senddate;
	}

	public void setSenddate(Long senddate) {
		this.senddate = senddate;
	}

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public Long getMobile() {
		return mobile;
	}

	public void setMobile(Long mobile) {
		this.mobile = mobile;
	}

	public Short getChannel() {
		return channel;
	}

	public void setChannel(Short channel) {
		this.channel = channel;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content == null ? null : content.trim();
	}

	public Integer getContentnum() {
		return contentnum;
	}

	public void setContentnum(Integer contentnum) {
		this.contentnum = contentnum;
	}

	public Integer getOknum() {
		return oknum;
	}

	public void setOknum(Integer oknum) {
		this.oknum = oknum;
	}

	public Integer getErrornum() {
		return errornum;
	}

	public void setErrornum(Integer errornum) {
		this.errornum = errornum;
	}

	public Short getStat() {
		return stat;
	}

	public void setStat(Short stat) {
		this.stat = stat;
	}

	public String getMtstat() {
		return mtstat;
	}

	public void setMtstat(String mtstat) {
		this.mtstat = mtstat == null ? null : mtstat.trim();
	}

	public Long getPid() {
		return pid;
	}

	public void setPid(Long pid) {
		this.pid = pid;
	}

	public Short getGrade() {
		return grade;
	}

	public void setGrade(Short grade) {
		this.grade = grade;
	}

	public String getExpid() {
		return expid;
	}

	public void setExpid(String expid) {
		this.expid = expid == null ? null : expid.trim();
	}

	public String getRptstat() {
		return rptstat;
	}

	public void setRptstat(String rptstat) {
		this.rptstat = rptstat == null ? null : rptstat.trim();
	}

	public Date getRpttime() {
		return rpttime;
	}

	public void setRpttime(Date rpttime) {
		this.rpttime = rpttime;
	}

	public String getMsgid() {
		return msgid;
	}

	public void setMsgid(String msgid) {
		this.msgid = msgid == null ? null : msgid.trim();
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location == null ? null : location.trim();
	}
}