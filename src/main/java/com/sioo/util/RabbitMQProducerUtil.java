package com.sioo.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.springframework.amqp.AmqpIllegalStateException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;

import com.alibaba.fastjson.JSON;
import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.sioo.cache.ConfigCache;
import com.sioo.log.LogInfo;

public class RabbitMQProducerUtil {

	private static Logger log = Logger.getLogger(RabbitMQProducerUtil.class);
	// 连接配置
	private ConnectionFactory factory = null;
	private Connection connection = null;
	private Channel channel = null;
	private DeclareOk declareOk;
	public static RabbitMQProducerUtil producerUtil;
	private volatile MessageConverter messageConverter = new SimpleMessageConverter();

	// config
	private final static int DEFAULT_PORT = 5672;
	private final static String DEFAULT_USERNAME = "sioo";
	private final static String DEFAULT_PASSWORD = "sioo58657686";
	// private final static int DEFAULT_PROCESS_THREAD_NUM =
	// Runtime.getRuntime().availableProcessors() * 2;
	// private static final int PREFETCH_SIZE = 1;
	private final static String DEFAULT_HOST = "127.0.0.1";

	public static RabbitMQProducerUtil getProducerInstance() {
		try {
			producerUtil = new RabbitMQProducerUtil();
		} catch (Exception e) {
			log.error("初始化RabbitMQ失败，\r\n", e);
		}
		return producerUtil;
	}

	/**
	 * 发送
	 *
	 * @param queueName
	 *            队列名称
	 * @param obj
	 *            发送对象
	 * @param priority
	 *            优先级,最大10,最小为0,数值越大优先级越高
	 * @throws IOException
	 */
	public void send(String queueName, Object obj, int priority) {
		try {
			BasicProperties.Builder properties = new BasicProperties.Builder();
			properties.priority(priority);
			channel.basicPublish("", queueName, properties.build(), serialize(obj));
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("放入RabbitMQ消息队列异常",
					"[RabbitMQProducerUtil.send(" + queueName + "," + JSON.toJSONString(obj) + "," + priority + ") Exception]" + LogInfo.getTrace(e));
		}
	}

	public void send(String queueName, Object obj) throws IOException {
		try {
			channel.basicPublish("", queueName, com.rabbitmq.client.MessageProperties.PERSISTENT_TEXT_PLAIN, serialize(obj));
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("放入RabbitMQ消息队列异常", "[RabbitMQProducerUtil.send(" + queueName + "," + JSON.toJSONString(obj) + ") Exception]" + LogInfo.getTrace(e));
		}
	}

	/**
	 * 初始化
	 *
	 * @throws IOException
	 * @throws TimeoutException
	 */
	private RabbitMQProducerUtil() throws IOException, TimeoutException {
		try {

			ConfigCache configCache = ConfigCache.getInstance();
			if(configCache.getServerType()==1){
				factory = new ConnectionFactory();
				factory.setHost(configCache.getRabbitHost1() == null ? DEFAULT_HOST : configCache.getRabbitHost1());
				factory.setPort(configCache.getRabbitPort1() <= 0 ? DEFAULT_PORT : configCache.getRabbitPort1());
				factory.setUsername(configCache.getRabbitUserName1() == null ? DEFAULT_USERNAME : configCache.getRabbitUserName1());
				factory.setPassword(configCache.getRabbitPassword1() == null ? DEFAULT_PASSWORD : configCache.getRabbitPassword1());
				factory.setAutomaticRecoveryEnabled(true);
				factory.setRequestedHeartbeat(5);// 心跳时间s
				factory.setNetworkRecoveryInterval(6000);// 网络重连失败重试间隔时间ms
				connection = factory.newConnection();
				channel = connection.createChannel();
			}else if(configCache.getServerType()==0){
				factory = new ConnectionFactory();
				factory.setHost(configCache.getRabbitHost0() == null ? DEFAULT_HOST : configCache.getRabbitHost0());
				factory.setPort(configCache.getRabbitPort0() <= 0 ? DEFAULT_PORT : configCache.getRabbitPort0());
				factory.setUsername(configCache.getRabbitUserName0() == null ? DEFAULT_USERNAME : configCache.getRabbitUserName0());
				factory.setPassword(configCache.getRabbitPassword0() == null ? DEFAULT_PASSWORD : configCache.getRabbitPassword0());
				factory.setAutomaticRecoveryEnabled(true);
				factory.setRequestedHeartbeat(5);// 心跳时间s
				factory.setNetworkRecoveryInterval(6000);// 网络重连失败重试间隔时间ms
				connection = factory.newConnection();
				channel = connection.createChannel();
			}
			/*// 初始化
			factory = new ConnectionFactory();
			factory.setHost(configCache.getRabbitHost() == null ? DEFAULT_HOST : configCache.getRabbitHost());
			factory.setPort(configCache.getRabbitPort() <= 0 ? DEFAULT_PORT : configCache.getRabbitPort());
			factory.setUsername(configCache.getRabbitUserName() == null ? DEFAULT_USERNAME : configCache.getRabbitUserName());
			factory.setPassword(configCache.getRabbitPassword() == null ? DEFAULT_PASSWORD : configCache.getRabbitPassword());
			factory.setAutomaticRecoveryEnabled(true);
			factory.setRequestedHeartbeat(5);// 心跳时间s
			factory.setNetworkRecoveryInterval(6000);// 网络重连失败重试间隔时间ms
			connection = factory.newConnection();
			channel = connection.createChannel();*/
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("初始化RabbitMQ参数异常", "[RabbitMQProducerUtil() Exception]" + LogInfo.getTrace(e));
		}
	}

	/**
	 * 单个获取
	 *
	 * @param queueName
	 *            队列名称
	 * @param exchangeName
	 *            交换器名称
	 * @return
	 */
	public Object getObj(String queueName) throws Exception {
		try {
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("x-max-priority", 10);
			channel.queueDeclare(queueName, true, false, false, args);
			// 流量控制
			channel.basicQos(1);
			return receive(channel, queueName);
		} catch (IOException e) {
			LogInfo.getLog().errorAlert("从RabbitMQ获取单个消息失败，IO异常。", "[RabbitMQProducerUtil.getObj(" + queueName + ") IOException]" + LogInfo.getTrace(e));
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("从RabbitMQ获取单个消息异常。", "[RabbitMQProducerUtil.getObj(" + queueName + ") Exception]" + LogInfo.getTrace(e));
		}
		return null;
	}

	/**
	 * 批量获取
	 *
	 * @param queueName
	 *            队列名称
	 * @param exchangeName
	 *            交换器名称
	 * @return
	 */
	public List<Object> getObjList(String queueName, int limit) {
		List<Object> list = new ArrayList<Object>();
		try {
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("x-max-priority", 10);
			channel.queueDeclare(queueName, true, false, false, args);
			// 流量控制
			channel.basicQos(1);
			// 声明消费者
			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(queueName, false, consumer);
			for (int i = 0; i < limit; i++) {
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				Object obj = deSerialize(delivery.getBody());
				if (obj != null) {
					list.add(obj);
					channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false); // 反馈给服务器表示收到信息
				}
			}
			return list;
		} catch (IOException e) {
			LogInfo.getLog().errorAlert("从RabbitMQ获取多个消息失败，IO异常。", "[RabbitMQProducerUtil.getObjList(" + queueName + "," + limit + ") IOException]" + LogInfo.getTrace(e));
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("从RabbitMQ获取多个消息异常。", "[RabbitMQProducerUtil.getObjList(" + queueName + "," + limit + ") Exception]" + LogInfo.getTrace(e));
		}
		return null;
	}

	public List<Object> getObjListNoPriority(String queueName, int limit) {
		List<Object> list = new ArrayList<Object>();
		try {

			channel.queueDeclare(queueName, true, false, false, null);
			// 流量控制
			channel.basicQos(1);
			// 声明消费者
			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(queueName, false, consumer);
			for (int i = 0; i < limit; i++) {
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				Object obj = deSerialize(delivery.getBody());
				if (obj != null) {
					list.add(obj);
					channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false); // 反馈给服务器表示收到信息
				}
			}
			return list;
		} catch (IOException e) {
			LogInfo.getLog().errorAlert("从RabbitMQ获取多个带优先级消息失败，IO异常。",
					"[RabbitMQProducerUtil.getObjListNoPriority(" + queueName + "," + limit + ") IOException]" + LogInfo.getTrace(e));
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("从RabbitMQ获取多个带优先级消息异常。", "[RabbitMQProducerUtil.getObjListNoPriority(" + queueName + "," + limit + ") Exception]" + LogInfo.getTrace(e));
		}
		return null;
	}

	/**
	 * 获取队列长度
	 *
	 * @param queueName
	 *            队列名称
	 * @return
	 */
	public int getQueueListSize(String queueName) {
		int count = 0;
		try {
			declareOk = channel.queueDeclare(queueName, false, false, false, null);
			declareOk.getMessageCount();
		} catch (IOException e) {
			LogInfo.getLog().errorAlert("获取RabbitMQ消息队列大小异常。", "[RabbitMQProducerUtil.getQueueListSize(" + queueName + ") Exception]" + LogInfo.getTrace(e));
		}
		return count;
	}

	private Object receive(Channel channel, String queueName) throws Exception {
		try {
			// 声明消费者
			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(queueName, false, consumer);
			while (true) {
				// 等待队列推送消息
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				Object obj = deSerialize(delivery.getBody());
				if (obj != null) {
					channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false); // 反馈给服务器表示收到信息
				}
				return obj;
			}
		} catch (Exception e) {
			LogInfo.getLog().errorAlert("回复RabbitMQ消息队列消息异常。",
					"[RabbitMQProducerUtil.receive(" + JSON.toJSONString(channel) + "," + queueName + ") Exception]" + LogInfo.getTrace(e));
		}
		return null;
	}

	public void close() {
		try {
			channel.close();
			connection.close();
		} catch (IOException e) {
			LogInfo.getLog().errorAlert("关闭RabbitMQ连接失败，IO异常。", "[RabbitMQProducerUtil.close() IOException]" + LogInfo.getTrace(e));
		} catch (TimeoutException e) {
			LogInfo.getLog().errorAlert("关闭RabbitMQ连接失败，Timeout异常。", "[RabbitMQProducerUtil.close() Exception]" + LogInfo.getTrace(e));
		}
	}

	public byte[] serialize(Object obj) throws IOException {
		ByteArrayOutputStream baos = null;
		HessianOutput output = null;
		try {
			baos = new ByteArrayOutputStream(1024);
			output = new HessianOutput(baos);
			output.startCall();
			output.writeObject(obj);
			output.completeCall();
		} catch (final IOException ex) {
			throw ex;
		} finally {
			if (output != null) {
				try {
					baos.close();
				} catch (final IOException ex) {
					LogInfo.getLog().errorAlert("序列化RabbitMQ消息异常。", "[RabbitMQProducerUtil.serialize() ByteArrayOutputStream.colse() IOException]" + LogInfo.getTrace(ex));
				}
			}
		}
		return baos != null ? baos.toByteArray() : null;
	}

	public Object deSerialize(byte[] in) {
		Object obj = null;
		ByteArrayInputStream bais = null;
		HessianInput input = null;
		try {
			bais = new ByteArrayInputStream(in);
			input = new HessianInput(bais);
			input.startReply();
			obj = input.readObject();
			input.completeReply();
		} catch (final IOException ex) {
			LogInfo.getLog().errorAlert("反序列化RabbitMQ消息失败，IO异常。", "[RabbitMQProducerUtil.deSerialize() IOException]" + LogInfo.getTrace(ex));
		} catch (final Throwable ex) {
			LogInfo.getLog().errorAlert("反序列化RabbitMQ消息异常。", "[RabbitMQProducerUtil.deSerialize() Throwable]" + LogInfo.getTrace(ex));
		} finally {
			if (input != null) {
				try {
					bais.close();
				} catch (final IOException ex) {
					log.error("[RabbitMQ] deSerialize ByteArrayInputStream close failed, IOException: " + ex.toString());
				}
			}
		}
		return obj;
	}

	protected Message convertMessageIfNecessary(final Object object) {
		if (object instanceof Message) {
			return (Message) object;
		}
		return getRequiredMessageConverter().toMessage(object, new MessageProperties());
	}

	private MessageConverter getRequiredMessageConverter() throws IllegalStateException {
		MessageConverter converter = this.getMessageConverter();
		if (converter == null) {
			throw new AmqpIllegalStateException("No 'messageConverter' specified. Check configuration of RabbitTemplate.");
		}
		return converter;
	}

	public MessageConverter getMessageConverter() {
		return messageConverter;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

}
