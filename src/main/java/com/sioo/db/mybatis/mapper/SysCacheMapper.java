package com.sioo.db.mybatis.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

/***
 * 缓存信息Mapper接口
 * 
 * @author OYJM
 * @date 2016年9月18日
 *
 */
public interface SysCacheMapper {
	/***
	 * 查询用户黑名单
	 * 
	 * @param uid
	 * @return
	 */
	public List<Map<String, Object>> findUserBlackMobile(@Param("uid") Integer uid);

	/***
	 * 查询用户全量黑名单
	 * @return
	 */
	public List<Map<String, Object>> findAllUserBlackMobile();
	
	/***
	 * 查询用户白名单
	 * 
	 * @param uid
	 * @return
	 */
	public List<Map<String, Object>> findUserWhiteMobile(@Param("uid") Integer uid);

	/***
	 * 查询用户屏蔽词
	 * 
	 * @param uid
	 * @return
	 */
	public List<Map<String, Object>> findSmsWordsUser(@Param("uid") Integer uid,@Param("type")Integer type);

	/***
	 * 查询通道屏蔽词
	 * 
	 * @param channel
	 * @return
	 */
	public List<Map<String, Object>> findSmsWordsChannel(@Param("channel") Integer channel);

	/***
	 * 根据用户ID查询用户信息
	 * 
	 * @param uid
	 * @return
	 */
	public Map<String, Object> findSmsUserByUid(@Param("uid") Integer uid);

	/***
	 * 查询用户ID列表
	 * 
	 * @param uid
	 * @param child
	 * @return
	 */
	public List<Integer> findUid(@Param("uid") Integer uid, @Param("child") Integer child);

	/***
	 * 根据用户ID查询用户控制信息
	 * 
	 * @param uid
	 * @return
	 */
	public Map<String, Object> findSmsUserControl(@Param("uid") Integer uid);


	/***
	 * 查询通道信息
	 * 
	 * @return
	 */
	public List<Map<String, Object>> findSmsChannel();

	/****
	 * 根据ID查询通道信息
	 * 
	 * @param channelId
	 * @return
	 */
	public Map<String, Object> findSmsChannelById(@Param("channelId") Integer channelId);

	/***
	 * 查询用户提醒
	 * 
	 * @param uid
	 * @return
	 */
	public List<Map<String, Object>> findSmsUserAlert(@Param("uid") Integer uid);


	/***
	 * 查询用户通道黑签名
	 * 
	 * @param uid
	 * @return
	 */
	public List<Map<String, Object>> findSmsSignChannelBlack(@Param("uid") Integer uid, @Param("store") String store);

	/***
	 * 查询用户白签名
	 * 
	 * @param uid
	 * @return
	 */
	public List<Map<String, Object>> findUserWhiteSign(@Param("uid") Integer uid);

	/***
	 * 查询用户发送
	 * 
	 * @param uid
	 * @return
	 */
	public List<Map<String, Object>> findSmsUserSendModel(@Param("uid") Integer uid);

	/***
	 * 查询用户路由
	 * 
	 * @param uid
	 * @return
	 */
	public List<Map<String, Object>> findSmsUserRoute(@Param("uid") Integer uid);

	/***
	 * 查询用户设置的策略组
	 * 
	 * @param uid
	 * @return
	 */
	public List<Map<String, Object>> findSmsUserStrategyRelation(@Param("uid") Integer uid);

	/***
	 * 查询策略组
	 * 
	 * @param uid
	 * @return
	 */
	public List<Map<String, Object>> findStrategyGourp(@Param("type") Integer type);

	/***
	 * 获取通道组列表
	 * 
	 * @param channel
	 * @return
	 */
	public List<Map<String, Object>> findChannelGourp(@Param("channel") Integer channel);

	/***
	 * 获取用户列表
	 * 
	 * @return
	 */
	public List<Map<String, Object>> findSmsUser();

	/****
	 * 获取归属地
	 * 
	 * @param provincecode
	 * @return
	 */
	public List<Map<String, Object>> findSmsMobileArea(@Param("provincecode") Integer provincecode,@Param("citycode") Integer citycode);

	/****
	 * 根据ID获取归属地
	 * 
	 * @param provincecode
	 * @return
	 */
	public Map<String, Object> findSmsMobileAreaById(@Param("id") Integer id);

	/***
	 * 查询用户屏蔽地区
	 * 
	 * @param uid
	 * @return
	 */
	public List<Map<String, Object>> findSmsBlackArea(@Param("uid") Integer uid);

	/***
	 * 查询通道屏蔽地区
	 * 
	 * @param uid
	 * @return
	 */
	public List<Map<String, Object>> findSmsBlackAreaByChannel(@Param("channelId") Integer channelId);

	/***
	 * 查询系统控制
	 * 
	 * @return
	 */
	public int findSysConfig();
	
	/***
	 * 查询扣量用户信息
	 * @param uid
	 * @return
	 */
	public List<Map<String, Object>> findSmsRptRatioConfig(@Param("uid") Integer uid);
	
	/***
	 * 查询审核模板
	 * 
	 * @param uid
	 * @return
	 */
	public List<Map<String, Object>> findReleaseTemplate(@Param("uid") Integer uid);

    List<Map<String,Object>> findSmsStrategyGroup();
}
