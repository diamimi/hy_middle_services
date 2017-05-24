package com.sioo.sms.handle.socket;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Administrator  331737188@qq.com
 * @date : 2017年1月17日 下午3:54:17
 *
 */
public class SmsRatioClientHandler extends IoHandlerAdapter {
	private final Logger logger = LoggerFactory.getLogger((getClass()).getSimpleName());

	@Override
	public void sessionOpened(IoSession session) {

	}

	@Override
	public void messageReceived(IoSession session, Object message) {
		session.setAttribute("result", message.toString());
		session.close(true);
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) {
		logger.error(cause.getMessage());
		session.close(true);
	}
}