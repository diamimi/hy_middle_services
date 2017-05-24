package com.sioo.sms.handle.sms.test;

import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.sioo.cache.ChannelCache;
import com.sioo.cache.UserCache;
import com.sioo.cache.UserSignCache;
import com.sioo.hy.cmpp.vo.SendingVo;
import com.sioo.sms.handle.channel.ChannelHandle;
import com.sioo.util.SmsCache;

public class ServiceTest {
	public static void main(String[] args) {
		SmsCache.mongoDbName = "SMS";
		SmsCache.mongoHost = "101.227.68.21";
		SmsCache.mongoPort = 27017;
		ChannelCache.getInstance().loadChannel();
		UserCache.getInstance().loadUser();
		// testChannel1();
		testOther();
	}

	private static void testOther() {
		UserSignCache.getInstance().loadUserSign(0, null);
		Map<String, Object> user_map = UserCache.getInstance().getUser(7978);
		String str = "{\"arrive_fail\":0,\"autoFlag\":0,\"channel\":38,\"content\":\"【国家会展中心(上海)11】欢迎您，您的上网验证码为：362283。\",\"contentNum\":0,\"expid\":\"806481001\",\"fail\":0,\"grade\":0,\"handStat\":0,\"hisids\":0,\"id\":0,\"mobile\":13764702798,\"mtype\":1,\"pid\":12090893,\"senddate\":20161208130625,\"source\":\"CMPP1\",\"stat\":0,\"succ\":0,\"uid\":7978}";
		SendingVo vo = JSON.parseObject(str, SendingVo.class);
		// 非移动，签名库中不存在------ 校验通过，设置拓展为UID+提交拓展
		// vo.setMtype(3);
		// 通道不需要报备，签名库中存在------ 校验通过，设置拓展为提交拓展
		// vo.setExpid("0017339");
		// vo.setContent("【希奥】" + vo.getContent().substring(str.indexOf("】")));
		// 通道不需要报备，签名库中存在,且提交拓展为签名对应拓展开头------ 校验通过，设置拓展为提交拓展
		// vo.setExpid("0017339001");
		// 通道需要报备，签名库中存在且未报备------ 校验失败
		// vo.setChannel(43);
		// 通道需要报备，签名库中存在且已报备------ 校验通过，设置拓展为提交拓展
		// 联通电信，渠道用户，签名库中不存在------ 生成自定义拓展 UID+5位自增
		// 联通电信，渠道用户，签名存在,拓展不存在------ 直接生成用户提交拓展
		// vo.setContent("【希奥】" + vo.getContent().substring(str.indexOf("】")));
		// vo.setExpid("1234");
		// 联通电信，渠道用户，签名库中不存在，添加失败------ 循环五次后记录日志并不执行
		// 联通电信，普通用户，签名库中不存在------ 生成系统自增长ID拓展
		// vo.setContent("【fsdatest】" +
		// vo.getContent().substring(str.indexOf("】")));
		// user_map.put("usertype", 2);
//		ChannelHandle.getInstance().matchSignAndExpend(vo, new String[] { vo.getContent().substring(vo.getContent().indexOf("【"), vo.getContent().indexOf("】") + 1) }, user_map);

	}

	private static void testChannel1() {
		UserSignCache.getInstance().loadUserSign(30032, null);
		Map<String, Object> user_map = UserCache.getInstance().getUser(30032);
		String str = "{\"arrive_fail\":0,\"autoFlag\":0,\"channel\":1,\"content\":\"【欧阳2】您已提交借款申请。如3个工作日内没有结果，请试试贷嘛新品吧！！http://t.cn/Rt2XDhF回TD退订\",\"contentNum\":1,\"expid\":\"\",\"fail\":0,\"grade\":0,\"handStat\":0,\"hisids\":0,\"id\":10881743,\"location\":\"重庆\",\"mdstr\":\"92B3471D48AF0B0E\",\"mobile\":13883868729,\"mtype\":1,\"pid\":10883211,\"release\":1,\"senddate\":20161206134637,\"source\":\"CMPP1\",\"stat\":0,\"succ\":0,\"uid\":30032}";
		SendingVo vo = JSON.parseObject(str, SendingVo.class);
		// 通道1，拓展类型自定义拓展或强制拓展，拓展为空，签名不存在------ 校验失败
		// 通道1，拓展类型自定义拓展或强制拓展，拓展不为空且不存在，签名不存在 ------校验失败
		// vo.setExpid("1234");
		// 通道1，拓展类型自定义拓展或强制拓展，拓展不为空且存在，签名不存在 ------校验失败
		// vo.setExpid("0036161");
		// 通道1，拓展类型自定义拓展，拓展为空，签名存在且未报备 ------校验失败
		// vo.setContent("【希奥】" + vo.getContent().substring(str.indexOf("】")));
		// 通道1，拓展类型强制拓展，拓展为空，签名存在且未报备------校验失败
		// 通道1，拓展类型自定义拓展，拓展为空，签名存在且已报备 ------校验失败
		// 通道1，拓展类型强制拓展，拓展为空，签名存在且已报备------校验通过，修改拓展为缓存中的拓展
		// 通道1，拓展类型强制拓展，拓展不为空，签名存在且已报备------校验通过，修改拓展为缓存中的拓展
		// 通道1，拓展类型自定义拓展，拓展为不为空且与签名对应，签名存在且未报备 ------校验失败
		// 通道1，拓展类型自定义拓展，拓展为不为空且与签名对应，签名存在且已报备 ------校验通过
		// vo.setExpid("0017339");
		// 通道1，拓展类型自定义拓展，拓展为不为对应拓展开头的，签名存在且已报备 ------校验通过
		// vo.setExpid("0017339001");
		// 拓展类型:强制拓展
		// user_map.put("expidSign", 2);
//		ChannelHandle.getInstance().matchSignAndExpend(vo, new String[] { vo.getContent().substring(vo.getContent().indexOf("【"), vo.getContent().indexOf("】") + 1) }, user_map);
	}
}
