package com.yudi.catalina.http;

public abstract class MYServlet {

	public abstract void doGet(MyRequest request,MyResponse response);
	public abstract void doPost(MyRequest request,MyResponse response);
}
