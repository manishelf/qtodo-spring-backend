package com.qtodo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequest {

	String email;
	
	String password;
	
	String userGroup;
	
}
