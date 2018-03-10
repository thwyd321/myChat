package com.yudi.catalina.http;

import java.util.List;
import java.util.Map;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

public class MyRequest {
	private ChannelHandlerContext ctx;
	private HttpRequest r;
	public MyRequest(ChannelHandlerContext ctx, HttpRequest r) {
		this.ctx = ctx;
		this.r = r;
	}
	public String getUri(){
		return r.getUri();
	}
	public String getMethod(){
		return r.getMethod().name();
	}
	public Map<String, List<String>> getParameters(){
		QueryStringDecoder decoder = new QueryStringDecoder(r.getUri());
		return decoder.parameters();
	}
	public String getParameters(String name){
		Map<String, List<String>> params = getParameters();
		List<String> param = params.get(name);
		if(null == param) {
			return null;
		}else{
			return param.get(0);
			
		}
	}
	
}
