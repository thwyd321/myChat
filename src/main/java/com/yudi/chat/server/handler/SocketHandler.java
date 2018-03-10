package com.yudi.chat.server.handler;

import com.yudi.chat.process.IMProcessor;
import com.yudi.chat.protocol.IMMessage;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class SocketHandler extends SimpleChannelInboundHandler<IMMessage> {
	private IMProcessor processor = new IMProcessor();
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, IMMessage msg) throws Exception {
		processor.process(ctx.channel(), msg);
		
	}

	
}
