package com.sioo.db.mybatis.mapper;

import com.sioo.hy.cmpp.vo.SendingVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SmsUserSendingMapper {
	/***
	 * 批量保存队列消息
	 * 
	 * @param vo
	 */
	public void batchSave(@Param("list") List<SendingVo> vo);

    List<SendingVo> getSmsUserSending();

	void deleteByIds(List<Long> ids);
}
