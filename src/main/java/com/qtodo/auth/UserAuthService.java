package com.qtodo.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.qtodo.dto.UserDto;
import com.qtodo.dto.UserLoginRequest;
import com.qtodo.response.TokenResponse;
import com.qtodo.response.UserLoginResponse;
import com.qtodo.response.ValidationException;
import com.qtodo.service.UserService;
import com.qtodo.service.ValidationService;
import com.qtodo.utils.CommonUtils;
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

		userService.addUser(userDetails);

		return jwtUtils.generateTokenForUser(userDetails);
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
		
		UserDto userDto = CommonUtils.basicUserDtoFromUserEntity(user.getUserEntity());

		userDto.setUserGroup(userGroup);
		
		TokenResponse tokens = jwtUtils.generateTokenForUser(userDto);

		UserLoginResponse response = new UserLoginResponse(HttpStatus.OK);
		response.setUserDetails(userDto);
		response.setTokens(tokens);

		return response;
	}

	public TokenResponse refreshAuthToken(String expiredToken, String refreshToken) throws ValidationException {
		
		Claims refClaims = jwtUtils.getUserClaimsFromJwtToken(refreshToken);
		
		Claims claims = jwtUtils.getUserClaimsFromExpiredToken(expiredToken);
		

		String email = refClaims.getSubject();
		String userGroup = (String) refClaims.get("user_group"); // to prevent mis-matched tokens to affect
		String firstName = (String) refClaims.get("first_name");
		String lastName = (String) claims.get("last_name");

		UserDto userDto = new UserDto();
		userDto.setEmail(email);
		userDto.setUserGroup(userGroup);
		userDto.setFirstName(firstName);
		userDto.setLastName(lastName);

		return jwtUtils.generateTokenForUser(userDto);
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
                .build();
	}

}
