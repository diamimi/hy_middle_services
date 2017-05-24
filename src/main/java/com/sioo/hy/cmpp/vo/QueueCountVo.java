package com.sioo.hy.cmpp.vo;

import java.io.Serializable;

public class QueueCountVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int uid; // 用户ID
	private int submit; // 提交条数
	private int success;// 提交成功条数
	private int fail; // 提交失败条数
	private int status; // 状态条数
	private int date; // 日期 ，YYYYMMDD格式

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getSubmit() {
		return submit;
	}

	public void setSubmit(int submit) {
		this.submit = submit;
	}

	public int getSuccess() {
		return success;
	}

	public void setSuccess(int success) {
		this.success = success;
	}

	public int getFail() {
		return fail;
	}

	public void setFail(int fail) {
		this.fail = fail;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getDate() {
		return date;
	}

	public void setDate(int date) {
		this.date = date;
	}

}
