package com.sioo.util;

public class ConstantSys {

	/** 处理命令,自动处理标记 */
	public static final int AUTO_FLAG_HISTORY = 1;
	public static final int AUTO_FLAG_RELEASE = 2;
	public static final int AUTO_FLAG_NONE = 3;
	public static final int AUTO_FLAG_SMS = 4;

	/*********************************************************************/

	/** 号码存储截取位置 */
	public static final int SYS_BLACK_MOBILE_SUB_POSITION = 5;
	public static final int MOBILE_USER_SUB_POSITION = 4;
	public static final int MOBILE_USER_WHITE_POSITION = 5;
	public static final int MAX_SIGN_NUM = 20;
	public static final int MOBILE_LOCATION_POSITION = 7; // 号码归属地，前七位
	public static final int MOBILE_LOCATION_SUB_POSITION = 3; // 号码归属地，前七位
	public static final int MOBILE_POSITION = 5;

	/** 用户策略组类型：1.自动屏蔽词 2审核屏蔽词 3系统黑名单 4屏蔽地区 5系统白名单 6系统白签名 **/
	public static final int USER_STRATEGY_GROUP_SYS_BLACK_WORDS_AUTO = 1;
	public static final int USER_STRATEGY_GROUP_SYS_BLACK_WORDS_CHECK = 2;
	public static final int USER_STRATEGY_GROUP_SYS_BLACK_MOBILE = 3;
	// public static final int USER_STRATEGY_GROUP_SYS_BLACK_LOCATION = 4;
	public static final int USER_STRATEGY_GROUP_SYS_WHITE_MOBILE = 5;
	public static final int USER_STRATEGY_GROUP_SYS_WHITE_SIGN = 6;
	public static final int USER_STRATEGY_GROUP_SYS_BLACK_SIGN = 7;
	/*********************************************************************/
	public static final int SUBMIT_RESULT_SUCCESS = 1;

	/** 过滤条件 0:不过滤; 1:10分钟; 2:1小时; 3:24小时 **/
	public static final Integer REPEATFILTER_NONE = 0;
	public static final Integer REPEATFILTER_TEN_MINUTE = 1;
	public static final Integer REPEATFILTER_ONE_HOUR = 2;
	public static final Integer REPEATFILTER_ONE_DAY = 3;
	public static final Integer REPEATFILTER_ONE_WEEK=4;

	/** 签名类型：1平台；2通道 **/
	public static final Integer SIGN_TYPE_USER = 1;
	public static final Integer SIGN_TYPE_CHANNEL = 2;

	public static final int SYSTEM_GROUP_AUTO=6;
	public static final int SYSTEM_GROUP_CHECK=5;
	public static final int SYSTEM_GROUP_BLACK_SIGN=7;
	public static final int SYSTEM_GROUP_WHITE_SIGN=3;
	public static final int SYSTEM_GROUP_WHITE_MOBILE=1;
	public static final int SYSTEM_GROUP_BLACK_MOBILE=2;


	public static final int USER_WORDS_CHECK=1;
	public static final int USER_WORDS_AUTO=2;

	public static final int USER_WORDS_CHECK_NIGHT=44;
}
