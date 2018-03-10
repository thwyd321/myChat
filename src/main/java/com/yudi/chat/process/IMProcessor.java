package com.yudi.chat.process;

import com.alibaba.fastjson.JSONObject;
import com.yudi.chat.protocol.IMDecoder;
import com.yudi.chat.protocol.IMEncoder;
import com.yudi.chat.protocol.IMMessage;
import com.yudi.chat.protocol.IMP;
import com.yudi.chat.util.CommonUtil;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;

public class IMProcessor {
	//
	private final static ChannelGroup onlineUsers = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	//定义一些扩展属性
	private final AttributeKey<String> NICK_NAME = AttributeKey.valueOf("nickName");
	private final AttributeKey<String> IP_ADDR = AttributeKey.valueOf("ipAddr");
	private final AttributeKey<JSONObject> ATTRS = AttributeKey.valueOf("attrs");
	/**
	 * 获取用户昵称
	 * @param client
	 * @return
	 */
	public String getNickName(Channel client){
		return client.attr(NICK_NAME).get();
	}
	/**
	 * 获取用户远程IP地址
	 * @param client
	 * @return
	 */
	public String getAddress(Channel client){
		return client.remoteAddress().toString().replaceFirst("/","");
	}
	
	/**
	 * 获取扩展属性
	 * @param client
	 * @return
	 */
	public JSONObject getAttrs(Channel client){
		try{
			return client.attr(ATTRS).get();
		}catch(Exception e){
			return null;
		}
	}
	
	/**
	 * 获取扩展属性
	 * @param client
	 * @return
	 */
	private void setAttrs(Channel client,String key,Object value){
		try{
			JSONObject json = client.attr(ATTRS).get();
			json.put(key, value);
			client.attr(ATTRS).set(json);
		}catch(Exception e){
			JSONObject json = new JSONObject();
			json.put(key, value);
			client.attr(ATTRS).set(json);
		}
	}
	public void logout(Channel client){
		onlineUsers.remove(client);
	}
	
	private IMDecoder decoder = new IMDecoder();
	private IMEncoder encoder = new IMEncoder();
	//控制台与websocket转换
	public void process(Channel client, IMMessage msg){
		process(client, encoder.encode(msg));
	}
	public void process(Channel client, String msg) {
		IMMessage request = decoder.decode(msg);
		if(null == request){
			return;
		}
		//判断 如果是登陆动作，就往onlineUsers中加入一条信息
		if(IMP.LOGIN.getName().equals(request.getCmd())){
			onlineUsers.add(client);
			
			String nickname = request.getSender();
			client.attr(NICK_NAME).getAndSet(nickname);
			for (Channel channel : onlineUsers) {
				
				if(channel != client){
					request = new IMMessage(IMP.SYSTEM.getName(),CommonUtil.getTime(),onlineUsers.size(),nickname+"来到聊天室");
				}else{
					request = new IMMessage(IMP.SYSTEM.getName(),CommonUtil.getTime(),onlineUsers.size(),"您已与服务器建立连接");
				}
				String text = encoder.encode(request);
				//将消息发送到浏览器的websocket上
				channel.writeAndFlush(new TextWebSocketFrame(text));
				
			}
			
			
			
		}else if (IMP.LOGOUT.getName().equals(request.getCmd())){
			onlineUsers.remove(client);
		}else if(IMP.CHAT.getName().equals(request.getCmd())){
			for (Channel channel : onlineUsers) {
				if(channel !=client){
					request.setSender(getNickName(client));
				}else{	
					request.setSender("you");
				}
				String text = encoder.encode(request);
				channel.writeAndFlush(new TextWebSocketFrame(text));
				
			}
		}else if(request.getCmd().equals(IMP.FLOWER.getName())){
			JSONObject attrs = getAttrs(client);
			long currTime = CommonUtil.getTime();
			if(null != attrs){
				long lastTime = attrs.getLongValue("lastFlowerTime");
				//60秒之内不允许重复刷鲜花
				int secends = 10;
				long sub = currTime - lastTime;
				if(sub < 1000 * secends){
					request.setSender("you");
					request.setCmd(IMP.SYSTEM.getName());
					request.setContent("您送鲜花太频繁," + (secends - Math.round(sub / 1000)) + "秒后再试");
					String content = encoder.encode(request);
					client.writeAndFlush(new TextWebSocketFrame(content));
					return;
				}
			}
			
			//正常送花
			for (Channel channel : onlineUsers) {
				if (channel == client) {
					request.setSender("you");
					request.setContent("你给大家送了一波鲜花雨");
					setAttrs(client, "lastFlowerTime", currTime);
				}else{
					request.setSender(getNickName(client));
					request.setContent(getNickName(client) + "送来一波鲜花雨");
				}
				request.setTime(CommonUtil.getTime());
				
				String content = encoder.encode(request);
				channel.writeAndFlush(new TextWebSocketFrame(content));
			}
		}
	}
}
