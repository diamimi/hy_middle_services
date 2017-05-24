package com.sioo.util;

public class ConstantStatus {
	/**
	 * 消息流程： 1,接口提交缓存计费,放入接口缓存,缓存key定义key+uid的第一位数字
	 * 
	 * 2,以UID的第一位数字1-9创建9个线程，从接口缓存中取出 前端校验， 结果：
	 * ①正常结果，放入队列queue，由queue批量入队列sending表， ②异常结果直接入历史记录queue
	 * 
	 * 3，从sending表取出校验放入通道缓存，结果： ①正常结果放入通道队列 ②异常结果直接入历史queue
	 * 
	 * 4，发送程序从通道队列中读取发送,结果： ①发送记录放入历史记录queue，由queue读取入历史记录库（包含正常数据和校验失败数据）
	 * ②报告放入临时报告缓存
	 * 
	 * 注意：发送程序如果挂掉，短信会不停往通道队列中放，导致记录丢失，这里要有预警或停止措施， 或补发措施
	 * 
	 * 5，发送程序收到报告后到临时报告缓存找到对应记录，把报告拼接完整 ①放入已更新报告队列中
	 * ②放入用户状态报告中待取，格式hash，有效期1天,如果客户1天没拿走报告，报告就会过期
	 * 
	 * 6，由已更新报告队列取出报告更新到系统。
	 * */

	/**
	 * SIOO自定义错误代码
	 * 
	 * 0001-1999 系统错误码 
	 * XA:0000 超过5分钟没有响应结果, 
	 * XA:0001 驳回失败 
	 * XA:0002 触发黑签名
	 * XA:0100重号过滤 
	 * XA:0003 签名未报备
	 * XA:0004 触发自动屏蔽词 
	 * XA:0139 触发系统黑名单 
	 * XA:0140 触发屏蔽地区
	 * 
	 * 2000-3999 用户错误码 
	 * XA:2001 非用户白名单 
	 * XA:2002 非用户短信模板 
	 * XA:2003 内容没签名 
	 * XA:2004内容签名位置不正确 
	 * XA:2006用户余额不足 
	 * XA:2139 触发用户黑名单
	 * */

	public static String SYS_STATUS_NORESPONSE = "XA:0000";
	public static String SYS_STATUS_REJECT = "XA:0001";
	public static String SYS_STATUS_BLACKSIGN = "XA:0002";
	public static String SYS_STATUS_NOREPORTSIGN = "XA:0003";
	public static String SYS_STATUS_AUTOBLACKWORD = "XA:0004";
	public static String SYS_STATUS_REPEATMOBILE = "XA:0100";
	public static String SYS_STATUS_BLACKMOBILE = "XA:0139";
	public static String SYS_STATUS_BLACKLOCATION = "XA:0140";

	public static String USER_STATUS_NOWHITE = "XA:2001";
	public static String USER_STATUS_NOMODEL = "XA:2002";
	public static String USER_STATUS_NOSIGN = "XA:2003";
	public static String USER_STATUS_SIGNPOSITION = "XA:2004";
	public static String USER_STATUS_NOBLANCE = "XA:2006";
	public static String USER_STATUS_BLACKMOBILE = "XA:2139";
}
