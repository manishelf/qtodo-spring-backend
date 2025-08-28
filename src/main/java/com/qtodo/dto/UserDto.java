package com.qtodo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.qtodo.model.UserEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserDto {
	String firstName;

	String lastName;

	String email;
	
	@JsonProperty(access = Access.WRITE_ONLY)
	String password;
	
	byte[] profilePicture;
	
	String userGroup;
	
	@JsonProperty(access = Access.READ_ONLY)
	String accessToken;
	
	public UserDto(UserEntity ue, String userGroup, byte[] profilePicture) {
		this.firstName = ue.getFirstName();
		this.lastName = ue.getLastName();
		this.email = ue.getEmail();
		this.userGroup = userGroup;
		this.profilePicture = profilePicture;
	}

	public UserEntity toBasicEntity() {
		UserEntity ue = new UserEntity();
		ue.setFirstName(firstName);
		ue.setLastName(lastName);
		ue.setEmail(email);
		return ue;
	}
	
}

