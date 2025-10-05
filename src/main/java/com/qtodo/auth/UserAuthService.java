package com.qtodo.auth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.qtodo.dto.UserDto;
import com.qtodo.dto.UserGroupUpdateRequest;
import com.qtodo.dto.UserLoginRequest;
import com.qtodo.dto.UserPermissionUpdateRequest;
import com.qtodo.model.PermissionForUserInUserGroup;
import com.qtodo.model.UserEntity;
import com.qtodo.model.UserGroup;
import com.qtodo.response.TokenResponse;
import com.qtodo.response.UserLoginResponse;
import com.qtodo.response.ValidationException;
import com.qtodo.service.ServiceBase;
import com.qtodo.service.UserService;
import com.qtodo.service.ValidationService;
import com.qtodo.socket.WebSocketSessionManager;
import com.qtodo.utils.JwtUtils;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserAuthService extends ServiceBase{

	@Autowired
	ValidationService validationService;

	@Autowired
	UserService userService;
	
	@Autowired
	WebSocketSessionManager wsSessionManager;

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
		
		tokens = jwtUtils.generateTokenForUser(userDetails, getUserClaims(ue, userGroup));
		
        ResponseCookie cookie = ResponseCookie.from(getRefTokenKey(userGroup, email), tokens.getRefreshToken())
        		.secure(true)
        		.httpOnly(true)
        		.maxAge(jwtUtils.getJwtRefreshExpirationMs())
				.sameSite("None")
                .build();

        
        tokens.setNewCookie(cookie);
        tokens.setResponseMessage("signed up for usergroup "+tokens.getUserGroup()+" successfully");
        
        return tokens;
	}
	
	public Map<String, Object> getUserClaims(UserEntity ue, String ugTitle){
		UserGroup currUg = userGroupRepo.getByGroupTitle(ugTitle);
		
		Map<String, Object> claims = jwtUtils.getGenericClaimsMap(new UserDto(ue, currUg));
		
		List<UserPermission> permissions = permissionRepo.getEnabledPermByUserEmailAndGroupTitle(ue.getEmail(), ugTitle);
		
		claims.put("permissions", permissions);
		claims.put("user_group_colaboration",currUg.isColaboration());
		claims.put("user_group_open",currUg.isOpen());
		return claims;
	}

	public UserLoginResponse loginUser(UserLoginRequest loginCreds) throws ValidationException {
		String email = loginCreds.getEmail();
		String userGroup = loginCreds.getUserGroup();

		validationService.verifyEmail(email);
		validationService.verifyPassword(loginCreds.getPassword());
		Authentication authentication = null;
		try {
			authentication = authenticationManager.authenticate(
		            new UsernamePasswordAuthenticationToken(
		                loginCreds.getEmail()+"/usergroup/"+userGroup,
		                loginCreds.getPassword()
		            )
		        );
		}catch(BadCredentialsException e) {
			throw ValidationException.failedFor("login attemp for "+userGroup, "Invalid credentials");
		}
		
		CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
		UserEntity ue = user.getUserEntity();
		UserDto userDto;
		userDto = userService.getUserDetailsForUserInUserGroup(ue, userGroup);
		
		TokenResponse tokens = jwtUtils.generateTokenForUser(userDto, getUserClaims(ue, userGroup));

		UserLoginResponse response = new UserLoginResponse(HttpStatus.OK);
		response.setUserDetails(userDto);
		userDto.setAccessToken(tokens.getAccessToken());
		response.setTokens(tokens);
		
        ResponseCookie cookie = ResponseCookie.from(getRefTokenKey(userGroup, email)
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
		
		var ug = userGroupRepo.getByGroupTitle(userGroup);
		boolean userGroupColab = ug.isColaboration();
		boolean userGroupOpen = ug.isOpen();
		
		var permissions = permissionRepo.getEnabledPermByUserEmailAndGroupTitle(email, userGroup);
		
		String alias = (String) refClaims.get("alias");

		UserDto userDto = new UserDto();
		userDto.setEmail(email);
		userDto.setUserGroup(userGroup);
		userDto.setAlias(alias);
		userDto.setUserGroupColaboration(userGroupColab);
		userDto.setUserGroupOpen(userGroupOpen);

		var tokens = jwtUtils.generateTokenForUser(userDto, permissions);
		
		 ResponseCookie cookie = ResponseCookie.from(getRefTokenKey(userGroup, email), tokens.getRefreshToken())
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
		String email = claims.getSubject();
		
		String refToken = null;
		
		for (Cookie cookie : cookies) {
            if (cookie.getName().equals(getRefTokenKey(userGroup, email))) {
            	refToken = cookie.getValue();
            }
        }
		
		if(refToken == null) throw ValidationException.failedFor("refresh_token", "not found for user group "+userGroup);
		
		return refToken;
	}

	public ResponseCookie clearRefreshTokenForUserGroupFromCookies(String sessionToken) throws ValidationException {
		
		Claims claims = jwtUtils.getUserClaimsFromExpiredToken(sessionToken);
		
		String userGroup = (String)claims.get("user_group");
		String email = claims.getSubject();
		
        return ResponseCookie.from(getRefTokenKey(userGroup, email), null)
        		.path("/")
        		.secure(true)
        		.httpOnly(true)
        		.maxAge(jwtUtils.getJwtRefreshExpirationMs())
				.sameSite("None")
                .build();
	}
	
	

	public List<UserDto> getAllUsersInCurrentUserGroup() {
		var ug = getAuthenticatedUsersUserGroup();
		var participants = ug.getParticipantUsers();
		
		var userDetailsForAllParticipants = participants.stream().map(participant->{
			var ud = new UserDto(participant, ug);
			
			ud.setProfilePicture(userService.getProfilePicUrlForUser(participant.getId(), ug.getGroupTitle()));
			
			var permissions = permissionRepo.getEnabledPermByUserEmailAndGroupTitle(participant.getEmail(), ug.getGroupTitle());
			
			ud.setPermissions(permissions);
			
			if(ug.isColaboration()) {
				var session = wsSessionManager.getWebSocketInGroupSession(ug.getGroupTitle(), participant.getEmail());
				if(session.isPresent()) {
					ud.setOnlineInUg(true);
				}
			}
			
			return ud;
		}).collect(Collectors.toList());
		
		return userDetailsForAllParticipants;
	}

	public ArrayList<UserPermission> updateUserPermissions(List<UserPermissionUpdateRequest> permissionChangeRequest) throws ValidationException {
		
		var ug = getAuthenticatedUsersUserGroup();
		
		var changedPermissions = new ArrayList<UserPermission>();
		var newPermissions = new ArrayList<UserPermission>();
		
		try {
			permissionChangeRequest.forEach(req->{
				var permissions = permissionRepo.getByUserEmailAndGroupTitle(req.getUserEmail(), ug.getGroupTitle());
				permissions.forEach(p->{
					var perm = req.getUserPermission();
					if(p.getPermission().equals(UserPermission.valueOf(perm))) {
						p.setEnabled(req.isEnabled());
						permissionRepo.save(p);
						changedPermissions.add(p.getPermission());
					}
				});
			});
			if(changedPermissions.size() != permissionChangeRequest.size()) {
				var changed = new HashSet(changedPermissions);
				permissionChangeRequest.forEach(req->{
					var perm = UserPermission.valueOf(req.getUserPermission());
					if(!changed.contains(perm)) {
						var permission = new PermissionForUserInUserGroup();
						permission.setEnabled(true);
						permission.setPermission(perm);
						permission.setUser(userRepo.getByEmailInUserGroup(req.getUserEmail(), ug.getGroupTitle()));
						permission.setUserGroup(ug);
						permissionRepo.save(permission);
						newPermissions.add(perm);
					}
				});
			}
		}catch(IllegalArgumentException e) {
			// incase the permission enum does not match input value
			throw ValidationException.failedFor("update user permission", e.getMessage());
		}
		
	    if(changedPermissions.size() == 0 && newPermissions.size() == 0) return null;	
	    
		return newPermissions;
	}

	public boolean setUserPermissionsForRole(String userEmail, UserRole role) throws ValidationException {
		
		var ug = getAuthenticatedUsersUserGroup();
		
		var permissionForRole = jwtUtils.getPermissionsForUserRole(role);
		
		var disabledPermissions = new ArrayList<>();
		var enabledPermissions = new ArrayList<>();
	
		var user = getAuthenticatedUser();
		
		if(user == null) {
			throw ValidationException.failedFor("set user role", "no user with this email in usergroup");
		}
		
		var permissions = permissionRepo.getByUserEmailAndGroupTitle(userEmail, ug.getGroupTitle());
		permissions.forEach(p->{
			p.setEnabled(false);
		});
		
		permissionForRole.forEach(p->{
			var permission = new PermissionForUserInUserGroup();
			permission.setEnabled(true);
			permission.setUser(user);
			permission.setUserGroup(ug);
			permission.setPermission(p);
			permissionRepo.save(permission); // save or update
		});
		
		
		return true;
	}

	public boolean enableDisableUserGroup() {
		var ug = getAuthenticatedUsersUserGroup();
		
		var enabled = false;
		if(ug.isDeleted()) {
			enabled = true;
			ug.setDeleted(false);
		}else {
			ug.setDeleted(true);
		}
		
		return enabled;
	}

	public void updateUserGroupDetails(UserGroupUpdateRequest ugUpdateReq) {
		var ug = getAuthenticatedUsersUserGroup();
		
		var isColab = ugUpdateReq.getColaboration();
		var isOpen = ugUpdateReq.getOpen();
		
		if(isColab != null)
		ug.setColaboration(isColab);
		
		if(isOpen != null)
		ug.setOpen(isOpen);
	
	}
	
	private String getRefTokenKey(String userGroup, String email) {
		String key = "refresh_token_for_"+userGroup+"_"+email.replaceAll("[.@]","_");
		return key;
	}

	public List<String> getOpenUserGroupTitles() {
		return userGroupRepo.findAllOpen().stream().map(ug->ug.getGroupTitle()).collect(Collectors.toList());
	}

}
