package com.sioo.main;

import com.sioo.cache.*;
import com.sioo.db.mongo.MongoManager;
import com.sioo.mq.ReceiveBatchSmsRunnable;
import com.sioo.mq.ReceiveSmsChannelCheck;
import com.sioo.mq.ReceiveSmsRunnable;
import com.sioo.servlet.HttpSubmitServer;
import com.sioo.sms.handle.taskloop.SyncReleaseTemplate;
import com.sioo.sms.handle.taskloop.SyncRptRatioConfig;
import com.sioo.sms.handle.taskloop.SyncSign;
import com.sioo.sms.handle.taskloop.SyncSmsDB;
import com.sioo.thread.*;
import com.sioo.util.RabbitMQProducerUtil;
import com.sioo.util.SmsCache;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.Calendar;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/****
 * 主方法
 *
 * @author OYJM
 * @date 2016年10月19日
 *
 */
public class TopicsMain {

	private static Logger log = Logger.getLogger(TopicsMain.class);

	/***
	 * 主入口
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		// 声明mongoDB、RabbitMQ操作类对象
		try {
			TopicsMain.initSystemCache();
			TopicsMain.start();
			TopicsMain.jettyServerStart();
		} catch (Exception e) {
			log.error("启动异常");
			e.printStackTrace();
		}

		log.info("hy_middle_services server start success...");
		log.info("==========================================");
	}

	/***
	 * 启动所有线程
	 */
	private static void start() throws Exception {
		log.info("[Threads.start()] start...");
		MongoManager mongo = MongoManager.getInstance();
		RabbitMQProducerUtil util = RabbitMQProducerUtil.getProducerInstance();
		//RabbitMQProducerUtil2 util2 = RabbitMQProducerUtil2.getProducerInstance();
		ConfigCache.getInstance().loadHisId(mongo);

		new Thread(new SmsSendHistoryUnknownSave(mongo,util)).start();// 消息入MongoDB库
		new Thread(new SmsSendHistoryBatchSave(mongo)).start(); // 批次消息入MongoDB库,针对前台批量提交,内容相同的短信
		new Thread(new SmsReportSave(mongo,util)).start(); // RPT入MongoDB库,校验失败,返回状态给用户,存到用户推送表和短信状态表里
		new Thread(new SmsUserSendingReleaseSave()).start(); //签名等触发了审核, 审核入mysql库
		new Thread(new SmsUserSendingSave()).start(); // 通道停止,进入队列消息入mysql库
		new Thread(new ResultSave(mongo)).start(); // 用户发送统计入mongo库,用户的日报表(每天的发送量)

		/**
		 * 校验优先短信
		 */
		// 启动3个线程处理优先队列短信
		for (int i = 0; i < 3; i++) {
			new Thread(new SmsCheckThread(1, util)).start();  //处理本地优先的消息,校验等
		}

		/**
		 * 校验普通短信
		 */
		// 启动10个线程处理普通队列短信
		for (int i = 0; i < 10; i++) {
			new Thread(new SmsCheckThread(0, util)).start();//处理本地普通的消息,校验等
		}


		// CMPP消息队列
		new Thread(new ReceiveSmsRunnable("SUBMIT_CMPP_PRIORITY", util)).start();//接受用户提交的消息,放到本地队列,优先的消息,验证码
		// HTTP消息队列
		new Thread(new ReceiveSmsRunnable("SUBMIT_HTTP_PRIORITY", util)).start();//接受用户提交的消息,放到本地队列,优先的消息,验证码

		// 普通消息队列，用户提交队列采用用户账号第一位数字做为入队标准
		for (int i = 1; i < 10; i++) {
			new Thread(new ReceiveSmsRunnable("SUBMIT_QUEUE_" + i, util)).start();//接受用户提交的消息,放到本队队列,普通的短信
		}

		// 批次消息队列
		new Thread(new ReceiveBatchSmsRunnable("BATCH_QUEUE_TEMP", util)).start();

		new Thread(new ReceiveSmsChannelCheck(util,mongo)).start();//取从后台补发的消息

		// 处理审核信息
		new Thread(new SmsSendReleaseSync(util, mongo)).start();//取从后台审核的消息

		// 提醒消息
		new Thread(new SmsAlertThread()).start();//余额提醒
		new Thread(new SmsUnKouSave()).start();//余额不足,保存到消费记录
		new Thread(new SmsRatioThread()).start();//
		// 启动同步任务同步余额以及消费记录
		SyncSmsDB.sysnSms();
		// 启动签名同步
		SyncSign.sysnCache();
		// 启动审核模板同步
		SyncReleaseTemplate.sysnCache();
		// 用户扣量信息同步
		SyncRptRatioConfig.sysnCache();

		log.info("[Threads.start()] success...");
	}

	/***
	 * 初始化缓存
	 *
	 * @param
	 */
	private static void initSystemCache() throws Exception {
		log.info("[TopicsMain.initSystemCache()] start...");

		// 加载配置文件
		ConfigCache.getInstance().initConfig();
		// 通道相关信息
		ChannelCache.getInstance().loadChannel();
		ChannelCache.getInstance().loadGroupChannel(0);
		// 用户相关信息
		UserCache.getInstance().loadUser();
		UserRouteCache.getInstance().loadUserRoute(0);
		UserSignCache.getInstance().loadUserSign(0, null);
		UserSmsAlertCache.getInstance().loadUserSmsAlert(0);
		RptRatioConfigCache.getInstance().loadRptRatioConfig(0);

		// 校验相关信息
		UserWhiteMobileCache.getInstance().loadUserWhiteMobile(0);
		UserWhiteSignCache.getInstance().loadUserWhiteSign(0);
		UserBlackMobileCache.getInstance().loadUserBlackMobile(0);
		UserBlackMobileCache.getInstance().loadAllUserBlackMobile();
		UserBlackWordsCache.getInstance().loadUserBlackWords(0);
		/**
		 * 加载用户自动屏蔽词
		 */
		UserBlackWordsCache.getInstance().loadUserBlackWordsAuto(0);
		UserMsgTemplateCache.getInstance().loadUserMsgTemplate(0);
		StrategyGroupCache.getInstance().loadUserStrategyGroup(0);
		StrategyGroupCache.getInstance().loadStrategyGroup();

		MobileAreaCache.getInstance().loadMobileArea(0,null);
		UserBlackLocationCache.getInstance().loadUserBlackLocation(0);
		UserBlackLocationCache.getInstance().loadChannelBlackLocation(0);
		ReleaseTemplateCache.getInstance().loadReleaseTemplate(0);

		// 加载保存在文件中用户发送条数
		UserSmsCache.getInstance().loadSmsCacheByTxt();
		// 重号过滤缓存清理
		Long delay = 24 * 3600L * 1000;// 间隔时间1天
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);// 每天0点2分开始执行
		cal.set(Calendar.MINUTE, 2);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Long diff = (cal.getTimeInMillis() - System.currentTimeMillis());
		new Timer().schedule(new ClearRepeatMobileCache(), diff, delay);

		SmsCache.NOSIGN_COUNT = new AtomicInteger(0);
		SmsCache.NOSIGN_TIME = new AtomicLong(0L);
		log.info("[TopicsMain.initSystemCache()] success...");
	}

	/***
	 * 启动JettyServer
	 */
	private static void jettyServerStart() throws Exception {
		log.info("[TopicsMain.jettyServerStart()] start...");

		Server server = new Server(8090);
		server.setAttribute("org.eclipse.jetty.server.Request.maxFormContentSize",-1);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/hy_middle_services");
		context.setResourceBase(".");
		server.setHandler(context);

		context.addServlet(new ServletHolder(new HttpSubmitServer()), "/updateCache");
		server.start();
		server.join();

		log.info("[TopicsMain.jettyServerStart()] success...");
	}
}
