package com.qtodo.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.qtodo.dao.UserRepo;
import com.qtodo.model.UserEntity;
import com.qtodo.response.ValidationException;

@Service
public class UserDetailsServiceImpl implements UserDetailsService{
	
	@Autowired
	UserRepo userRepo;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		int i = username.indexOf("/usergroup/");
		
		String email = username.substring(0, i);
		String userGroup = username.substring(i+11);
		
		
		UserEntity user = userRepo.getByEmailInUserGroup(email, userGroup);
		
		if(user == null) throw new ValidationException().failedFor("email", "no user with email "+email+" in usergroup "+userGroup);
	
		CustomUserDetails userDetails = new CustomUserDetails();
		
		userDetails.setUserEntity(user);
		
		return userDetails;
	}
	
}
