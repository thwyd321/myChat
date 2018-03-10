package com.yudi.catalina.servlets;

import com.yudi.catalina.http.MYServlet;
import com.yudi.catalina.http.MyRequest;
import com.yudi.catalina.http.MyResponse;

public class MyOwnServlet extends MYServlet {

	@Override
	public void doGet(MyRequest request, MyResponse response) {
		// TODO Auto-generated method stub
		try {
			response.write(request.getParameters("name"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void doPost(MyRequest request, MyResponse response) {
		// TODO Auto-generated method stub
		
	}

}
