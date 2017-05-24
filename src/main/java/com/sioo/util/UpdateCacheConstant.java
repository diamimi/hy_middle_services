package com.sioo.util;

public class UpdateCacheConstant {
	public class METHOD {
		// 增、删、改、重新加载
		public static final int GET = 6;
		public static final int ADD = 1;
		public static final int DELETE = 2;
		public static final int UPDATE = 3;
		public static final int RELOAD = 4;
		public static final int STAT = 5;
	}

	public class TYPE {
		public static final int CHANNEL = 1;
		public static final int CHANNEL_GROUP = 2;
		public static final int CHANNEL_BLACK_SIGN = 4;

		public static final int USER = 11;
		public static final int USER_ROUTE = 12;
		public static final int USER_SIGN = 13;
		public static final int USER_SMS_ALERT = 14;
		public static final int USER_SMS = 15;
		public static final int USER_ALERT = 16;
		
		public static final int USER_WHITE_MOBILE = 21;
		public static final int USER_WHITE_SIGN = 22;
		public static final int USER_BLACK_MOBILE = 23;
		public static final int USER_BLACK_WORDS = 24;
		public static final int USER_MSG_TEMPLATE = 25;
		public static final int USER_BLACK_AREA = 26;
		public static final int USER_STRATEGY_GROUP = 27;
		public static final int STRATEGY_GROUP = 28;
		public static final int MOBILE_AREA = 29;
		public static final int RELEASE_TEMPLATE = 30;
		public static final int REPEAT_MOBILE = 31;
		public static final int SYS_STRATEGY_GOURP=33;

		/**
		 * 切换用户service线路
		 */
		public static final int CHANGE_USER_LINE=32;
		
		public static final int CONTROL = 99;

	}

}
