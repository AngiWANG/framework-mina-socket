package com.allinpay.framework.socket.mina;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinaClientHandler extends IoHandlerAdapter {

	Logger logger = LoggerFactory.getLogger(MinaClientHandler.class);

	// 当一个连接建立时
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		session.write("client says：我来啦........");
		logger.debug("connected:" + session.getRemoteAddress());
	}

	// 当服务端发送的消息到达时:
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		// 我们己设定了服务器解析消息的规则是一行一行读取,这里就可转为String:
		String s = (String) message;
		logger.debug(s);
		// 测试将消息回送给客户端
		session.write(s);
	}
}