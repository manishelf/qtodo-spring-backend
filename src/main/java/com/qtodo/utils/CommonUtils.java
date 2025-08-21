package com.qtodo.utils;

import com.qtodo.dto.UserDto;
import com.qtodo.model.UserEntity;

public class CommonUtils {

	public static boolean isBlank(String userGroup) {
		if(userGroup == null || userGroup.isBlank()) return true;
		return false;
	}

	public static UserDto basicUserDtoFromUserEntity(UserEntity userEntity) {
		UserDto userDto = new UserDto();
		
		userDto.setEmail(userEntity.getEmail());
		userDto.setFirstName(userEntity.getFirstName());
		userDto.setLastName(userEntity.getLastName());
		
	
		return userDto;
	}

}
