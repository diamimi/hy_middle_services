package com.sioo.sms.handle.sms.test;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.junit.Test;

import com.sioo.cache.MobileAreaCache;
import com.sioo.cache.UserCache;
import com.sioo.hy.cmpp.vo.SendingVo;
import com.sioo.util.MyUtils;
import com.sioo.util.SmsCache;

public class SmsHandleTest {

	@Test
	public void testCheckSms() throws Exception {
		// new TopicsMain().initSystemCache();

		SendingVo vo = new SendingVo();
		vo.setUid(83535000);
		vo.setMobile(17769565845L);
		vo.setContent("亲爱的华视会员，您好：您在我公司购买的隐形眼镜即将到使用期限，建议您抽空来我公司进行复查。回T退订【华视眼镜】");
		vo = updateProperty(vo);
		Map<String, Object> user_map = UserCache.getInstance().getUser(vo.getUid());

		// vo.setMobile(18621008899L); // 白名单
		// vo.setMobile(1869865985L);
		// SmsHandle.getInstance().checkSms(vo, user_map);

		// vo.setMobile(15869565845L); // 黑名单
		// vo.setMobile(15235698653L);
		// SmsHandle.getInstance().checkSms(vo, user_map);

		// vo.setMobile(15169565845L);
		// vo.setContent("亲爱的华视会员，您好：您在我公司购买的隐形眼镜即将到使用期限，建议您抽空来我公司进行复查。回T退订【深圳嘉盈】");//白签名
		// vo.setContent("亲爱的华视会员，您好：您在我公司购买的隐形眼镜即将到使用期限，建议您抽空来我公司进行复查。回T退订【我】");
		// SmsHandle.getInstance().checkSms(vo, user_map);

		// vo.setMobile(15169565845L);
		// vo.setContent("亲爱的华视会员，您好：您在我公司购买的隐形眼镜即将到使用期限，建议您抽空来我公司进行复查。回T退订回电400-88985【华视眼镜】");//屏蔽词
		// SmsHandle.getInstance().checkSms(vo, user_map);

	}

	private SendingVo updateProperty(SendingVo vo) throws UnsupportedEncodingException {
		String location = "全国,-1";
		Map<String, Object> map = MobileAreaCache.getInstance().getMobileArea(vo.getMobile());
		if (map != null && !map.isEmpty()) {
			location = map.get("province").toString() + "," + map.get("provincecode");
		}
		vo.setLocation(location);
		vo.setContent(vo.getContent().trim().replace("\\r\\n", "\\n").replace("'", "‘").replace("\\", "/"));
		// 重新计算条数
		int contentLength = vo.getContent().length();
		int cCount = contentLength > 70 ? 67 : 70;
		if (contentLength % cCount != 0) {
			cCount = (contentLength / cCount) + 1;
		} else {
			cCount = contentLength / cCount;
		}
		vo.setContentNum(cCount);

		String mdStr = MyUtils.getMD5Str(vo.getUid(), vo.getChannel(), vo.getMtype(), vo.getContent(), 16);
		vo.setMdstr(mdStr);
		vo.setId(SmsCache.getHisID());
		vo.setRelease(null);
		return vo;
	}
}
