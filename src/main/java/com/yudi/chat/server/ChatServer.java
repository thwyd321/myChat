package com.yudi.chat.server;

import org.apache.log4j.chainsaw.Main;

import com.yudi.catalina.netty.server.MYTomcatHandler;
import com.yudi.chat.protocol.IMDecoder;
import com.yudi.chat.protocol.IMEncoder;
import com.yudi.chat.server.handler.httphandler;
import com.yudi.chat.server.handler.SocketHandler;
import com.yudi.chat.server.handler.WebSocketHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class ChatServer {
	
	public void start(int port) throws Exception {
		// boss thread
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		// worker thread
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		// netty服务
		try {
			//启动引擎
			ServerBootstrap server = new ServerBootstrap();
			// 链路式编程
			server.group(bossGroup, workerGroup)
					// 主线程处理类
					.channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 1024)
					// 子线程处理类
					.childHandler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel client) throws Exception {
							
							ChannelPipeline pipeline = client.pipeline();
							//--------支持自定义socket协议
							pipeline.addLast(new IMDecoder());
							pipeline.addLast(new IMEncoder());
							pipeline.addLast(new SocketHandler());
							//------------这里是用来支持http协议的
							//解码和编码http请求
							pipeline.addLast(new HttpServerCodec());
							pipeline.addLast(new HttpObjectAggregator(64*1024));
							//用于处理文件流的一个handler
							pipeline.addLast(new ChunkedWriteHandler());
							pipeline.addLast(new httphandler());
						
							//------支持webSocket协议
							pipeline.addLast(new WebSocketServerProtocolHandler("/im"));
							pipeline.addLast(new WebSocketHandler());
						
							
						}

					});
					//配置信息
					// main thread配置
					//.childOption(ChannelOption.SO_KEEPALIVE, true);// 子线程配置			//阻塞
			ChannelFuture f = server.bind(port).sync();
			System.out.println("聊天室服务已经启动" + port);
			f.channel().closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	public static void main(String[] args) {
		 try {
			new ChatServer().start(8080);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
