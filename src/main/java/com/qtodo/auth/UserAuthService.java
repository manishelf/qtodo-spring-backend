package com.qtodo.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.qtodo.dto.UserDto;
import com.qtodo.dto.UserLoginRequest;
import com.qtodo.model.UserEntity;
import com.qtodo.model.UserGroup;
import com.qtodo.response.TokenResponse;
import com.qtodo.response.UserLoginResponse;
import com.qtodo.response.ValidationException;
import com.qtodo.service.UserService;
import com.qtodo.service.ValidationService;
import com.qtodo.utils.JwtUtils;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;

@Service
public class UserAuthService {

	@Autowired
	ValidationService validationService;

	@Autowired
	UserService userService;

	@Autowired
	JwtUtils jwtUtils;
	
	@Autowired
    AuthenticationManager authenticationManager;

	public TokenResponse signupUser(UserDto userDetails) throws ValidationException {
		validationService.verifyEmail(userDetails.getEmail());
		validationService.verifyPassword(userDetails.getPassword());

		String email = userDetails.getEmail();
		String userGroup = userDetails.getUserGroup();

		boolean exists = validationService.isEmailAlreadyInUse(email, userGroup);

		if (exists)
			throw ValidationException.failedFor("email", email + " email already in use in usergroup " + userGroup);

		UserEntity ue = userService.addUser(userDetails);
		TokenResponse tokens = null;
		
		tokens = jwtUtils.generateTokenForUser(userDetails, getUserClaimsIfUgOwner(ue, userGroup));
		
        ResponseCookie cookie = ResponseCookie.from("refresh_token_for_"+ userGroup, tokens.getRefreshToken())
        		.secure(true)
        		.httpOnly(true)
        		.maxAge(jwtUtils.getJwtRefreshExpirationMs())
				.sameSite("None")
                .build();

        
        tokens.setNewCookie(cookie);
        tokens.setResponseMessage("signed up for usergroup "+tokens.getUserGroup()+" successfully");
        
        return tokens;
	}
	
	public Map<String, Object> getUserClaimsIfUgOwner(UserEntity ue, String ugTitle){
		List<UserGroup> owns = ue.getOwnerOfUserGroups();
		UserGroup currUg = userService.getUserGroupByTitle(ugTitle);
		Map<String, Object> claims = jwtUtils.getGenericClaimsMap(new UserDto(ue, ugTitle,null));
		
		if(!owns.isEmpty() && owns.contains(currUg)) {
			List<UserPermissions> permissions = (List<UserPermissions>) claims.get("permissions");
			var newPermissions = new ArrayList<>(permissions);
			newPermissions.add(UserPermissions.UG_OWNER);
			claims.put("permissions", newPermissions);
			
		}	
		return claims;
	}

	public UserLoginResponse loginUser(UserLoginRequest loginCreds) throws ValidationException {
		String email = loginCreds.getEmail();
		String userGroup = loginCreds.getUserGroup();

		validationService.verifyEmail(email);
		validationService.verifyPassword(loginCreds.getPassword());

		Authentication authentication = authenticationManager.authenticate(
	            new UsernamePasswordAuthenticationToken(
	                loginCreds.getEmail()+"/usergroup/"+userGroup,
	                loginCreds.getPassword()
	            )
	        );
		
		
		CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
		UserEntity ue = user.getUserEntity();
		UserDto userDto;
		userDto = userService.getUserDetailsForUserInUserGroup(ue, userGroup);
		
		TokenResponse tokens = jwtUtils.generateTokenForUser(userDto, getUserClaimsIfUgOwner(ue, userGroup));

		UserLoginResponse response = new UserLoginResponse(HttpStatus.OK);
		response.setUserDetails(userDto);
		userDto.setAccessToken(tokens.getAccessToken());
		response.setTokens(tokens);
		
		
        ResponseCookie cookie = ResponseCookie.from("refresh_token_for_"+response.getUserDetails().getUserGroup()
        		, response.getTokens().getRefreshToken())
        		.secure(true)
        		.httpOnly(true)
        		.maxAge(jwtUtils.getJwtRefreshExpirationMs())
				.sameSite("None")
                .build();

        response.setNewCookie(cookie);
        response.setResponseMessage("logged in for usergroup "+response.getUserDetails().getUserGroup());

		return response;
	}

	public TokenResponse refreshAuthToken(String expiredToken, String refreshToken) throws ValidationException {
		
		Claims refClaims = jwtUtils.getUserClaimsFromJwtToken(refreshToken);

		Claims claims = jwtUtils.getUserClaimsFromExpiredToken(expiredToken);
		

		String email = refClaims.getSubject();
		String userGroup = (String) refClaims.get("user_group");
		String alias = (String) refClaims.get("alias");

		UserDto userDto = new UserDto();
		userDto.setEmail(email);
		userDto.setUserGroup(userGroup);
		userDto.setAlias(alias);

		var tokens = jwtUtils.generateTokenForUser(userDto);
		
		 ResponseCookie cookie = ResponseCookie.from("refresh_token_for_"+tokens.getUserGroup(), tokens.getRefreshToken())
        		.secure(true)
        		.httpOnly(true)
        		.maxAge(jwtUtils.getJwtRefreshExpirationMs())
				.sameSite("None")
                .build();
       
        tokens.setNewCookie(cookie);
        
        return tokens;
	}

	public String getRefreshTokenForUserGroupFromCookies(Cookie[] cookies, String sessionToken) throws ValidationException {
        
		Claims claims = jwtUtils.getUserClaimsFromExpiredToken(sessionToken);
		
		String userGroup = (String)claims.get("user_group");
		
		String refToken = null;
		
		for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh_token_for_"+userGroup)) {
            	refToken = cookie.getValue();
            }
        }
		
		if(refToken == null) throw ValidationException.failedFor("refresh_token", "not found for user group "+userGroup);
		
		return refToken;
	}

	public ResponseCookie clearRefreshTokenForUserGroupFromCookies(String sessionToken) throws ValidationException {
		
		Claims claims = jwtUtils.getUserClaimsFromExpiredToken(sessionToken);
		
		String userGroup = (String)claims.get("user_group");
		
        return ResponseCookie.from("refresh_token_for_"+userGroup, null)
        		.path("/")
        		.secure(true)
        		.httpOnly(true)
        		.maxAge(jwtUtils.getJwtRefreshExpirationMs())
				.sameSite("None")
                .build();
	}

	public List<String> getOpenUserGroupTitles() {
		
		return userService.getOpenUserGroupTitles();
		
	}
	
	public UserEntity getAuthenticatedUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return (UserEntity) authentication.getPrincipal();
	}

	public UserGroup getAuthenticatedUsersUserGroup() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String userGroupTitle =  (String) authentication.getCredentials();
		return userService.getUserGroupByTitle(userGroupTitle);
	}

}
