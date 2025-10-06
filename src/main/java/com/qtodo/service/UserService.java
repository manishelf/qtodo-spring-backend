package com.qtodo.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.qtodo.auth.UserRole;
import com.qtodo.dto.UserDto;
import com.qtodo.model.DocumentEntity;
import com.qtodo.model.PermissionForUserInUserGroup;
import com.qtodo.model.UserEntity;
import com.qtodo.model.UserGroup;
import com.qtodo.response.ValidationException;
import com.qtodo.utils.CommonUtils;
import com.qtodo.utils.JwtUtils;

@Service
public class UserService extends ServiceBase{
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	public UserEntity addUser(UserDto userDetails) throws ValidationException {

		var user = userDetails.toBasicEntity();

		String userGroup = userDetails.getUserGroup();
		
		var roles = new ArrayList<UserRole>();
		roles.add(UserRole.AUDIENCE);
		
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
			if(!ug.getGroupTitle().equals("qtodo")) {
				user.getOwnerOfUserGroups().add(ug);
				roles.add(UserRole.UG_OWNER);
				roles.add(UserRole.AUTHOR);
			}
		}
		
		if(ug.getGroupTitle().equals("qtodo")) {
			roles.add(UserRole.AUTHOR);
		}
		
		if(ug.isColaboration()) {
			roles.add(UserRole.COLLABORATOR);
		}

		ug.getParticipantUsers().add(user);
		user.getParticipantInUserGroups().add(ug);
		userRepo.save(user);
		userGroupRepo.save(ug);
		
		final var finalUg = ug;
		final var finalUser = user;
		roles.forEach(role->{
			var permissions = JwtUtils.getPermissionsForUserRole(role);
			permissions.forEach(permission->{
				var pe = new PermissionForUserInUserGroup();
				pe.setPermission(permission);
				pe.setUser(finalUser);
				pe.setUserGroup(finalUg);
				pe.setEnabled(true);
				permissionRepo.save(pe);
			});
		});
		
		return user;

	} 

	public boolean userExistsWithEmail(String email) {
		Long userId = this.userRepo.getIdByEncryptedEmail(email);
		if (userId != null)
			return true;
		return false;
	}

	public UserDto getUserDetailsForUserInUserGroup(UserEntity user, String userGroup) {	
		
		var permissions = permissionRepo.getByUserEmailAndGroupTitle(user.getEmail(), userGroup)
						.stream().filter(perm->!perm.isEnabled()).map(perm->perm.getPermission()).collect(Collectors.toList());
			
		UserGroup ug = userGroupRepo.getByGroupTitle(userGroup);
		UserDto userDetails = new UserDto(user, ug);
		
		userDetails.setProfilePicture(getProfilePicUrlForUser(user.getId(), userGroup));
		userDetails.setPermissions(permissions);
		
		return userDetails;
	}

	public List<String> getOpenUserGroupTitles() {
		List<String> res = new LinkedList();
		
		userGroupRepo.findAllOpen().stream().forEach(ug->{
			res.add(ug.getGroupTitle());
		});
		
		return res;
	}

	public String getProfilePicUrlForUser(Long userId, String userGroup) {
		String profilePicUrl = null;
		Optional<DocumentEntity> profPic = userRepo.getProfilePicByUserId(userId, "profile_pic_"+userGroup);
		if(profPic.isPresent()) {
			var profPicUrl = profPic.get().getRefUrl();
			profilePicUrl = "/item/doc"+profPicUrl;
		}
		return profilePicUrl;
	}

}
