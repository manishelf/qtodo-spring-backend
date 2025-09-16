package com.qtodo.service;

import java.util.LinkedList;
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
import com.qtodo.response.ValidationException;
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
	
	public UserEntity addUser(UserDto userDetails) throws ValidationException {

		var user = userDetails.toBasicEntity();

		String userGroup = userDetails.getUserGroup();
		
		if (CommonUtils.isBlank(userGroup)) {
			userGroup = "qtodo";
		}

		String encPassword = passwordEncoder.encode(userDetails.getPassword());
		user.setEmail(userDetails.getEmail());
		user.setEncryptedPassword(encPassword);

		UserGroup ug = userGroupRepo.getByGroupTitle(userGroup);

		if (ug == null) {
			ug = new UserGroup();
			ug.setGroupTitle(userGroup);
			ug.getOwningUsers().add(user);
			user.getOwnerOfUserGroups().add(ug);
		}

		ug.getParticipantUsers().add(user);
		user.getParticipantInUserGroups().add(ug);
		userRepo.save(user);
		userGroupRepo.save(ug);
		
		String profPic = userDetails.getProfilePicture();
		if(profPic != null) {
			DocumentEntity profilePicEntity = new DocumentEntity();
			profilePicEntity.setInfo("profile_pic_" + userGroup);
			profilePicEntity.setDataType("image");
			profilePicEntity.setRefUrl("/"+profPic);
			profilePicEntity.setOwningUser(user);
			profilePicEntity.setOwningUserGroup(ug);
			docRepo.save(profilePicEntity);
			
			user.getDocs().add(profilePicEntity);
		}

		return user;

	} 

	public boolean userExistsWithEmail(String email) {
		Long userId = this.userRepo.getIdByEncryptedEmail(email);
		if (userId != null)
			return true;
		return false;
	}

	public UserDto getUserDetailsForUserInUserGroup(UserEntity user, String userGroup) {
		
		String profilePicUrl = null;
		Optional<DocumentEntity> profPic = userRepo.getProfilePicByUserId(user.getId(), "profile_pic_"+userGroup);
		if(profPic.isPresent()) {
			var profPicUrl = profPic.get().getRefUrl();
			profilePicUrl = "/item/doc"+profPicUrl;
		}
		UserDto userDetails = new UserDto(user, userGroup, profilePicUrl);
		return userDetails;
	}

	public List<String> getOpenUserGroupTitles() {
		List<String> res = new LinkedList();
		
		userGroupRepo.findAllOpen().stream().forEach(ug->{
			res.add(ug.getGroupTitle());
		});
		
		return res;
	}

	public UserGroup getUserGroupByTitle(String userGroupTitle) {
		return userGroupRepo.getByGroupTitle(userGroupTitle);
	}

}
