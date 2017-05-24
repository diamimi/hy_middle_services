package com.sioo.db.mybatis.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.sioo.hy.cmpp.vo.ConsumeVo;
import com.sioo.hy.cmpp.vo.SmsUserVo;

public interface SmsUserMapper {

	/***
	 * 批量修改用户信息
	 * 
	 * @param list
	 * @return
	 */
	public int updateSmsUser(@Param("list") List<SmsUserVo> list);

	/***
	 * 保存用户消费信息
	 * 
	 * @param list
	 * @return
	 */
	public int saveSmsUserConsume(@Param("list") List<ConsumeVo> list);

	/***
	 * 修改用户消费信息
	 * 
	 * @param list
	 * @param date
	 * @return
	 */
	public int updateSmsUserConsume(@Param("list") List<ConsumeVo> list, @Param("date") Long date);

	
	/***
	 * 修改用户未扣费消费信息
	 * 
	 * @param list
	 * @param date
	 * @return
	 */
	public int updateSmsUserConsumeUnKou(@Param("list") List<ConsumeVo> list, @Param("date") Long date);
	
	/***
	 * 获取用户级别信息
	 * 
	 * @param id
	 * @return
	 */
	public Map<String, Object> findSmsUserById(@Param("id") Integer id);

	/***
	 * 获取用户控制信息
	 * 
	 * @param uid
	 * @return
	 */
	public Map<String, Object> findSmsUserControlById(@Param("uid") Integer uid);

	/***
	 * 获取用户审核条数
	 * 
	 * @param uid
	 * @return
	 */
	public int findSmsUserReleaseNum(@Param("uid") Integer uid);

	/***
	 * 保存用户基本信息，同步用户使用
	 * 
	 * @param map
	 * @return
	 */
	public int saveUser(Map<String, Object> map);

	/***
	 * 修改用户基本信息，同步用户使用
	 * 
	 * @param map
	 * @return
	 */
	public int updateUser(Map<String, Object> map);

	/***
	 * 保存用户控制信息，同步用户使用
	 * 
	 * @param map
	 * @return
	 */
	public int saveUserControl(Map<String, Object> map);

	/***
	 * 修改用户控制信息，同步用户使用
	 * 
	 * @param map
	 * @return
	 */
	public int updateUserControl(Map<String, Object> map);
}
