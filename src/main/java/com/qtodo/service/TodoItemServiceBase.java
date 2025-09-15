package com.qtodo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.qtodo.auth.UserAuthService;
import com.qtodo.dao.DocumentRepo;
import com.qtodo.dao.FormSchemaRepo;
import com.qtodo.dao.TagRepo;
import com.qtodo.dao.TodoItemRepo;
import com.qtodo.dao.UserDefinedTypesRepo;
import com.qtodo.dao.UserRepo;
import com.qtodo.model.UserEntity;
import com.qtodo.model.UserGroup;

import lombok.Getter;

@Service
@Getter
@Component
public class TodoItemServiceBase {

	@Autowired
	protected TodoItemRepo todoItemRepo;

	@Autowired
	protected TagRepo tagRepo;

	@Autowired
	protected UserDefinedTypesRepo udtRepo;
	
	@Autowired 
	protected FormSchemaRepo fsRepo; 
	
	@Autowired
	protected DocumentRepo docRepo;
	
	@Autowired
	protected UserRepo userRepo;
	
	@Autowired
	protected UserAuthService userAuthService;
	
	@Value("${qtodo.app.user.doc.location}")
	protected String fsDocUrl;
	
	protected UserEntity getAuthenticatedUser() {
		return userAuthService.getAuthenticatedUser();
	}
	
	protected UserGroup getAuthenticatedUserGroup() {
		return userAuthService.getAuthenticatedUsersUserGroup();
	}
}
