package com.sioo.db.druid;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.druid.pool.DruidPooledConnection;

public class DBPool {
	private DBPool() {
	}

	private static DBPool databasePool = null;
	private static DruidDataSource dds = null;
	private static Logger log = Logger.getLogger(DBPool.class);

	static {
		try {
			InputStream in = DBPool.class.getClassLoader().getResourceAsStream("druid.properties");
			Properties props = new Properties();
			props.load(in);

			dds = (DruidDataSource) DruidDataSourceFactory.createDataSource(props);
		} catch (Exception e) {
			log.error("[DBPool static Exception]", e);
		}
	}

	public static synchronized DBPool getInstance() {
		if (null == databasePool) {
			databasePool = new DBPool();
		}
		return databasePool;
	}

	public DruidPooledConnection getConnection() throws SQLException {
		return dds.getConnection();
	}
}