package com.sioo.hy.cmpp.vo;

import java.io.Serializable;

public class ChannelVo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer id;

	private Integer localReadNum; // 网关提供的流速

	private Integer localRate; // 通道实际流速

	private Integer sendWordsMaxlen;// 通道支持最大字数

	private Integer recordType; // 报备方式(0为无,1为先报备后发,2为先发后报备)

	private Integer routeType; // 路由类型(0为无路由,1为关键词路由,2为验证码路由,3为通知路由,4为营销路由)

	private String routeRequire;// 路由条件

	private Integer routeChannel;// 路由通道

	private Integer signPosition;// 签名位置

	private Integer isGroup; // 是否为通道组

	private Integer status;// 通道状态0为正常,1为暂停,2为停止

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getLocalReadNum() {
		return localReadNum;
	}

	public void setLocalReadNum(Integer localReadNum) {
		this.localReadNum = localReadNum;
	}

	public Integer getLocalRate() {
		return localRate;
	}

	public void setLocalRate(Integer localRate) {
		this.localRate = localRate;
	}

	public Integer getSendWordsMaxlen() {
		return sendWordsMaxlen;
	}

	public void setSendWordsMaxlen(Integer sendWordsMaxlen) {
		this.sendWordsMaxlen = sendWordsMaxlen;
	}

	public Integer getRecordType() {
		return recordType;
	}

	public void setRecordType(Integer recordType) {
		this.recordType = recordType;
	}

	public Integer getRouteType() {
		return routeType;
	}

	public void setRouteType(Integer routeType) {
		this.routeType = routeType;
	}

	public String getRouteRequire() {
		return routeRequire;
	}

	public void setRouteRequire(String routeRequire) {
		this.routeRequire = routeRequire;
	}

	public Integer getRouteChannel() {
		return routeChannel;
	}

	public void setRouteChannel(Integer routeChannel) {
		this.routeChannel = routeChannel;
	}

	public Integer getSignPosition() {
		return signPosition;
	}

	public void setSignPosition(Integer signPosition) {
		this.signPosition = signPosition;
	}

	public Integer getIsGroup() {
		return isGroup;
	}

	public void setIsGroup(Integer isGroup) {
		this.isGroup = isGroup;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
}
