package com.sioo.cache;

import com.sioo.dao.SmsSendHistoryUnknownDao;
import com.sioo.dao.SysCacheDao;
import com.sioo.db.mongo.MongoManager;
import com.sioo.log.LogInfo;
import com.sioo.util.SmsCache;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/***
 * 配置文件相关缓存
 *
 * @author OYJM
 * @date 2016年10月31日
 *
 */
public class ConfigCache {
	private static Logger log = Logger.getLogger(ConfigCache.class);
	private static ConfigCache configCache = null;

	public static ConfigCache getInstance() {
		if (configCache != null) {
			return configCache;
		}
		synchronized (ConfigCache.class) {
			if (configCache == null) {
				configCache = new ConfigCache();
			}
		}
		return configCache;
	}

	/***
	 * 初始化配置信息
	 */
	public void initConfig() {
		Properties prop = new Properties();
		InputStream inputStream = null;
		try {

			inputStream = getClass().getResourceAsStream("/config.properties");
			prop.load(inputStream);

			ConfigCache configCache = ConfigCache.getInstance();
			configCache.setServerType(Integer.valueOf(prop.getProperty("server.type").trim()));
			configCache.setAlertMsgFlag(Boolean.valueOf(prop.getProperty("alertMsg.flag").trim()));
			configCache.setAlertMsgDayMobiles(new ArrayList<String>(Arrays.asList(prop.getProperty("alertMsg.dayMobiles").trim().split(","))));
			configCache.setAlertMsgNightMobiles(new ArrayList<String>(Arrays.asList(prop.getProperty("alertMsg.nightMobiles").trim().split(","))));
//			if (configCache.getAlertMsgFlag()) {
//				log.info("告警信息已打开。\r\n白天告警号码：" + configCache.getAlertMsgDayMobiles() + "  晚上告警号码：" + configCache.getAlertMsgNightMobiles());
//			} else {
//				log.info("告警信息已关闭。");
//			}
			configCache.setMongoHost(prop.getProperty("mongo.host").trim());
			configCache.setMongoPort(Integer.valueOf(prop.getProperty("mongo.port").trim()));
			configCache.setMongoDbName(prop.getProperty("mongo.dbName").trim());

			configCache.setRabbitPort1(Integer.valueOf(prop.getProperty("rabbit1.port").trim()));
			configCache.setRabbitHost1(prop.getProperty("rabbit1.host").trim());
			configCache.setRabbitUserName1(prop.getProperty("rabbit1.userName").trim());
			configCache.setRabbitPassword1(prop.getProperty("rabbit1.password").trim());
			configCache.setRabbitPrefetchSize1(Integer.valueOf(prop.getProperty("rabbit1.prefetchSize").trim()));

			configCache.setRabbitPort0(Integer.valueOf(prop.getProperty("rabbit0.port").trim()));
			configCache.setRabbitHost0(prop.getProperty("rabbit0.host").trim());
			configCache.setRabbitUserName0(prop.getProperty("rabbit0.userName").trim());
			configCache.setRabbitPassword0(prop.getProperty("rabbit0.password").trim());
			configCache.setRabbitPrefetchSize0(Integer.valueOf(prop.getProperty("rabbit0.prefetchSize").trim()));

			configCache.setLine(prop.getProperty("line0").trim(),prop.getProperty("line1").trim());

		} catch (IOException ex) {
			log.error("加载配置文件异常。", ex);
		}
		log.info("[TopicsMain.initConfig()] success...");
	}

	public void setServerType(Integer serverType){
		SmsCache.SERVER_TYPE = serverType;
	}

	public Integer getServerType(){
		return SmsCache.SERVER_TYPE;
	}

	public void setAlertMsgFlag(boolean alertMsgFlag) {
		SmsCache.alertMsgFlag = alertMsgFlag;
	}

	public boolean getAlertMsgFlag() {
		return SmsCache.alertMsgFlag;
	}

	public void setAlertMsgDayMobiles(List<String> alertMsgDayMobiles) {
		SmsCache.alertMsgDayMobiles = alertMsgDayMobiles;
	}

	public void addAlertMsgDayMobile(String alertMsgDayMobile) {
		if (SmsCache.alertMsgDayMobiles == null) {
			SmsCache.alertMsgDayMobiles = new ArrayList<String>();
		}
		if (!SmsCache.alertMsgDayMobiles.contains(alertMsgDayMobile)) {
			SmsCache.alertMsgDayMobiles.add(alertMsgDayMobile);
		}
	}

	public List<String> getAlertMsgDayMobiles() {
		return SmsCache.alertMsgNightMobiles;
	}

	public void setAlertMsgNightMobiles(List<String> alertMsgNightMobiles) {
		SmsCache.alertMsgNightMobiles = alertMsgNightMobiles;
	}

	public void addAlertMsgNightMobile(String alertMsgNightMobile) {
		if (SmsCache.alertMsgNightMobiles == null) {
			SmsCache.alertMsgNightMobiles = new ArrayList<String>();
		}
		if (!SmsCache.alertMsgNightMobiles.contains(alertMsgNightMobile)) {
			SmsCache.alertMsgNightMobiles.add(alertMsgNightMobile);
		}
	}

	public List<String> getAlertMsgNightMobiles() {
		return SmsCache.alertMsgNightMobiles;
	}

	/***** mongo cache begin *****/
	public void setMongoHost(String mongoHost) {
		SmsCache.mongoHost = mongoHost;
	}

	public String getMongoHost() {
		return SmsCache.mongoHost;
	}

	public void setMongoPort(int mongoPort) {
		SmsCache.mongoPort = mongoPort;
	}

	public int getMongoPort() {
		return SmsCache.mongoPort;
	}

	public void setMongoDbName(String mongoDbName) {
		SmsCache.mongoDbName = mongoDbName;
	}

	public String getMongoDbName() {
		return SmsCache.mongoDbName;
	}

	/***** mongo cache end *****/

	/***** rabbitmq cache begin *****/
	public void setRabbitPort(int rabbitPort) {
		SmsCache.RABBIT_PORT = rabbitPort;
	}

	public int getRabbitPort() {
		return SmsCache.RABBIT_PORT;
	}

	public void setRabbitHost(String rabbitHost) {
		SmsCache.RABBIT_HOST = rabbitHost;
	}

	public String getRabbitHost() {
		return SmsCache.RABBIT_HOST;
	}

	public void setRabbitUserName(String rabbitUserName) {
		SmsCache.RABBIT_USERNAME = rabbitUserName;
	}

	public String getRabbitUserName() {
		return SmsCache.RABBIT_USERNAME;
	}

	public void setRabbitPassword(String rabbitPassword) {
		SmsCache.RABBIT_PASSWORD = rabbitPassword;
	}

	public String getRabbitPassword() {
		return SmsCache.RABBIT_PASSWORD;
	}

	public void setRabbitPrefetchSize(int rabbitPrefetchSize) {
		SmsCache.RABBIT_PREFETCH_SIZE = rabbitPrefetchSize;
	}

	public int getRabbitPrefetchSize() {
		return SmsCache.RABBIT_PREFETCH_SIZE;
	}


	/**
	 * rabitt20
	 * @param rabbitPort
	 */
	public void setRabbitPort1(int rabbitPort) {
		SmsCache.RABBIT_PORT1 = rabbitPort;
	}

	public int getRabbitPort1() {
		return SmsCache.RABBIT_PORT1;
	}

	public void setRabbitHost1(String rabbitHost) {
		SmsCache.RABBIT_HOST1 = rabbitHost;
	}

	public String getRabbitHost1() {
		return SmsCache.RABBIT_HOST1;
	}

	public void setRabbitUserName1(String rabbitUserName) {
		SmsCache.RABBIT_USERNAME1= rabbitUserName;
	}

	public String getRabbitUserName1() {
		return SmsCache.RABBIT_USERNAME1;
	}

	public void setRabbitPassword1(String rabbitPassword) {
		SmsCache.RABBIT_PASSWORD1 = rabbitPassword;
	}

	public String getRabbitPassword1() {
		return SmsCache.RABBIT_PASSWORD1;
	}

	public void setRabbitPrefetchSize1(int rabbitPrefetchSize) {
		SmsCache.RABBIT_PREFETCH_SIZE1 = rabbitPrefetchSize;
	}

	public int getRabbitPrefetchSize1() {
		return SmsCache.RABBIT_PREFETCH_SIZE1;
	}



	/**
	 * rabitt50
	 * @param rabbitPort
	 */
	public void setRabbitPort0(int rabbitPort) {
		SmsCache.RABBIT_PORT0 = rabbitPort;
	}

	public int getRabbitPort0() {
		return SmsCache.RABBIT_PORT0;
	}

	public void setRabbitHost0(String rabbitHost) {
		SmsCache.RABBIT_HOST0 = rabbitHost;
	}

	public String getRabbitHost0() {
		return SmsCache.RABBIT_HOST0;
	}

	public void setRabbitUserName0(String rabbitUserName) {
		SmsCache.RABBIT_USERNAME0 = rabbitUserName;
	}

	public String getRabbitUserName0() {
		return SmsCache.RABBIT_USERNAME0;
	}

	public void setRabbitPassword0(String rabbitPassword) {
		SmsCache.RABBIT_PASSWORD0 = rabbitPassword;
	}

	public String getRabbitPassword0() {
		return SmsCache.RABBIT_PASSWORD0;
	}

	public void setRabbitPrefetchSize0(int rabbitPrefetchSize) {
		SmsCache.RABBIT_PREFETCH_SIZE0= rabbitPrefetchSize;
	}

	public int getRabbitPrefetchSize0() {
		return SmsCache.RABBIT_PREFETCH_SIZE0;
	}

	/***** rabbitmq cache end *****/

	public void loadControl() {
		try {
			boolean flag = SysCacheDao.getInstance().findSysConfig() == 1 ? true : false;
			SmsCache.CONTROL = flag;
			log.info("系统控制加载【" + flag + "】");
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("加载系统控制属性异常", "[ConfigCache.loadControl() ]" + LogInfo.getTrace(e));
		}
	}

	/***
	 * 加载最大的历史记录ID
	 *
	 * @param mongo
	 */
	public void loadHisId(MongoManager mongo) {
		try {
			int maxHisId;
			try {
				maxHisId = SmsSendHistoryUnknownDao.getInstance().findMaxHisId(mongo);//从mogodb取,加载最大的值
			} catch (Exception e) {
				maxHisId = 0;
			}
			if(maxHisId == 0){
				if(SmsCache.SERVER_TYPE == null || SmsCache.SERVER_TYPE == 0){
					maxHisId = 10000001;
				}else{
					maxHisId = 1000000001;
				}
			}
			SmsCache.hisId = maxHisId;
			log.info("加载历史记录最大ID: " + maxHisId);
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("加载缓存中历史记录最大ID异常", "[UserSmsCache.loadHisId() ]" + LogInfo.getTrace(e));
		}
	}


	public void setLine(String line0,String line1) {
		SmsCache.LINE.put(SmsCache.line0,line0);
		SmsCache.LINE.put(SmsCache.line1,line1);
	}

}
