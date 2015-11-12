package com.allinpay.framework.socket.mina;

import java.net.InetSocketAddress;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinaSocketClient {

	static Logger logger = LoggerFactory.getLogger(MinaSocketClient.class);

	/**
	 * 远程服务端地址
	 */
	private String remoteServerHost;

	/**
	 * 远程服务端端口
	 */
	private int remoteServerPort;

	/**
	 * 心跳消息
	 */
	private String heartbeatMessage = "0000";

	/**
	 * 心跳时间间隔，单位：秒
	 */
	private int hearbeatInterval = 60;

	/**
	 * 重连时间间隔，单位：毫秒
	 */
	private long reConnnectInterval = 3 * 1000L;

	private IoHandler ioHandler;

	NioSocketConnector connector;

	public MinaSocketClient(String remoteServerHost, int remoteServerPort) {
		this.remoteServerHost = remoteServerHost;
		this.remoteServerPort = remoteServerPort;
	}

	public void init() {

		// Create TCP/IP connector.
		connector = new NioSocketConnector();
		// 创建接收数据的过滤器
		DefaultIoFilterChainBuilder chain = connector.getFilterChain();
		//自动重连
		chain.addLast("reConnect", new IoFilterAdapter() {
			public void sessionClosed(NextFilter nextFilter, IoSession session) throws Exception {
				logger.error("连接被关闭：" + getServerInfo());
				scheduleConnect();
			}
		});
		// 设定这个过滤器将一行一行(/r/n)的读取数据
		chain.addLast("protocolCodecFilter", new ProtocolCodecFilter(new TextLineCodecFactory()));
		// 设定服务器端的消息处理器:一个MinaClientHandler对象,
		connector.setHandler(ioHandler);
		// Set connect timeout.
		connector.setConnectTimeoutMillis(30 * 1000);

		doConnnect();
	}

	public void doConnnect() {
		// 连结到服务器:
		ConnectFuture cf = connector.connect(new InetSocketAddress(remoteServerHost, remoteServerPort));
		cf.addListener(new IoFutureListener<ConnectFuture>() {
			public void operationComplete(ConnectFuture future) {
				if (future.isConnected()) {
					logger.info("连接成功：" + getServerInfo());
				} else {
					logger.error("连接失败：" + "，原因：" + future.getException());
					// 启动连接失败时定时重连
					scheduleConnect();
				}
			}

		});
	}

	private void scheduleConnect() {
		try {
			Thread.sleep(reConnnectInterval);
		} catch (InterruptedException e) {
		}
		doConnnect();
	}

	public void close() {
		connector.dispose();
	}

	private String getServerInfo() {
		return String.format("%s:%d", remoteServerHost, remoteServerPort);
	}

	public IoHandler getIoHandler() {
		return ioHandler;
	}

	public void setIoHandler(IoHandler ioHandler) {
		this.ioHandler = ioHandler;
	}

}
