package com.qtodo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qtodo.auth.UserAuthService;
import com.qtodo.dao.TagRepo;
import com.qtodo.dao.TodoItemRepo;
import com.qtodo.model.UserEntity;
import com.qtodo.model.UserGroup;

import lombok.Getter;

@Service
@Getter
public class TodoItemServiceBase {

	@Autowired
	protected TodoItemRepo todoItemRepo;

	@Autowired
	protected TagRepo tagRepo;
	
	@Autowired
	protected UserAuthService userAuthService;
	
	
	protected UserEntity getAuthenticatedUser() {
		return userAuthService.getAuthenticatedUser();
	}
	
	protected UserGroup getUserGroup() {
		return userAuthService.getAuthenticatedUserUserGroup();
	}
}
