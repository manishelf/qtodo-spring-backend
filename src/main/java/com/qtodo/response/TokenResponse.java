package com.qtodo.response;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TokenResponse extends ApiResponseBase{
	
	String accessToken;

	@JsonIgnore
	String refreshToken;
	
	String userGroup;
	
	public TokenResponse(String userGroup, String accessToken,String refreshToken) {
		super(HttpStatus.OK);
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.userGroup = userGroup;
	}
	
	
	public TokenResponse(ValidationException e) {
		super(HttpStatus.BAD_REQUEST);
	}
	
}
