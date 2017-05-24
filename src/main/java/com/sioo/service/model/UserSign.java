package com.sioo.service.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class UserSign implements Serializable{


	private Integer id;

	private Integer uid;

	private String store;

	private String expend;

	private String expend2;

	private String expendqd;

	private String userexpend;

	private Date addtime;

	private Integer status;

	private Integer channel;

	private String remark;

	private Date signtime;

	private Integer userstat;

	private Integer type;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public String getStore() {
		return store;
	}

	public void setStore(String store) {
		this.store = store == null ? null : store.trim();
	}

	public String getExpend() {
		return expend;
	}

	public void setExpend(String expend) {
		this.expend = expend == null ? null : expend.trim();
	}

	public String getExpend2() {
		return expend2;
	}

	public void setExpend2(String expend2) {
		this.expend2 = expend2 == null ? null : expend2.trim();
	}

	public String getExpendqd() {
		return expendqd;
	}

	public void setExpendqd(String expendqd) {
		this.expendqd = expendqd == null ? null : expendqd.trim();
	}

	public String getUserexpend() {
		return userexpend;
	}

	public void setUserexpend(String userexpend) {
		this.userexpend = userexpend == null ? null : userexpend.trim();
	}

	public Date getAddtime() {
		return addtime;
	}

	public void setAddtime(Date addtime) {
		this.addtime = addtime;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getChannel() {
		return channel;
	}

	public void setChannel(Integer channel) {
		this.channel = channel;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark == null ? null : remark.trim();
	}

	public Date getSigntime() {
		return signtime;
	}

	public void setSigntime(Date signtime) {
		this.signtime = signtime;
	}

	public Integer getUserstat() {
		return userstat;
	}

	public void setUserstat(Integer userstat) {
		this.userstat = userstat;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}
	
	public static void Sort(List<UserSign> list) {  
        Collections.sort(list, new Comparator<UserSign>() {  
            public int compare(UserSign a, UserSign b) {  
                int ret = b.getAddtime().compareTo(a.getAddtime()) ;
                return ret;  
            }  
        });  
    }
}