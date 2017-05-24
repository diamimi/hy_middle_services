package com.sioo.db.druid;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.druid.pool.DruidPooledConnection;

public class DBConnect {

	private DruidPooledConnection dpConn = null;
	private Statement stmt = null;
	private PreparedStatement pstmt = null;
	private ResultSet rs = null;

	public DruidPooledConnection getConnection(boolean autoCommit) throws SQLException {
		dpConn = DBPool.getInstance().getConnection();
		if (!autoCommit) {
			dpConn.setAutoCommit(false);
		}
		return dpConn;
	}

	private ResultSet getResultSet(String sql) throws SQLException {
		dpConn = this.getConnection(true);
		stmt = dpConn.createStatement();
		return stmt.executeQuery(sql);
	}

	protected void close() throws SQLException {
		if (rs != null) {
			rs.close();
			rs = null;
		}
		if (stmt != null) {
			stmt.close();
			stmt = null;
		}
		if (pstmt != null) {
			pstmt.close();
			pstmt = null;
		}
		if (dpConn != null) {
			dpConn.close();
			dpConn = null;
		}
	}

	/**
	 * 查询多行数据，List<Map<String, Object>>
	 * 
	 * @param sql
	 * @return List<Map<String, Object>>
	 * @throws SQLException
	 * */
	protected List<Map<String, Object>> findModelRows(String sql) throws SQLException {
		Map<String, Object> map = null;
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			rs = this.getResultSet(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			while (rs.next()) {
				map = new HashMap<String, Object>();
				for (int i = 1; i <= columnCount; i++) {
					map.put(rsmd.getColumnName(i), rs.getObject(i));
				}
				list.add(map);
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			this.close();
		}
		return list;
	}

	/**
	 * 查询唯一值
	 * 
	 * @param sql
	 * @return Object
	 * @throws SQLException
	 * */
	protected List<Object> findSingleColumn(String sql) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		try {
			rs = getResultSet(sql);
			while (rs.next()) {
				list.add(rs.getObject(1));
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			this.close();
		}
		return list;
	}

	/**
	 * 查询唯一值
	 * 
	 * @param sql
	 * @return Object
	 * @throws SQLException
	 * */
	protected Object findUniqueValue(String sql) throws SQLException {
		Object value = null;
		try {
			ResultSet rs = getResultSet(sql);
			if (rs.next()) {
				value = rs.getObject(1);
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			this.close();
		}
		return value;
	}

	/**
	 * 增加 删除 修改
	 * 
	 * @return
	 * @throws SQLException
	 * */
	protected int executeUpdate(String sql, Object[] obj) throws Exception {
		int num = 0;
		try {
			dpConn = this.getConnection(true);
			pstmt = dpConn.prepareStatement(sql);
			if (obj != null && obj.length > 0) {
				for (int i = 1; i <= obj.length; i++) {
					pstmt.setObject(i, obj[i - 1]);
				}
			}
			num = pstmt.executeUpdate();
		} catch (Exception e) {
			throw e;
		} finally {
			this.close();
		}
		return num;
	}

	/**
	 * 插入或更新并返回id
	 * 
	 * @throws SQLException
	 * */
	protected long executeUpdateReturnKeys(String sql, Object[] obj) throws Exception {
		long id = 0L;
		try {
			dpConn = this.getConnection(true);
			pstmt = dpConn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			if (obj != null && obj.length > 0) {
				for (int i = 1; i <= obj.length; i++) {
					pstmt.setObject(i, obj[i - 1]);
				}
			}
			pstmt.executeUpdate();
			ResultSet rsKeys = pstmt.getGeneratedKeys();
			if (rsKeys.next()) {
				id = rsKeys.getLong(1);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			this.close();
		}
		return id;
	}

	/**
	 * 批量插入，多行多列操作
	 * 
	 * @param sql
	 *            sql
	 * @param params
	 *            参数列表
	 * @return int 影响行数
	 * @throws SQLException
	 * */
	protected int executeByPrepareStatement(String sql, List<Object[]> params) throws Exception {
		int len = 0;
		try {
			dpConn = this.getConnection(false);
			pstmt = dpConn.prepareStatement(sql);
			if (params != null && params.size() > 0) {
				for (Object[] objs : params) {
					for (int i = 1; i <= objs.length; i++) {
						pstmt.setObject(i, objs[i - 1]);
					}
					pstmt.addBatch();
				}
			}
			int[] num = pstmt.executeBatch();
			for (int i : num) {
				len += i;
			}
			dpConn.commit();
			dpConn.setAutoCommit(true);
		} catch (Exception e) {
			throw e;
		} finally {
			this.close();
		}
		return len;
	}

	/**
	 * 批量多行1列操作
	 * 
	 * @param sql
	 * @param columnsValue
	 *            列值
	 * @return boolean
	 * @throws SQLException
	 * */
	protected int executeByPrepareStatementSingleColumn(String sql, Object[] columnsValue) throws SQLException {
		int len = 0;
		try {
			dpConn = this.getConnection(false);
			pstmt = dpConn.prepareStatement(sql);
			if (columnsValue != null && columnsValue.length > 0) {
				for (Object string : columnsValue) {
					pstmt.setObject(1, string);
					pstmt.addBatch();
				}
			}
			int[] num = pstmt.executeBatch();
			for (int i : num) {
				len += i;
			}
			dpConn.commit();
			dpConn.setAutoCommit(true);
		} catch (SQLException e) {
			throw e;
		} finally {
			this.close();
		}
		return len;
	}

	/**
	 * 增加 删除 修改
	 * 
	 * @return
	 * @throws SQLException
	 * */
	protected int executeByStatement(List<String> sqls) throws SQLException {
		int len = 0;
		try {
			dpConn = this.getConnection(false);
			stmt = dpConn.createStatement();
			for (String s : sqls) {
				stmt.addBatch(s);
			}
			int[] num = stmt.executeBatch();
			for (int i : num) {
				len += i;
			}
			dpConn.commit();
			dpConn.setAutoCommit(true);
		} catch (SQLException e) {
			throw e;
		} finally {
			this.close();
		}
		return len;
	}
}
