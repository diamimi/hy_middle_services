package com.sioo.db.mybatis;

import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.sioo.log.LogInfo;

public class SessionFactory {
	private static SqlSessionFactory sqlSessionFactory;
	private static Reader reader;

	static {
		try {
			reader = Resources.getResourceAsReader("mybatis-configuration.xml");
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("获取SessionFactory异常", "[SessionFactory.Static Exception]" + LogInfo.getTrace(e));
		}
	}

	public static SqlSessionFactory getSessionFactory() {
		return sqlSessionFactory;
	}
}
