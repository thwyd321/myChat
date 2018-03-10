package com.yudi.catalina.netty.server;

import com.yudi.catalina.http.MyRequest;
import com.yudi.catalina.http.MyResponse;
import com.yudi.catalina.servlets.MyOwnServlet;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;

public class MYTomcatHandler extends ChannelInboundHandlerAdapter {
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof HttpRequest) {
			HttpRequest r = (HttpRequest) msg;
			MyRequest request = new MyRequest(ctx,r);
			MyResponse response = new MyResponse(ctx,r);
			new MyOwnServlet().doGet(request, response);
		}
	
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
	}

}
