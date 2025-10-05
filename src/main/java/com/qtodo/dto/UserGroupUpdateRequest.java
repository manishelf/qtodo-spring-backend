package com.qtodo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserGroupUpdateRequest {
	
//	String userGroupName;
	String userGroupDescription;
	Boolean open;
	Boolean colaboration;
}
