package com.qtodo.response;

import org.springframework.http.HttpStatusCode;

import com.qtodo.dto.UserDto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserLoginResponse extends ApiResponseBase{

	UserDto userDetails;
	
	TokenResponse tokens;
	
	public UserLoginResponse(HttpStatusCode status) {
		super(status);
	}
	
}
