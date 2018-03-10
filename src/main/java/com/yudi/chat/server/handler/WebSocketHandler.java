package com.yudi.chat.server.handler;

import com.yudi.chat.process.IMProcessor;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
	private IMProcessor process = new IMProcessor();

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
		System.out.println(msg.text());
		process.process(ctx.channel(), msg.text());

	}
	//如果直接关掉网页 也移除
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		process.logout(ctx.channel());
	}
	
	
	
}
