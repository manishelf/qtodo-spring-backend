package com.qtodo.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.qtodo.auth.UserPermission;
import com.qtodo.model.UserEntity;
import com.qtodo.model.UserGroup;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserDto {
	String alias;

	String email;
	
	@JsonProperty(access = Access.WRITE_ONLY)
	String password;
	
	String profilePicture;
	
	String userGroup;
	
	boolean isOnlineInUg;
	
	List<UserPermission> permissions;
	
	
	@JsonProperty(access = Access.READ_ONLY)
	boolean isUserGroupOpen = true;
	
	@JsonProperty(access = Access.READ_ONLY)
	boolean isUserGroupColaboration = false;
	
	@JsonProperty(access = Access.READ_ONLY)
	String accessToken;
	
	public UserDto(UserEntity ue, UserGroup ug) {
		this.alias = ue.getAlias();
		this.email = ue.getEmail();
		this.userGroup = ug.getGroupTitle();
		this.isUserGroupColaboration = ug.isColaboration();
		this.isUserGroupOpen = ug.isOpen();
	}

	public UserEntity toBasicEntity() {
		UserEntity ue = new UserEntity();
		ue.setAlias(alias);
		ue.setEmail(email);
		return ue;
	}
	
}

