package com.sioo.util;

import com.sioo.hy.cmpp.vo.*;
import com.sioo.service.model.UserSign;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author CQL 331737188@qq.com
 * @date : 2016��4��28�� ����1:23:02
 *
 */
public class SmsCache {
	public static Integer SERVER_TYPE = null;
	public static boolean CONTROL = true;
	// 警告消息发送开关
	public static boolean alertMsgFlag = false;
	// ҹ�䱨�������ֻ������б�
	public static List<String> alertMsgDayMobiles = null;
	// �ռ䱨�������ֻ������б�
	public static List<String> alertMsgNightMobiles = null;

	public static String mongoHost = null;
	public static Integer mongoPort = null;
	public static String mongoDbName = null;

	public static int RABBIT_PORT = 0;
	public static String RABBIT_HOST = null;
	public static String RABBIT_USERNAME = null;
	public static String RABBIT_PASSWORD = null;
	public static int RABBIT_PREFETCH_SIZE = 0;



	public static int RABBIT_PORT1= 0;
	public static String RABBIT_HOST1 = null;
	public static String RABBIT_USERNAME1 = null;
	public static String RABBIT_PASSWORD1 = null;
	public static int RABBIT_PREFETCH_SIZE1 = 0;



	public static int RABBIT_PORT0 = 0;
	public static String RABBIT_HOST0 = null;
	public static String RABBIT_USERNAME0 = null;
	public static String RABBIT_PASSWORD0 = null;
	public static int RABBIT_PREFETCH_SIZE0 = 0;

	public static Map<String, Integer> contentCount = new ConcurrentHashMap<String, Integer>();// 每天晚上10至次日早上8点用于内容批量内容统计过滤
	public static int isClean = 0;;// 是否清除.每天的早上8点前或者晚上10后,如果相同内容的数量超过10条,则记录.
	// 次日清除

	public static boolean cleaningRepeatMobile = false;

	// 本地队列处理标记 ，如果为true,肯定还没处理完
	public static Map<String, Boolean> QUEUE_FIRST_FLG = new ConcurrentHashMap<String, Boolean>();
	// public static boolean QUEUE_FIRST_FLG = true;
	public static boolean QUEUE_SENDING_HISTORY_FLG = false;
	public static boolean QUEUE_SENDING_FLG = false;
	public static boolean QUEUE_SENDING_RELEASE_FLG = false;
	public static boolean QUEUE_REPORT_FLG = false;
	public static boolean QUEUE_RESULT_FLG = false;
	public static boolean QUEUE_CHECK_RELEASE_FLG = false;
	public static boolean QUEUE_CHECK_SENDING_FLG = false;
	public static boolean QUEUE_CHECK_ALERT_FLG = false;
	public static boolean QUEUE_SMS_CACHE_FLG = false;
	public static boolean QUEUE_SMS_BATCH_FLG = false;
	public static boolean QUEUE_SMS_CHANNEL_FLG = false;
	public static boolean QUEUE_UPDATE_SENDING_FLG = false;
	public static boolean QUEUE_RATIO_FLG = false;

	public static BlockingQueue<BatchSendingVo> QUEUE_BATCHSMS = new LinkedBlockingQueue<BatchSendingVo>();// 批次消息队列

	public static BlockingQueue<SendingVo> QUEUE_SMSCHECK_NORMAL = new LinkedBlockingQueue<SendingVo>();// 普通队列
	public static BlockingQueue<SendingVo> QUEUE_SENDING_PRIORITIZED = new LinkedBlockingQueue<SendingVo>();// 优先队列

	public static BlockingQueue<SendingVo> QUEUE_SENDING_HISTORY = new LinkedBlockingQueue<SendingVo>();// 历史记录队列
	public static BlockingQueue<SendingVo> QUEUE_SENDING = new LinkedBlockingQueue<SendingVo>();// 发送通道队列
	public static BlockingQueue<SendingVo> QUEUE_SENDING_RELEASE = new LinkedBlockingQueue<SendingVo>();// 审核消息队列
	public static BlockingQueue<SendingVo> QUEUE_REPORT = new LinkedBlockingQueue<SendingVo>();// 状态报告队列
	public static BlockingQueue<ResultVo> QUEUE_RESULT = new LinkedBlockingQueue<ResultVo>(); // 用户提交结果统计队列

	public static BlockingQueue<String> QUEUE_ALERT = new LinkedBlockingQueue<String>();  // 短信提醒队列
	public static BlockingQueue<SendingVo> QUEUE_UNKOU_SMS = new LinkedBlockingQueue<SendingVo>();// 未计费队列

	public static BlockingQueue<SendingVo> QUEUE_RATIO = new LinkedBlockingQueue<SendingVo>();// 扣量队列

	// 通道，key:channelId
	public static Map<Integer, Map<String, Object>> CHANNEL = new ConcurrentHashMap<Integer, Map<String, Object>>();

	public static Map<Integer, String> SYS_STRATEGY_GROUP=new ConcurrentHashMap<>();


	// 通道组，key:channelGroupId
	public static Map<Integer, List<Map<String, Object>>> CHANNEL_GROUP = new ConcurrentHashMap<Integer, List<Map<String, Object>>>();

	// 通道签名库，key:expend+store md5
	public static Map<String, List<Map<String, Object>>> CHANNEL_SIGN_EXPEND = new ConcurrentHashMap<String, List<Map<String, Object>>>();

	// 通道黑签名
	public static Map<String, List<Map<String, Object>>> CHANNEL_BLACK_SIGN = new ConcurrentHashMap<String, List<Map<String, Object>>>();

	public static Map<Integer, Map<String, Object>> USER = new ConcurrentHashMap<Integer, Map<String, Object>>();
	// 通道，key:uid
	public static Map<Integer, Map<String, Object>> RPT_RATIO_CONFIG = new ConcurrentHashMap<Integer, Map<String, Object>>();

	public static Map<Integer, Map<String, AtomicLong>> RPT_RATIO_USER_SEND = new ConcurrentHashMap<Integer, Map<String, AtomicLong>>();

	// 短信提醒
	public static Map<Integer, Map<String, Object>> USER_ALERT = new ConcurrentHashMap<Integer, Map<String, Object>>();

	// 用户路由,key:uid
	public static Map<Integer, List<Map<String, Object>>> USER_ROUTE = new ConcurrentHashMap<Integer, List<Map<String, Object>>>();

	// 通用签名库，key:uid
	public static Map<Integer, CopyOnWriteArrayList<UserSign>> USER_SIGN = new ConcurrentHashMap<Integer, CopyOnWriteArrayList<UserSign>>();

	// 私有签名库，key:uid
	public static Map<Integer, List<UserSign>> CHANNEL_SIGN = new ConcurrentHashMap<Integer, List<UserSign>>();

	// 签名库，key:expend
	public static Map<String, UserSign> EXPEND_SIGN = new ConcurrentHashMap<String, UserSign>();

	// 最大自增长拓展ID,普通用户用
	public static AtomicLong MAX_EXPEND2 = new AtomicLong();

	// 用户黑名单，key:uid+号码前5位
	public static Map<String, List<Long>> USER_BLACK_MOBILE = new ConcurrentHashMap<String, List<Long>>();

	public static Map<String, List<Long>> ALL_BLACK_USER_MOBILE = new ConcurrentHashMap<String, List<Long>>();

	// 用户白名单，key:uid+号码前5位
	public static Map<String, List<Long>> USER_WHITE_MOBILE = new ConcurrentHashMap<String, List<Long>>();

	// 用户审核屏蔽词，key:uid
	public static Map<Integer, CopyOnWriteArrayList<String>> USER_BALCK_WORDS = new ConcurrentHashMap<>();

	/**
	 * 用户自动屏蔽词
	 */
	public static Map<Integer, CopyOnWriteArrayList<String>> USER_BALCK_WORDS_AUTO = new ConcurrentHashMap<>();

	// 用户白签名，key:uid
	public static Map<Integer, List<String>> USER_WHITE_SIGN = new ConcurrentHashMap<Integer, List<String>>();

	// 用户模板，key:uid
	public static Map<Integer, List<String>> USER_SMS_TEMPLATE = new ConcurrentHashMap<Integer, List<String>>();

	// 重号过滤，key:日期yyyyMMdd+uid；value key:号码前五位，value：号码
	// public static Map<String, Map<Integer, Map<Long, List<Long>>>>
	// USER_REPEAT_MOBILE = new ConcurrentHashMap<String, Map<Integer, Map<Long,
	// List<Long>>>>();

	/**
	 * 重号过滤缓存
	 */
	public static Map<Integer, Map<Long, List<Long>>> USER_REPEAT_MOBILE = new ConcurrentHashMap<Integer, Map<Long, List<Long>>>();

	/**
	 * 重号初始化标记
	 */
	public static boolean USER_REPEAT_MOBILE_INIT=true;

	public static boolean USER_REPEAT_MOBILE_TEMP_INIT=true;

	public static Map<Integer, Map<Long, List<Long>>> USER_REPEAT_MOBILE_TEMP = new ConcurrentHashMap<Integer, Map<Long, List<Long>>>();

	public static Map<Long, Integer> REPEAT_MOBILE_SIZE = new ConcurrentHashMap<Long, Integer>();

	public static Map<Integer, List<ReleaseTemplateVo>> RELEASE_TEMPLATE = new ConcurrentHashMap<Integer, List<ReleaseTemplateVo>>();

	// 屏蔽地区，key:uid
	public static Map<Integer, List<String>> USER_BLACK_LOCATION = new ConcurrentHashMap<Integer, List<String>>();

	// 屏蔽地区，key:channel_id
	public static Map<Integer, List<String>> CHANNEL_BLACK_LOCATION = new ConcurrentHashMap<Integer, List<String>>();

	public static Map<String, Integer> BLACK_LOCATION_ROUTE = new ConcurrentHashMap<String, Integer>();

	// 用户策略组，key:uid
	public static Map<Integer, Map<Integer, String>> USER_STRATEGY_GROUP = new ConcurrentHashMap<Integer, Map<Integer, String>>();

	// 白名单策略组，key:groupid,subkey号码前5位
	public static Map<Integer, Map<String, List<Long>>> GROUP_WHITE_MOBILE = new ConcurrentHashMap<Integer, Map<String, List<Long>>>();

	// 黑名单策略组，key:groupid,subkey号码前5位
	public static Map<Integer, Map<String, List<Long>>> GROUP_BLACK_MOBILE = new ConcurrentHashMap<Integer, Map<String, List<Long>>>();

	// 白签名策略组，key:groupid
	public static Map<Integer, List<String>> GROUP_WHITE_SIGN = new ConcurrentHashMap<Integer, List<String>>();

	// 黑签名策略组，key:groupid
	public static Map<Integer, List<String>> GROUP_BLACK_SIGN = new ConcurrentHashMap<Integer, List<String>>();

	// 审核屏蔽词策略组，key:groupid
	public static Map<Integer, List<String>> GROUP_RELEASE_WORDS = new ConcurrentHashMap<Integer, List<String>>();

	/**
	 * 审核屏蔽词缓存
	 */
	public static Map<Integer, CopyOnWriteArrayList<SmsBlackWords>> GROUP_RELEASE_WORDS_SCREENTYPE = new ConcurrentHashMap<Integer, CopyOnWriteArrayList<SmsBlackWords>>();

	// �Զ����δʲ����飬key:groupid
	public static Map<Integer, List<String>> GROUP_AUTO_WORDS = new ConcurrentHashMap<Integer, List<String>>();

	/**
	 * 自动屏蔽词缓存
	 */
	public static Map<Integer, CopyOnWriteArrayList<SmsBlackWords>> GROUP_AUTO_WORDS_SCREENTYPE = new ConcurrentHashMap<Integer, CopyOnWriteArrayList<SmsBlackWords>>();

	// 归属地
	public static Map<Integer, Map<Integer, Map<String, Object>>> MOBILE_AREA = new ConcurrentHashMap<Integer, Map<Integer, Map<String, Object>>>();

	// 余额计算标记
	public static int SMS_FLAG = 1;

	// 需要扣款
	public static ConcurrentHashMap<Long, ConcurrentHashMap<String, Integer>> USER_SMS_KOU = new ConcurrentHashMap<>();

	// 用户余额（预扣）
	public static Map<Integer, Integer> USER_SMS_YUKOU = new ConcurrentHashMap<Integer, Integer>();

	// 用户余额（预扣）
	public static Map<Integer, Integer> USER_SMS = new ConcurrentHashMap<Integer, Integer>();

	//redis统计缓存辅助REPEAT_MOBILE_SIZE 的map
	public static ConcurrentHashMap<String,Integer> countMap=new ConcurrentHashMap<String,Integer>();

	/**
	 * 程序启动，只从ehcache加载一次 start
	 */

	/**
	 * 余额提醒
	 */
	public static Boolean USER_ALERT_INIT=true;

	/**
	 * 用户黑名单
	 */
	public static Boolean USER_BLACK_MOBILE_INIT=true;

	public static Boolean ALL_BLACK_USER_MOBILE_INIT=true;

	public static Boolean CHANNEL_SIGN_INIT=true;

	public static Boolean USER_SIGN_INIT=true;

	public static Boolean EXPEND_SIGN_INIT=true;

	public static Boolean GROUP_WHITE_MOBILE_INIT=true;
	public static Boolean GROUP_WHITE_SIGN_INIT=true;
	public static Boolean GROUP_BLACK_MOBILE_INIT=true;
	public static Boolean GROUP_BLACK_SIGN_INIT=true;
	public static Boolean GROUP_RELEASE_WORDS_SCREENTYPE_INIT=true;
	public static Boolean GROUP_AUTO_WORDS_SCREENTYPE_INIT=true;
	public static Boolean MOBILE_AREA_INIT=true;

	/**
	 * 执行缓存本地化操作标记
	 */
	public static Boolean CACHE_FLAG=true;

	/**
	 * end
	 */

	/**
	 * 线路0-url
	 */
	public static int line0=0;

	/**
	 * 线路1-url
	 */
	public static int line1=1;

	/**
	 * 线路url
	 */
	public static Map<Integer,String> LINE=new ConcurrentHashMap<>();

	/**
	 * 用户线路同步余额到数据库标记
	 */
	public static Map<Integer, Integer> USER_LINE_SYNC_STATE = new ConcurrentHashMap<Integer, Integer>();

	/**
	 * 标记为1,则把队列的消息推送到另一台服务器
	 */
	public static Map<Integer, Integer> USER_LINE = new ConcurrentHashMap<Integer, Integer>();

	public static BlockingQueue<SendingVo> QUEUE_SENDING_PRIORITIZED_LINE = new LinkedBlockingQueue<SendingVo>();

	public static AtomicLong ATOMIC = new AtomicLong();
	public static AtomicLong SUB_ATOMIC = new AtomicLong();

	public static AtomicLong NOSIGN_TIME = new AtomicLong();
	public static AtomicInteger NOSIGN_COUNT = new AtomicInteger();

	public static long LOG_TIME_SIGN = 0L;
	public static int LOG_TIME_COUNT = 0;

	public static int hisId = 0;

	public synchronized static int getHisID() {
		if(SmsCache.SERVER_TYPE == 0 && (hisId < 10000000 || hisId >= 999999999)){
			hisId = 10000000;
		}else if(SmsCache.SERVER_TYPE == 1 && (hisId < 1000000000 || hisId >= 2000000000)){
			hisId = 1000000000;
		}
		hisId++;
		return hisId;
	}
}