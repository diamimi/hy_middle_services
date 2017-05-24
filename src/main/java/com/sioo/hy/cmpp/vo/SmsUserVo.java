package com.sioo.hy.cmpp.vo;

import java.io.Serializable;

public class SmsUserVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID =1L;

	private int uid;
	private int sms;

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getSms() {
		return sms;
	}

	public void setSms(int sms) {
		this.sms = sms;
	}

}
