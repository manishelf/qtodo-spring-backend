package com.qtodo.auth;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.qtodo.model.UserEntity;
import com.qtodo.utils.JwtUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomUserDetails implements UserDetails {

	UserEntity userEntity;
	
	String userGroup;
	
	@Autowired
	JwtUtils jwtUtils;
	
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return null;
	}

	@Override
	public String getPassword() {
		return userEntity.getEncryptedPassword();
	}

	@Override
	public String getUsername() {
		return userEntity.getEmail();
	}
	
	public String getUserEmail() {
		return userEntity.getEmail();
	}
	
	public String getUserAlias() {
		return userEntity.getAlias();
	}
	
	public CustomUserDetails() {
	}
	
	public CustomUserDetails(UserEntity userEntity) {
		this.userEntity = userEntity;
	}
	
	public CustomUserDetails(UserEntity userEntity, String userGroup){
		this.userEntity = userEntity;
		this.userGroup = userGroup;
	}

}
