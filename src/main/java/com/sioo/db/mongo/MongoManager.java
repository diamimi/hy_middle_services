package com.sioo.db.mongo;

import com.mongodb.*;
import com.sioo.cache.ConfigCache;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @作者 Liloo
 * @E-mail liloo@vip.qq.com
 * @时间 2015年5月14日
 * @版权 © Liloo 版权所有.
 */
public class MongoManager {
	private static Logger log = Logger.getLogger(MongoManager.class);

	private static MongoClient mongo = null;

	private static MongoManager instance = new MongoManager();

	private static ConfigCache configCache = ConfigCache.getInstance();

	public static MongoManager getInstance() {
		try {
			init();
			return instance;
		} catch (Exception e) {
			log.error("获取mongodb实例异常。", e);
		}
		return null;
	}

	/**
	 * 初始化MongoDB
	 */
	private static void init() {
		try {
			if (mongo == null)
				mongo = new MongoClient(configCache.getMongoHost(), configCache.getMongoPort());
		} catch (Exception e) {
			log.error("初始化mongodb异常。\r\n", e);
		}
	}

	/**
	 * 获取DB对象
	 * 
	 * @return
	 */
	private DB getDB() throws Exception {
		if (mongo == null) {
			init();
		}
		return mongo.getDB(configCache.getMongoDbName());
	}

	/**
	 * 获取集合对象
	 * 
	 * @param name
	 * @return
	 */
	private DBCollection getCollection(String name) throws Exception {
		return getDB().getCollection(name);
	}

	/**
	 * 插入MongoDB
	 * 
	 * @param name
	 * @param obj
	 */
	public void insert(String name, DBObject obj) throws Exception {
		getCollection(name).insert(obj);
	}

	/**
	 * 批量插入MongoDB
	 * 
	 * @param name
	 * @param obj
	 */
	public void batchInsert(String name, List<DBObject> obj) throws Exception {
		getCollection(name).insert(obj);
	}

	/**
	 * 删除指定条件的数据
	 * 
	 * @param name
	 * @param obj
	 */
	public void delete(String name, DBObject obj) throws Exception {
		getCollection(name).remove(obj);
	}

	public void deleteAll(String collection) throws Exception {
		List<DBObject> rs = findAll(collection);
		if (rs != null && !rs.isEmpty()) {
			for (int i = 0; i < rs.size(); i++) {
				getCollection(collection).remove(rs.get(i));
			}
		}
	}



	/**
	 * 如果更新的数据 不存在 插入一条数据
	 * 
	 */
	public void update1(String name, DBObject set, DBObject where) throws Exception {
		getCollection(name).update(where, set, true, false);
	}

	/**
	 * 只更新存在的数据,不会新增. 批量更新.
	 * 
	 * @param name
	 */
	public void update2(String name, DBObject set, DBObject where) throws Exception {
		getCollection(name).update(where, new BasicDBObject("$set", set), false, true);
	}


	/**
	 * $inc 用来增加已有键的值
	 *
	 * @param name
	 * @param setFields
	 * @param whereFields
	 */
	public void update3(String name, DBObject set, DBObject where) throws Exception {
		getCollection(name).update(where, new BasicDBObject("$inc", set), true, true);
	}


	/**
	 * $inc 用来增加已有键的值或者$set更新，可以同时存在
	 *
	 * @param name
	 * @param setFields
	 * @param whereFields
	 */
	public WriteResult update4(String name, DBObject set, DBObject where) throws Exception {
		return getCollection(name).update(where, set, false, true);
	}

	/***
	 * 根据条件更新自增长
	 * 
	 * @param name
	 * @param inc
	 * @param where
	 * @throws Exception
	 */
	public void updateByInc(String name, DBObject inc, DBObject where) throws Exception {
		getCollection(name).update(where, new BasicDBObject("$inc", inc), true, true);
	}

	/**
	 * 按照ObjectId,批量更新.
	 * 
	 * @param name
	 * @param obj
	 * @param set
	 */
	public void updateBatchByObjectId(String name, String obj, DBObject set) throws Exception {
		if (obj == null || obj == "") {
			return;
		}
		String[] id = obj.split(",");
		for (int i = 0; i < id.length; i++) {
			BasicDBObject dest = new BasicDBObject();
			BasicDBObject doc = new BasicDBObject();
			dest.put("_id", ObjectId.massageToObjectId(id[i]));
			doc.put("$set", set);
			getCollection(name).update(dest, doc, false, true);
		}
	}

	/**
	 * 查询全部
	 * 
	 * @param name
	 * @return
	 */
	public List<DBObject> findAll(String name) throws Exception {
		return getCollection(name).find().toArray();
	}

	/**
	 * 查询1条记录
	 * 
	 * @param name
	 * @param obj
	 * @return
	 */
	public DBObject findOne(String name, DBObject obj) throws Exception {
		DBCollection coll = getCollection(name);
		return coll.findOne(obj);
	}

	/**
	 * 查询指定条数的记录
	 * 
	 * @param name
	 * @param obj
	 * @param limit
	 * @return
	 */
	public List<DBObject> find(String name, DBObject obj, int limit) throws Exception {
		DBCollection coll = getCollection(name);
		DBCursor c = coll.find(obj).limit(limit);
		if (c != null) {
			return c.toArray();
		} else {
			return null;
		}
	}

	public List<DBObject> getMaxDBObject(String name, DBObject orderBy) throws Exception {
		DBCollection coll = getCollection(name);
		DBCursor c = coll.find().sort(orderBy).limit(1);
		if (c != null) {
			return c.toArray();
		} else {
			return null;
		}
	}
	
	public List<DBObject> getMaxDBObject(String name, DBObject where, DBObject orderBy) throws Exception {
		DBCollection coll = getCollection(name);
		DBCursor c = coll.find(where).sort(orderBy).limit(1);
		if (c != null) {
			return c.toArray();
		} else {
			return null;
		}
	}

	/**
	 * 查询符合的全部数据
	 * 
	 * @param name
	 * @param where
	 * @return
	 */
	public List<DBObject> find(String name, DBObject where) throws Exception {
		DBCursor c = getCollection(name).find(where);
		if (c != null) {
			return c.toArray();
		}
		return null;
	}

	public List<DBObject> find(String name, DBObject where, DBObject orderBy) throws Exception {
		DBCursor c = getCollection(name).find(where).sort(orderBy);
		if (c != null) {
			return c.toArray();
		}
		return null;
	}
	/**
	 * 返回Queue的查询
	 * 
	 * @param name
	 * @param where
	 * @return
	 * @throws Exception
	 */
	public LinkedBlockingQueue<DBObject> findQueue(String name, DBObject where) throws Exception {
		LinkedBlockingQueue<DBObject> queue = new LinkedBlockingQueue<DBObject>();
		DBCursor c = getCollection(name).find(where);
		if (c != null) {
			for (DBObject obj : c) {
				obj = c.next();
				queue.offer(obj);
			}
			return queue;
		}
		return null;
	}

	/**
	 * 关闭Mongo链接
	 */
	public void close() throws Exception {
		if (mongo != null) {
			mongo.close();
		}
	}
}
