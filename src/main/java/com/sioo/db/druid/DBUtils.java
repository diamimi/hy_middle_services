package com.sioo.db.druid;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DBUtils extends DBConnect {

	/**
	 * 获取数据库实例
	 * */
	public static DBUtils getInstance() {
		return new DBUtils();
	}

	/**
	 * 查询多行List<Map<String, Object>>
	 * 
	 * @throws SQLException
	 * */
	public List<Map<String, Object>> findModelRows(String sql) throws SQLException {
		return super.findModelRows(sql);
	}

	/**
	 * 查询一行，mysql统计行数用as 别名，取参数，map.get(别名)
	 * 
	 * @throws SQLException
	 * */
	public Map<String, Object> findSingleRow(String sql) throws SQLException {
		List<Map<String, Object>> list = super.findModelRows(sql);
		if (null == list || list.size() == 0) {
			return null;
		}
		return list.get(0);
	}

	/**
	 * 查询1列
	 * 
	 * @throws SQLException
	 * */
	public List<Object> findSingleColumn(String sql) throws SQLException {
		return super.findSingleColumn(sql);
	}

	/**
	 * 查询唯一值，mysql统计行数用as 别名，取参数，map.get(别名)
	 * 
	 * @return int
	 * @throws SQLException
	 * */
	public Object findUniqueValue(String sql) throws SQLException {
		return super.findUniqueValue(sql);
	}

	/**
	 * 批量操作多行多列
	 * 
	 * @param params
	 * @return
	 * */
	public int executeByPrepareStatement(String sql, List<Object[]> params) throws Exception {
		return super.executeByPrepareStatement(sql, params);
	}

	/**
	 * 批量操作多行1列
	 * 
	 * @param params
	 * @return
	 * @throws SQLException
	 * */
	public int executeByPrepareStatementSingleColumn(String sql, Object[] columnsValue) throws SQLException {
		return super.executeByPrepareStatementSingleColumn(sql, columnsValue);
	}

	/**
	 * 增删改
	 * 
	 * @param params
	 * @return 影响行数
	 * */
	public int executeUpdate(String sql) throws Exception {
		return super.executeUpdate(sql, null);
	}

	/**
	 * 增删改
	 * 
	 * @param params
	 * @return 影响行数
	 * @throws SQLException
	 * */
	public int executeUpdate(String sql, Object[] obj) throws Exception {
		return super.executeUpdate(sql, obj);
	}

	/**
	 * 插入并返回ID
	 * 
	 * @param sql
	 * @return id;
	 * @throws SQLException
	 * */
	public long executeUpdateReturnKeys(String sql) throws Exception {
		return super.executeUpdateReturnKeys(sql, null);
	}

	/**
	 * 插入并返回ID
	 * 
	 * @param sql
	 * @param obj
	 *            参数列表
	 * @return id;
	 * */
	public long executeRupdateReturnKeys(String sql, Object[] obj) throws Exception {
		return super.executeUpdateReturnKeys(sql, obj);
	}

	public int executeByStatement(List<String> sqls) throws SQLException {
		return super.executeByStatement(sqls);
	}

	public int executeByStatement(String[] sqls) throws SQLException {
		List<String> listSql = new ArrayList<String>();
		for (String s : sqls) {
			listSql.add(s);
		}
		return super.executeByStatement(listSql);
	}
}
