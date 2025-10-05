package com.qtodo.model;

import com.qtodo.auth.UserPermission;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@IdClass(PermissionForUserInUserGroupKey.class) // same permission cannot be repeated for given user in group
public class PermissionForUserInUserGroup{
	
	@Id
	@ManyToOne
	@NotNull
	UserEntity user;
	
	@Id
	@ManyToOne
	@NotNull
	UserGroup userGroup;

	@Id
	@Enumerated(EnumType.STRING)
	@NotNull
	UserPermission permission;
	
	boolean enabled;
}
