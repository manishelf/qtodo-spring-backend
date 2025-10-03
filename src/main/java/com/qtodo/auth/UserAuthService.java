package com.qtodo.auth;

import java.util.ArrayList;
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

@Service
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
		
		Map<String, Object> claims = jwtUtils.getGenericClaimsMap(new UserDto(ue, currUg));
		
		if(!owns.isEmpty() && owns.contains(currUg)) {
			List<UserPermission> permissions = new ArrayList<>();
			permissions.addAll(jwtUtils.getPermissionsForUserRole(UserRole.UG_OWNER));
			permissions.addAll(jwtUtils.getPermissionsForUserRole(UserRole.AUTHOR));
			claims.put("permissions", permissions);
			claims.put("roles", List.of(UserRole.UG_OWNER, UserRole.AUTHOR, UserRole.ADMIN));
		}	
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
		
		var ug = userGroupRepo.getByGroupTitle(userGroup);
		boolean userGroupColab = ug.isColaboration();
		boolean userGroupOpen = ug.isOpen();
		
		var permissions = permissionRepo.getByUserEmailAndGroupTitle(email, userGroup).stream()
				.filter(p->!p.isEnabled()).map(p->p.getPermission()).collect(Collectors.toList());
		
		var roles = (List<UserRole>)claims.get("roles");
		
		String alias = (String) refClaims.get("alias");

		UserDto userDto = new UserDto();
		userDto.setEmail(email);
		userDto.setUserGroup(userGroup);
		userDto.setAlias(alias);
		userDto.setRoles(roles);
		userDto.setUserGroupColaboration(userGroupColab);
		userDto.setUserGroupOpen(userGroupOpen);

		var tokens = jwtUtils.generateTokenForUser(userDto, permissions);
		
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
	public List<UserDto> getAllUsersInCurrentUserGroup() {
		var ug = userGroupRepo.getByGroupTitle(getAuthenticatedUsersUserGroup().getGroupTitle());
		var participants = ug.getParticipantUsers();
		
		var userDetailsForAllParticipants = participants.stream().map(participant->{
			var ud = new UserDto(participant, ug);
			
			ud.setProfilePicture(userService.getProfilePicUrlForUser(participant.getId(), ug.getGroupTitle()));
			
			var permissions = permissionRepo.getByUserEmailAndGroupTitle(participant.getEmail(), ug.getGroupTitle())
						.stream().filter(perm->!perm.isEnabled()).map(perm->perm.getPermission()).collect(Collectors.toList());
			
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

	public boolean updateUserPermissions(List<UserPermissionUpdateRequest> permissionChangeRequest) throws ValidationException {
		var permissionsForCurrUser = getAuthenticatedUsersPermissions();
		if(!permissionsForCurrUser.contains(UserPermission.MANAGE_PARTICIPANT_PERMISSIONS)) {
			throw ValidationException.failedFor("update user permissions", "no permission to UPDATE permissions");
		}
		
		var ug = getAuthenticatedUsersUserGroup();
		
		var changedPermissions = new ArrayList<>();
		
		permissionChangeRequest.forEach(req->{
			var permissions = permissionRepo.getByUserEmailAndGroupTitle(req.getUserEmail(), ug.getGroupTitle());
			permissions.forEach(p->{
				if(p.getPermission().equals(req.getUserPermission())) {
					p.setEnabled(req.isEnabled());
					changedPermissions.add(p.getPermission());
				}
			});
		});
		
		return permissionChangeRequest.size() == changedPermissions.size();
	}

	public boolean setUserPermissionsForRole(String userEmail, UserRole role) throws ValidationException {
		
		var permissionsForCurrentUser = getAuthenticatedUsersPermissions();
		if(!permissionsForCurrentUser.contains(UserPermission.MANAGE_PARTICIPANT_PERMISSIONS)) {
			throw ValidationException.failedFor("update user permissions", "no permission to UPDATE permissions");
		}
		
		var ug = getAuthenticatedUsersUserGroup();
		
		var permissionForRole = jwtUtils.getPermissionsForUserRole(role);
		
		var disabledPermissions = new ArrayList<>();
		var enabledPermissions = new ArrayList<>();
		
	
		var user = userRepo.getByEmailInUserGroup(userEmail, ug.getGroupTitle());
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

}
