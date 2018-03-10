package com.yudi.chat.server.handler;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URL;

import org.apache.log4j.Logger;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;

public class httphandler extends SimpleChannelInboundHandler<FullHttpRequest> {
	private static Logger LOG = Logger.getLogger(httphandler.class);

	// classpath
	private URL baseURL = httphandler.class.getProtectionDomain().getCodeSource().getLocation();
	private final String WEB_ROOT = "webroot";

	private File getFileFromRoot(String fileName) throws Exception {
		String path = baseURL.toURI() + WEB_ROOT + "/" + fileName;
		path = !path.startsWith("file:") ? path : path.substring(5);
		path = path.replace("//", "/");
		return new File(path);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		String uri = request.getUri();

		RandomAccessFile file = null;
		try {
			String page = uri.equals("/") ? "chat.html" : uri;
			file = new RandomAccessFile(getFileFromRoot(page), "r");
		} catch (Exception e) {
			ctx.fireChannelRead(request.retain());
			return;
		}
		HttpResponse response = new DefaultHttpResponse(request.getProtocolVersion(), HttpResponseStatus.OK);
		String contextType = "text/html;";
		if (uri.endsWith(".css")) {
			contextType = "text/css;";
		} else if (uri.endsWith(".js")) {
			contextType = "text/javascript;";
		} else if (uri.toLowerCase().matches("(jpg|png|gif)$")) {
			String ext = uri.substring(uri.lastIndexOf("."));
			contextType = "image/" + ext;
		}
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, contextType + "charset=utf-8;");
		boolean keepAlive = HttpHeaders.isKeepAlive(request);
		if (keepAlive) {
			response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, file.length());
			response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		}

		ctx.write(response);
		ctx.write(new DefaultFileRegion(file.getChannel(), 0, file.length()));
		ChannelFuture f = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

		if (!keepAlive) {
			f.addListener(ChannelFutureListener.CLOSE);
		}

		file.close();
		// System.out.println(file);
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		Channel client = ctx.channel();
		// 当出现异常就关闭连接
		cause.printStackTrace();
		ctx.close();
	}

}
