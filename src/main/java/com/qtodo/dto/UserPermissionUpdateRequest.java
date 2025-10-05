package com.qtodo.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserPermissionUpdateRequest {
	String userEmail;
	String userPermission;
	boolean enabled;
}
