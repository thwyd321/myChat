package com.yudi.catalina.netty.server;

import org.apache.log4j.chainsaw.Main;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.ClientAuth;

public class MYTomcat {

	public void start(int port) throws Exception {
		// boss thread
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		// worker thread
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		// netty服务
		try {
			ServerBootstrap server = new ServerBootstrap();
			// 链路式编程
			server.group(bossGroup, workerGroup)
					// 主线程处理类
					.channel(NioServerSocketChannel.class)
					// 子线程处理类
					.childHandler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel client) throws Exception {
							// 无锁化 串行编程
							// 业务逻辑链路,编码器
							client.pipeline().addLast(new HttpResponseEncoder());
							// 解码器
							client.pipeline().addLast(new HttpRequestDecoder());
							// 自己业务逻辑处理
							client.pipeline().addLast(new MYTomcatHandler());
						}

					})
					//配置信息
					.option(ChannelOption.SO_BACKLOG, 128)// main thread配置
					.childOption(ChannelOption.SO_KEEPALIVE, true);// 子线程配置
			//阻塞
			ChannelFuture f = server.bind(port).sync();
			System.out.println("tomcat服务已经启动" + port);
			f.channel().closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception {
		new MYTomcat().start(8080);
	}
}
