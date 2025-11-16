package com.qtodo.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.qtodo.auth.CustomUserDetails;
import com.qtodo.auth.UserPermission;
import com.qtodo.dao.DocumentRepo;
import com.qtodo.dao.FormSchemaRepo;
import com.qtodo.dao.PermissionForUserInUserGroupRepo;
import com.qtodo.dao.TagRepo;
import com.qtodo.dao.TodoItemRepo;
import com.qtodo.dao.UserDefinedTypesRepo;
import com.qtodo.dao.UserGroupRepo;
import com.qtodo.dao.UserRepo;
import com.qtodo.model.UserEntity;
import com.qtodo.model.UserGroup;

import lombok.Getter;

@Service
@Getter
@Component
public class ServiceBase {

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
	protected UserGroupRepo userGroupRepo;
	
	@Autowired
	protected PermissionForUserInUserGroupRepo permissionRepo;

	@Autowired
    AuthenticationManager authenticationManager;
	
	protected Logger logger = LogManager.getLogger("com.qtodo.service");
	
	@Value("${qtodo.app.user.doc.location}")
	protected String fsDocRootUrl;
	
		
	public UserEntity getAuthenticatedUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return ((CustomUserDetails) authentication.getPrincipal()).getUserEntity();
	}

	public UserGroup getAuthenticatedUsersUserGroup() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		CustomUserDetails user =  (CustomUserDetails) authentication.getPrincipal();
		return userGroupRepo.getByGroupTitle(user.getUserGroup());
	}
	
	public List<UserPermission> getAuthenticatedUsersPermissions(){
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		var user = ((CustomUserDetails) authentication.getPrincipal());
		var authorities = user.getAuthorities();
		
		List<UserPermission> permissions = new ArrayList<>();
		authorities.forEach(a->{
			permissions.add(UserPermission.valueOf(a.getAuthority()));
		});
		
		return permissions;
	}

}
