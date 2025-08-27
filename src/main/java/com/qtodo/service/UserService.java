package com.qtodo.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.sql.rowset.serial.SerialBlob;

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
	
	public Long addUser(UserDto userDetails) throws ValidationException {

		var user = userDetails.toBasicEntity();

		String userGroup = userDetails.getUserGroup();
		
		if (CommonUtils.isBlank(userGroup)) {
			userGroup = "qtodo";
		}

		DocumentEntity profilePicEntity = new DocumentEntity();

		byte[] profPicData = userDetails.getProfilePicture();

		if (profPicData != null) {
			try {
				profilePicEntity.setData(new SerialBlob(profPicData));
			} catch (Exception e) {
				e.printStackTrace();
				throw ValidationException.failedFor("profile_pic", e.getMessage());
			}
			profilePicEntity.setInfo("profile_pic_" + userGroup);
			profilePicEntity.setDataType("image");
		}

		docRepo.save(profilePicEntity);
		String encodedEmail = URLEncoder.encode(userDetails.getEmail(), StandardCharsets.UTF_8);
		profilePicEntity.setRefUrl("/user/" + encodedEmail + "/group/" + userGroup + "/profileImage");
		user.getDocs().add(profilePicEntity);

		String encPassword = passwordEncoder.encode(userDetails.getPassword());
		user.setEmail(userDetails.getEmail());
		user.setEncryptedPassword(encPassword);

		UserGroup ug = userGroupRepo.getByGroupTitle(userGroup);

		if (ug == null) {
			ug = new UserGroup();
			ug.setGroupTitle(userGroup);
		}

		ug.getParticipantUsers().add(user);
		user.getParticipantInUserGroups().add(ug);
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

	public UserDto getUserDetailsForUserInUserGroup(UserEntity user, String userGroup) throws SQLException {
		
		DocumentEntity profPic = userRepo.getProfilePicByUserId(user.getId(), "profile_pic_"+userGroup);
		var profPicData = profPic.getData();
		UserDto userDetails = new UserDto(user, userGroup, profPicData.getBytes(0, (int)profPicData.length()));
		
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
