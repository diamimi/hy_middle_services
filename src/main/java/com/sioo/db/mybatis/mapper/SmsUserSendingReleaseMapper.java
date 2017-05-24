package com.sioo.db.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.sioo.hy.cmpp.vo.SendingVo;

public interface SmsUserSendingReleaseMapper {
	/***
	 * 批量保存审核消息
	 * 
	 * @param vo
	 */
	public void batchSave(@Param("list") List<SendingVo> vo);

	/***
	 * 获取已审核消息列表
	 * 
	 * @return
	 */
	public List<SendingVo> getSmsUserSendingRelease();

	/***
	 * 删除审核消息
	 * 
	 * @param ids
	 * @return
	 */
	public int deleteByIds(@Param("list") List<Long> ids);
}
