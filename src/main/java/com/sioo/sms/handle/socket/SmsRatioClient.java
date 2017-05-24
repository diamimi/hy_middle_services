package com.sioo.sms.handle.socket;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author Administrator  331737188@qq.com
 * @date : 2017年1月17日 下午3:54:27
 *
 */
public class SmsRatioClient {
	private final Logger logger = LoggerFactory.getLogger((getClass()).getSimpleName());
	private String hostname = "127.0.0.1";
	private int port = 20094;
	private IoConnector connector = null;
	
	public SmsRatioClient(String hostname, int port) {
		super();
		this.hostname = hostname;
		this.port = port;
	}

	private IoConnector getIoConnector(){
		if(null == this.connector){
			IoConnector connector = new NioSocketConnector();
			connector.getSessionConfig().setReadBufferSize(4096);
			connector.getSessionConfig().setWriteTimeout(2000);  
			connector.getSessionConfig().setWriterIdleTime(10000);
			
			connector.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 3);
	        
	        TextLineCodecFactory lineFactory = new TextLineCodecFactory(Charset.forName("UTF-8"));  
	        lineFactory.setDecoderMaxLineLength(Integer.MAX_VALUE);  
	        lineFactory.setEncoderMaxLineLength(Integer.MAX_VALUE);
	        
	        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(lineFactory));
	        connector.setHandler(new SmsRatioClientHandler());
	        this.connector = connector;
		}
        return this.connector;
	}
	
	private ConnectFuture getConnectFuture(){
		return getIoConnector().connect(new InetSocketAddress(hostname, port));
	}
	
	public void submit(String message){
		IoSession session;
		try {
			ConnectFuture future = getConnectFuture();
			future.awaitUninterruptibly();
			session = future.getSession();
			
			session.write(message);
			session.getCloseFuture().awaitUninterruptibly();
			
		} catch (Exception e) {
			logger.info("[socket]:",e);
		}
	}
	
	public void close(){
		if(null != connector){
			connector.dispose(true);
		}
	}
}