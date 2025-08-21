package com.qtodo.dto;

import java.sql.Blob;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
	String firstName;

	String lastName;

	String email;
	
	@JsonProperty(access = Access.WRITE_ONLY)
	String password;
	
	Blob profilePicture;
	
	String userGroup;
	
	@JsonProperty(access = Access.READ_ONLY)
	String accessToken;

}

