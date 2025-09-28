package com.qtodo.dto;

import org.springframework.http.HttpStatusCode;

import com.qtodo.response.ApiResponseBase;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocketMessage extends ApiResponseBase{
	
	String usergroup;
	
	Operation operation;
	
	public SocketMessage(String usergroup, String responseMessage, HttpStatusCode status) {
		super(responseMessage, status);
		this.usergroup = usergroup;
	}

	public static enum Operation{
		MERGE,
		NOTIFY,
	}
}
