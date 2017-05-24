package com.sioo.db.mybatis.mapper;

import com.sioo.service.model.UserSign;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public interface SmsUserSignMapper {
	/***
	 * 获取用户签名列表
	 * 
	 * @param uid
	 * @param store
	 * @param type
	 * @return
	 */
	public CopyOnWriteArrayList<UserSign> findUserSignByUidAndStore(@Param("uid") Integer uid, @Param("store") String store, @Param("type") Integer type);

	/***
	 * 获取普通用户自增长最大值
	 * 
	 * @return
	 */
	public String findMaxExpend2();

	/***
	 * 根据拓展集合获取签名列表
	 * 
	 * @param expends
	 * @return
	 */
	public List<UserSign> findUserSignByExpends(@Param("expends") String expends);

	/***
	 * 保存用户签名
	 * 
	 * @param userSign
	 * @return
	 */
	public int insertUserSign(UserSign userSign);
	
	/***
	 * 修改用户签名
	 * 
	 * @param userSign
	 * @return
	 */
	public int updateUserSign(UserSign userSign);
}
