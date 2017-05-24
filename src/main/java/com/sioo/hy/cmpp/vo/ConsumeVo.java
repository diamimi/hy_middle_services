package com.sioo.hy.cmpp.vo;

import java.io.Serializable;

public class ConsumeVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int uid;
	private int kousms;
	private int unkousms;
	private Long date;

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getKousms() {
		return kousms;
	}

	public void setKousms(int kousms) {
		this.kousms = kousms;
	}

	public int getUnkousms() {
		return unkousms;
	}

	public void setUnkousms(int unkousms) {
		this.unkousms = unkousms;
	}

	public Long getDate() {
		return date;
	}

	public void setDate(Long date) {
		this.date = date;
	}

}