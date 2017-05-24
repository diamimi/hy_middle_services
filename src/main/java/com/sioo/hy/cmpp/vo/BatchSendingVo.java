package com.sioo.hy.cmpp.vo;

import java.io.Serializable;

/**
 * 批次显示记录,主要用于显示http、web批次提交的记录信息
 * @author Administrator  331737188@qq.com
 * @date : 2016年11月3日 下午2:30:54
 *
 */
public class BatchSendingVo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 批次号
	 */
	private int pid;
	
	/**
	 *  发送时间
	 */
	private Long senddate;
	
	/**
	 * 号码数量
	 */
	private Integer num;
	
	/**
	 * 发送内容
	 */
	private String content;
	
	/**
	 * 用户编号
	 */
	private Integer uid;

	/**  
	 * 批次号  
	 * @return pid 批次号  
	 */
	public int getPid() {
		return pid;
	}

	/**  
	 * 批次号  
	 * @return pid 批次号  
	 */
	public void setPid(int pid) {
		this.pid = pid;
	}

	/**  
	 * 发送时间  
	 * @return senddate 发送时间  
	 */
	public Long getSenddate() {
		return senddate;
	}

	/**  
	 * 发送时间  
	 * @return senddate 发送时间  
	 */
	public void setSenddate(Long senddate) {
		this.senddate = senddate;
	}

	/**  
	 * 号码数量  
	 * @return num 号码数量  
	 */
	public Integer getNum() {
		return num;
	}

	/**  
	 * 号码数量  
	 * @return num 号码数量  
	 */
	public void setNum(Integer num) {
		this.num = num;
	}

	/**  
	 * 发送内容  
	 * @return content 发送内容  
	 */
	public String getContent() {
		return content;
	}

	/**  
	 * 发送内容  
	 * @return content 发送内容  
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**  
	 * 用户编号  
	 * @return uid 用户编号  
	 */
	public Integer getUid() {
		return uid;
	}

	/**  
	 * 用户编号  
	 * @return uid 用户编号  
	 */
	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public BatchSendingVo(int pid, Long senddate, Integer num, String content,
			Integer uid) {
		super();
		this.pid = pid;
		this.senddate = senddate;
		this.num = num;
		this.content = content;
		this.uid = uid;
	}

	public BatchSendingVo() {
		super();
		// TODO Auto-generated constructor stub
	}

}
