package com.qtodo.service;

import org.apache.commons.lang3.RegExUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qtodo.dao.UserRepo;
import com.qtodo.model.UserEntity;
import com.qtodo.response.ValidationException;
import com.qtodo.utils.CommonUtils;

@Service
public class ValidationService {

	static final String EmailRegx = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
	static final String PasswordRegx = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{5,}$";
	@Autowired
	UserService userService;
	
	@Autowired
	UserRepo userRepo;

	public boolean verifyEmail(String email) throws ValidationException {
		if (CommonUtils.isBlank(email)) {
			throw ValidationException.failedFor("email", "email address cannot be null or empty.");
		}

		boolean isValid = RegExUtils.dotAllMatcher(EmailRegx, email).matches();

		if (!isValid) {
			throw ValidationException.failedFor("email", email + " is not a valid email address");
		}

		return true;
	}

	public boolean verifyPassword(String password) throws ValidationException {
		if (password.length() < 5)
			throw ValidationException.failedFor("password", "cannot be smaller than 5");
		
		boolean isValid = RegExUtils.dotAllMatcher(PasswordRegx, password).matches();

		if (!isValid) {
			throw ValidationException.failedFor("password", "password does not meet criteria atleast: {1 uppercase & 1 number & 1 special}");
		}
		return true;
	}

	public boolean isEmailAlreadyInUse(String email, String userGroup) {
		if (CommonUtils.isBlank(userGroup)) {
			userGroup = "qtodo";
		}
		UserEntity user = userRepo.getByEmailInUserGroup(email, userGroup);
		if(user == null) return false;
		else return true;
	}

}
