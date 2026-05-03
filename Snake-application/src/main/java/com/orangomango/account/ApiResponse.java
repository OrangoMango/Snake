package com.orangomango.account;

public class ApiResponse{
	private int code;
	public String content;

	public ApiResponse(int code, String content){
		this.code = code;
		this.content = content;
	}

	public int getCode(){
		return this.code;
	}

	public String getContent(){
		return this.content;
	}
}