package com.qtodo.utils;

public class CommonUtils {

	public static boolean isBlank(String userGroup) {
		if(userGroup == null || userGroup.isBlank()) return true;
		return false;
	}
}
