package com.qtodo.model;

import java.io.Serializable;
import java.util.Objects;

import com.qtodo.auth.UserPermission;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionForUserInUserGroupKey implements Serializable{

	UserEntity user;
	
	UserGroup userGroup;

	UserPermission permission;

	@Override
	public int hashCode() {
		return Objects.hash(user.getEmail(), userGroup.getGroupTitle(), permission);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if(!(obj instanceof PermissionForUserInUserGroupKey)) return false;
		
		var o = (PermissionForUserInUserGroupKey) obj;
		
		return this.permission.equals(o.getPermission()) &&
				this.user.getEmail().equals(o.getUser().getEmail()) &&
				this.userGroup.getGroupTitle().equals(o.getUserGroup().getGroupTitle());
	}
	
	
}
