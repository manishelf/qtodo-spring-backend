package com.qtodo.utils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.qtodo.auth.UserPermission;
import com.qtodo.auth.UserRole;
import com.qtodo.dto.UserDto;
import com.qtodo.response.TokenResponse;
import com.qtodo.response.ValidationException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.Getter;

@Component
@Getter
public class JwtUtils {

	@Value("${qtodo.app.jwtSessionExpirationMs}")
	int jwtSessionExpirationMs;

	@Value("${qtodo.app.jwtRefreshExpirationMs}")
	long jwtRefreshExpirationMs;
	
	SecretKey key;

	static List<UserPermission> genericPermissions = List.of(UserPermission.READ); 
	
	public JwtUtils(@Value("${qtodo.app.jwtSecret:none}") String secret) {
		if(!"none".equals(secret)) {
			this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		}else {
			this.key = Jwts.SIG.HS256.key().build();
		}
	}
	
	public TokenResponse generateTokenForUser(UserDto userDetails, Map<String,Object> claims) {
		
		String sessionToken = Jwts.builder()
				.subject(userDetails.getEmail())
				.claims(claims)
				.issuedAt(new Date())
				.expiration(new Date((new Date()).getTime() + jwtSessionExpirationMs))
				.signWith(key)
				.compact();
		
		String refreshToken = issueRefreshTokenForUser(userDetails.getEmail(), userDetails.getUserGroup(),
														userDetails.isUserGroupOpen(), userDetails.isUserGroupColaboration());
		
		return new TokenResponse(userDetails.getUserGroup(), sessionToken, refreshToken);
	}
	
	public TokenResponse generateTokenForUser(UserDto userDetails) {
		return generateTokenForUser(userDetails, getGenericClaimsMap(userDetails));
	}
	
	public TokenResponse generateTokenForUser(UserDto userDetails, List<UserPermission> permissions) {
		var claims = getGenericClaimsMap(userDetails);
		claims.put("permissions", permissions);
		return generateTokenForUser(userDetails, claims);
	}

	public Claims getUserClaimsFromJwtToken(String token) throws ValidationException {
	    try {
	        Jwts.parser()
	            .verifyWith(key)
	            .build()
	            .parseSignedClaims(token);

	        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
		
	    } catch (SignatureException e) {
	        throw ValidationException.failedFor("token", "Invalid JWT signature.");
	    } catch (MalformedJwtException e) {
	        throw ValidationException.failedFor("token", "Invalid JWT token.");
	    } catch (ExpiredJwtException e) {
	        throw ValidationException.failedFor("token", "JWT token is expired.");
	    } catch (UnsupportedJwtException e) {
	        throw ValidationException.failedFor("token", "JWT token is unsupported.");
	    } catch (IllegalArgumentException e) {
	        throw ValidationException.failedFor("token", "JWT claims string is empty.");
	    } catch (JwtException e) {
	        throw ValidationException.failedFor("token", "Failed to parse JWT token.");
	    }
	}

	public String issueRefreshTokenForUser(String email, String userGroup, boolean userGroupOpen, boolean userGroupColab) {
		return Jwts.builder()
				.subject(email)
				.claim("user_group",userGroup)
				.issuedAt(new Date())
				.expiration(new Date((new Date()).getTime() + jwtRefreshExpirationMs))
				.signWith(key)
				.compact();
	}

	public Map<String, Object> getGenericClaimsMap(UserDto userDetails) {
		Map<String, Object> claims = new HashMap();


		claims.put("email", userDetails.getEmail());
		claims.put("user_group", userDetails.getUserGroup());
		claims.put("user_group_open", userDetails.isUserGroupOpen());
		claims.put("user_group_colaboration", userDetails.isUserGroupColaboration());
		claims.put("alias", userDetails.getAlias());
		claims.put("permissions", genericPermissions);

		return claims;
	}

	public Claims getUserClaimsFromExpiredToken(String expiredToken) throws ValidationException {
		try {
	        return Jwts.parser().verifyWith(key).build().parseSignedClaims(expiredToken).getPayload();
	    } catch (ExpiredJwtException e) {
	        return e.getClaims();
	    } catch (SignatureException e) {
	        throw ValidationException.failedFor("token", "Invalid JWT signature.");
	    } catch (MalformedJwtException e) {
	        throw ValidationException.failedFor("token", "Invalid JWT token.");
	    } catch (UnsupportedJwtException e) {
	        throw ValidationException.failedFor("token", "JWT token is unsupported.");
	    } catch (IllegalArgumentException e) {
	        throw ValidationException.failedFor("token", "JWT claims string is empty.");
	    } catch (JwtException e) {
	        throw ValidationException.failedFor("token", "Failed to parse JWT token.");
	    }
	}

	public boolean isTokenExpired(String token){
		try {			
			Claims claim = this.getUserClaimsFromJwtToken(token);
		}catch(ValidationException ve) {
			return true;
		}
		return false;
	}

	public static List<UserPermission> getPermissionsForUserRole(UserRole role) {
		switch(role) {
			case ADMIN:
				return List.of(
						UserPermission.SERVER_TOOLS, 
						UserPermission.CHANGE_UG_CONFIG,
						UserPermission.REMOVE_PARTICIPANT,
						UserPermission.ADD_PARTICIPANTS,
						UserPermission.MANAGE_PARTICIPANT_PERMISSIONS,
						UserPermission.ENABLE_DISABLE_UG
						);
			case AUTHOR:
				return List.of(
						UserPermission.READ,
						UserPermission.WRITE,
						UserPermission.EDIT,
						UserPermission.SHARE,
						UserPermission.DELETE,
						UserPermission.GET_DOCUMENT
						);
			case AUDIENCE:
				return List.of(
						UserPermission.READ,
						UserPermission.GET_DOCUMENT
						);
			case COLLABORATOR: 
				return List.of(
						UserPermission.READ,
						UserPermission.GET_DOCUMENT,
						UserPermission.WRITE, 
						UserPermission.SHARE,
						UserPermission.EDIT,
						UserPermission.DELETE,
						UserPermission.COLAB
						);
			case UG_OWNER:
				return List.of(
						UserPermission.ADD_PARTICIPANTS,
						UserPermission.REMOVE_PARTICIPANT,
						UserPermission.MANAGE_PARTICIPANT_PERMISSIONS,
						UserPermission.CHANGE_UG_CONFIG,
						UserPermission.ENABLE_DISABLE_UG
						);
			default: 
				return genericPermissions;
		}
	}

}
