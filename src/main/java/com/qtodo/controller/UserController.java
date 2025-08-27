package com.qtodo.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qtodo.auth.UserAuthService;
import com.qtodo.dto.UserDto;
import com.qtodo.dto.UserLoginRequest;
import com.qtodo.response.ApiResponseBase;
import com.qtodo.response.TokenResponse;
import com.qtodo.response.UserLoginResponse;
import com.qtodo.response.ValidationException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@RestController
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	UserAuthService userAuthService;
	
	
	@PostMapping("/login")
	ResponseEntity<UserLoginResponse> loginUser(@RequestBody UserLoginRequest loginCreds, HttpServletRequest request, HttpServletResponse response) throws ValidationException{
		UserLoginResponse loginResponse = userAuthService.loginUser(loginCreds);
        
        loginResponse.setExistingCookies(request.getCookies());
        return ApiResponseBase.asWrapped(loginResponse);
	}
	
	@PostMapping("/signup")
	ResponseEntity<TokenResponse> signupUser(@RequestBody UserDto userDetails, HttpServletRequest request) throws ValidationException{
		
		var signupResponse = userAuthService.signupUser(userDetails);
        
        signupResponse.setExistingCookies(request.getCookies());
        
        return ApiResponseBase.asWrapped(signupResponse);
	}
	
	@GetMapping("/refresh")
	ResponseEntity<TokenResponse> getRefreshToken(
			@RequestHeader(HttpHeaders.AUTHORIZATION) String sessionToken,
			HttpServletRequest request
			) throws ValidationException{
		
		
		if(sessionToken == null || sessionToken.length()<8) {
			throw ValidationException.failedFor("session_token", "not found");
		}
		sessionToken = sessionToken.substring(7); //"Bearer "
		
		Cookie[] cookies = request.getCookies();
		
		if(cookies == null) throw ValidationException.failedFor("refresh_token", "not found");
		
		String refreshToken = userAuthService.getRefreshTokenForUserGroupFromCookies(cookies, sessionToken);
		
        var refreshResponse = userAuthService.refreshAuthToken(sessionToken, refreshToken);
        refreshResponse.setExistingCookies(cookies);
        
        return ApiResponseBase.asWrapped(refreshResponse);
	}
	
	@PostMapping("/logout")
	ResponseEntity<ApiResponseBase> logoutUser(
			@RequestHeader(HttpHeaders.AUTHORIZATION) String sessionToken,
			HttpServletRequest request
			) throws ValidationException {
		
		if(sessionToken == null) {
			throw ValidationException.failedFor("session_token", "not found");
		}
		
		ResponseCookie clearedCookie = userAuthService.clearRefreshTokenForUserGroupFromCookies(sessionToken);
        
		var response = new ApiResponseBase("Logged out successfully!",HttpStatus.OK, request.getCookies(), clearedCookie);
	
        return ApiResponseBase.asWrapped(response);
	}
	
	
	@GetMapping("/usergroups")
	ResponseEntity<ApiResponseBase> getOpenUserGroupTitleList(){
		List<String> res = userAuthService.getOpenUserGroupTitles();
		
		var response = new ApiResponseBase(res, HttpStatus.OK);
		
		return ApiResponseBase.asWrapped(response);
	}
}
