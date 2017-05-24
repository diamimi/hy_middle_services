package com.sioo.hy.cmpp.vo;

import java.io.Serializable;

public class ResultVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int uid; // 用户id
	private long senddate; // 时间
	private int submitTotal; // 提交总数
	private int submitSuccess;// 成功数
	private int submitFail; // 失败数

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public long getSenddate() {
		return senddate;
	}

	public void setSenddate(long senddate) {
		this.senddate = senddate;
	}

	public int getSubmitTotal() {
		return submitTotal;
	}

	public void setSubmitTotal(int submitTotal) {
		this.submitTotal = submitTotal;
	}

	public int getSubmitSuccess() {
		return submitSuccess;
	}

	public void setSubmitSuccess(int submitSuccess) {
		this.submitSuccess = submitSuccess;
	}

	public int getSubmitFail() {
		return submitFail;
	}

	public void setSubmitFail(int submitFail) {
		this.submitFail = submitFail;
	}

}
