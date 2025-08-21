package com.qtodo.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.qtodo.dao.DocumentRepo;
import com.qtodo.dao.UserGroupRepo;
import com.qtodo.dao.UserRepo;
import com.qtodo.dto.UserDto;
import com.qtodo.model.DocumentEntity;
import com.qtodo.model.UserEntity;
import com.qtodo.model.UserGroup;
import com.qtodo.utils.CommonUtils;

@Service
public class UserService {

	@Autowired
	UserRepo userRepo;

	@Autowired
	DocumentRepo docRepo;

	@Autowired
	UserGroupRepo userGroupRepo;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	public Long addUser(UserDto userDetails) {
		var user = new UserEntity();

		user.setFirstName(userDetails.getFirstName());
		user.setLastName(userDetails.getLastName());
		String userGroup = userDetails.getUserGroup();

		if (CommonUtils.isBlank(userGroup)) {
			userGroup = "qtodo";
		}
		

		DocumentEntity profilePic = new DocumentEntity();
		profilePic.setData(userDetails.getProfilePicture());
		profilePic.setInfo("profile_pic_"+userGroup);
		profilePic.setDataType("image");

		docRepo.save(profilePic);
		String encodedEmail = URLEncoder.encode(userDetails.getEmail(), StandardCharsets.UTF_8);
		profilePic.setRefUrl("/user/" + encodedEmail +"/group/"+userGroup+"/profileImage");

		user.getDocs().add(profilePic);

		String encPassword = passwordEncoder.encode(userDetails.getPassword());

		user.setEmail(userDetails.getEmail());
		user.setEncryptedPassword(encPassword);

		userRepo.save(user);
		
		UserGroup ug = userGroupRepo.getByGroupTitle(userGroup);
		if (ug == null) {
			ug = new UserGroup();
			ug.setGroupTitle(userGroup);
			ug.getParticipantUsers().add(user);
		}
		userRepo.save(user);
		userGroupRepo.save(ug);
		
		return user.getId();
	}

	public boolean userExistsWithEmail(String email) {
		Long userId = this.userRepo.getIdByEncryptedEmail(email);
		if (userId != null)
			return true;
		return false;
	}

	public UserDto getUserDetailsForUserByEmailAndUserGroup(String email, String userGroup) {
		
		UserEntity user = userRepo.getByEmailInUserGroup(email, userGroup);
		if(user == null) {			
			return null;
		}
		
		UserDto userDetails = new UserDto();
		
		userDetails.setFirstName(email);
		userDetails.setUserGroup(userGroup);
		userDetails.setFirstName(user.getFirstName());
		userDetails.setLastName(user.getLastName());
		userDetails.setEmail(user.getEmail());
		
		DocumentEntity profPic = userRepo.getProfilePicByUserId(user.getId(), "profile_pic_"+userGroup);
		
		userDetails.setProfilePicture(profPic.getData());
		
		return userDetails;
	}

}
