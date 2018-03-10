package com.yudi.catalina.http;


import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public class MyResponse {
	private ChannelHandlerContext ctx;
	private HttpRequest r;
	public MyResponse(ChannelHandlerContext ctx, HttpRequest r) {
		this.ctx = ctx;
		this.r = r;
	}
	public void write(String out) throws Exception{
		try{
			if(null == out){return;}
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, 
				HttpResponseStatus.OK,
				Unpooled.wrappedBuffer(out.getBytes("utf-8")));
				response.headers().set(HttpHeaders.Names.CONTENT_TYPE,"text/json");
				response.headers().set(HttpHeaders.Names.CONTENT_LENGTH,response.content().readableBytes());
				response.headers().set(HttpHeaders.Names.EXPIRES,0);
				if(HttpHeaders.isKeepAlive(r)){
					response.headers().set(HttpHeaders.Names.CONNECTION,Values.KEEP_ALIVE);
				}
				
				ctx.write(response);
		}finally{
			ctx.flush();
		}
		}

}
