package com.sioo.sms;

import com.sioo.cache.ConfigCache;
import com.sioo.hy.cmpp.vo.SendingVo;
import com.sioo.util.DateUtils;
import com.sioo.util.MatchUtil;
import com.sioo.util.RabbitMQProducerUtil;
import com.sioo.util.SmsCache;
import org.junit.Test;

import java.io.IOException;

public class SendSmsTest {

	public static void main(String[] args) throws IOException {
		SmsCache.RABBIT_HOST = "119.15.137.35";
		SmsCache.RABBIT_PORT = 5672;
		SmsCache.RABBIT_USERNAME = "sioomq";
		SmsCache.RABBIT_PASSWORD = "sioo58657686";
		SmsCache.RABBIT_PREFETCH_SIZE = 1;
		ConfigCache.getInstance().initConfig();
		RabbitMQProducerUtil util = RabbitMQProducerUtil.getProducerInstance();
		SendingVo vo = new SendingVo();
		vo.setUid(30032);
		vo.setMobile(18621367763L);
		//(联通)&(推出)&(市话)&(详询)
		vo.setContent("【全优商城】qihesdfsdf钱)");
		vo.setSenddate(DateUtils.getTime());
		vo.setMtype(2);
		vo.setExpid("300328913");
		vo.setPid(101);
		util.send("SUBMIT_QUEUE_3", vo);

//		System.exit(0);
	}


	@Test
	public void test(){
		String content="【公务用车】尊敬的驾驶员,武汉市硚口区人民政府办公室订单447288170511003,修改派车,用车人:谭金魁(13343426749),使用车辆鄂A06890,用车时间(2017-05-11 09:40),用车时长（1半天),上车地点(南门平台),下车地点（宗关街),调度员:邱琦(15972959020)。请您及时出车!";
		String match = MatchUtil.getInstance().match(content, "(针对男科|性病|艾滋病|癌症|绝症|)|(医院)");
		System.out.println(match);
	}

}
