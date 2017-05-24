package com.sioo.mybatis.dao.test;

import com.sioo.dao.SysCacheDao;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class SysCacheDaoTest {

	private int uid = 83535000;

	private int falseid = -1;

	@Test
	public void testFindUserBlackMobile() {
		List<Map<String, Object>> result = SysCacheDao.getInstance().findUserBlackMobile(null);
		Assert.assertTrue(!result.isEmpty());

		List<Map<String, Object>> result0 = SysCacheDao.getInstance().findUserBlackMobile(0);
		Assert.assertTrue(!result0.isEmpty());

		List<Map<String, Object>> result1 = SysCacheDao.getInstance().findUserBlackMobile(uid);
		Assert.assertTrue(!result1.isEmpty());

		List<Map<String, Object>> result2 = SysCacheDao.getInstance().findUserBlackMobile(falseid);
		Assert.assertTrue(result2.isEmpty());
	}

	@Test
	public void testFindUserWhiteMobile() {
		List<Map<String, Object>> result = SysCacheDao.getInstance().findUserWhiteMobile(null);
		Assert.assertTrue(!result.isEmpty());

		List<Map<String, Object>> result0 = SysCacheDao.getInstance().findUserWhiteMobile(0);
		Assert.assertTrue(!result0.isEmpty());

		List<Map<String, Object>> result1 = SysCacheDao.getInstance().findUserWhiteMobile(uid);
		Assert.assertTrue(!result1.isEmpty());

		List<Map<String, Object>> result2 = SysCacheDao.getInstance().findUserWhiteMobile(falseid);
		Assert.assertTrue(result2.isEmpty());
	}





	@Test
	public void testFindSmsUserByUid() {
		Map<String, Object> result = SysCacheDao.getInstance().findSmsUserByUid(uid);
		Assert.assertTrue(!result.isEmpty());

		Map<String, Object> result2 = SysCacheDao.getInstance().findSmsUserByUid(falseid);
		Assert.assertTrue(result2 == null);
	}

	@Test
	public void testFindUid() {
		List<Integer> result1 = SysCacheDao.getInstance().findUid(uid, 1);
		Assert.assertTrue(result1.isEmpty());

		List<Integer> result2 = SysCacheDao.getInstance().findUid(uid, 0);
		Assert.assertTrue(!result2.isEmpty());

		List<Integer> result3 = SysCacheDao.getInstance().findUid(falseid, 0);
		Assert.assertTrue(result3.isEmpty());
	}

	@Test
	public void testFindSmsUserControl() {
		Map<String, Object> result = SysCacheDao.getInstance().findSmsUserControl(null);
		Assert.assertTrue(result == null);

		Map<String, Object> result0 = SysCacheDao.getInstance().findSmsUserControl(0);
		Assert.assertTrue(result0 == null);

		Map<String, Object> result1 = SysCacheDao.getInstance().findSmsUserControl(uid);
		Assert.assertTrue(result1.get("releasenum") != null);

		Map<String, Object> result2 = SysCacheDao.getInstance().findSmsUserControl(falseid);
		Assert.assertTrue(result2 == null);
	}

	

	@Test
	public void testFindSmsChannel() {
		List<Map<String, Object>> result = SysCacheDao.getInstance().findSmsChannel();
		Assert.assertTrue(!result.isEmpty());
	}

	@Test
	public void testFindSmsUserAlert() {
		List<Map<String, Object>> result = SysCacheDao.getInstance().findSmsUserAlert(null);
		Assert.assertTrue(!result.isEmpty());

		List<Map<String, Object>> result0 = SysCacheDao.getInstance().findSmsUserAlert(0);
		Assert.assertTrue(!result0.isEmpty());

		List<Map<String, Object>> result1 = SysCacheDao.getInstance().findSmsUserAlert(uid);
		Assert.assertTrue(result1.isEmpty());

		List<Map<String, Object>> result2 = SysCacheDao.getInstance().findSmsUserAlert(falseid);
		Assert.assertTrue(result2.isEmpty());
	}


	@Test
	public void testFindSmsSignChannelBlack() {
		List<Map<String, Object>> result = SysCacheDao.getInstance().findSmsSignChannelBlack(null, null);
		Assert.assertTrue(!result.isEmpty());

		List<Map<String, Object>> result0 = SysCacheDao.getInstance().findSmsSignChannelBlack(0, null);
		Assert.assertTrue(!result0.isEmpty());

		List<Map<String, Object>> result1 = SysCacheDao.getInstance().findSmsSignChannelBlack(uid, null);
		Assert.assertTrue(result1.isEmpty());

		List<Map<String, Object>> result2 = SysCacheDao.getInstance().findSmsSignChannelBlack(falseid, null);
		Assert.assertTrue(result2.isEmpty());
	}

	@Test
	public void testFindUserWhiteSign() {
		List<Map<String, Object>> result = SysCacheDao.getInstance().findUserWhiteSign(null);
		Assert.assertTrue(!result.isEmpty());

		List<Map<String, Object>> result0 = SysCacheDao.getInstance().findUserWhiteSign(0);
		Assert.assertTrue(!result0.isEmpty());

		List<Map<String, Object>> result1 = SysCacheDao.getInstance().findUserWhiteSign(uid);
		Assert.assertTrue(!result1.isEmpty());

		List<Map<String, Object>> result2 = SysCacheDao.getInstance().findUserWhiteSign(falseid);
		Assert.assertTrue(result2.isEmpty());
	}

	@Test
	public void testFindSmsUserSendModel() {
		List<Map<String, Object>> result = SysCacheDao.getInstance().findSmsUserSendModel(null);
		Assert.assertTrue(!result.isEmpty());

		List<Map<String, Object>> result0 = SysCacheDao.getInstance().findSmsUserSendModel(0);
		Assert.assertTrue(!result0.isEmpty());

		List<Map<String, Object>> result1 = SysCacheDao.getInstance().findSmsUserSendModel(uid);
		Assert.assertTrue(result1.isEmpty());

		List<Map<String, Object>> result2 = SysCacheDao.getInstance().findSmsUserSendModel(falseid);
		Assert.assertTrue(result2.isEmpty());
	}

	@Test
	public void testFindSmsUserRoute() {
		List<Map<String, Object>> result = SysCacheDao.getInstance().findSmsUserRoute(null);
		Assert.assertTrue(!result.isEmpty());

		List<Map<String, Object>> result0 = SysCacheDao.getInstance().findSmsUserRoute(0);
		Assert.assertTrue(!result0.isEmpty());

		List<Map<String, Object>> result1 = SysCacheDao.getInstance().findSmsUserRoute(uid);
		Assert.assertTrue(!result1.isEmpty());

		List<Map<String, Object>> result2 = SysCacheDao.getInstance().findSmsUserRoute(falseid);
		Assert.assertTrue(result2.isEmpty());
	}

	@Test
	public void testFindSmsUserStrategyRelation() {
		List<Map<String, Object>> result = SysCacheDao.getInstance().findSmsUserStrategyRelation(null);
		Assert.assertTrue(!result.isEmpty());

		List<Map<String, Object>> result0 = SysCacheDao.getInstance().findSmsUserStrategyRelation(0);
		Assert.assertTrue(!result0.isEmpty());

		List<Map<String, Object>> result1 = SysCacheDao.getInstance().findSmsUserStrategyRelation(uid);
		Assert.assertTrue(!result1.isEmpty());

		List<Map<String, Object>> result2 = SysCacheDao.getInstance().findSmsUserStrategyRelation(falseid);
		Assert.assertTrue(result2.isEmpty());
	}

	@Test
	public void testFindStrategyGourp() {
		List<Map<String, Object>> result = SysCacheDao.getInstance().findStrategyGourp(null);
		Assert.assertTrue(result.get(0).get("1") != null);

		List<Map<String, Object>> result0 = SysCacheDao.getInstance().findStrategyGourp(0);
		Assert.assertTrue(result0.get(0).get("1") != null);

		List<Map<String, Object>> result1 = SysCacheDao.getInstance().findStrategyGourp(1);
		Assert.assertTrue(!result1.isEmpty());

		List<Map<String, Object>> result2 = SysCacheDao.getInstance().findStrategyGourp(2);
		Assert.assertTrue(!result2.isEmpty());

		List<Map<String, Object>> result3 = SysCacheDao.getInstance().findStrategyGourp(3);
		Assert.assertTrue(!result3.isEmpty());

		List<Map<String, Object>> result4 = SysCacheDao.getInstance().findStrategyGourp(4);
		Assert.assertTrue(!result4.isEmpty());

		List<Map<String, Object>> result5 = SysCacheDao.getInstance().findStrategyGourp(5);
		Assert.assertTrue(!result5.isEmpty());

		List<Map<String, Object>> result6 = SysCacheDao.getInstance().findStrategyGourp(6);
		Assert.assertTrue(!result6.isEmpty());
	}

	@Test
	public void testFindChannelGourp() {
		List<Map<String, Object>> result = SysCacheDao.getInstance().findChannelGourp(null);
		Assert.assertTrue(!result.isEmpty());
	}
}
