package com.qtodo.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.qtodo.auth.UserPermissions;
import com.qtodo.auth.UserRoles;
import com.qtodo.dto.UserDto;
import com.qtodo.response.TokenResponse;
import com.qtodo.response.ValidationException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.Getter;

@Component
@Getter
public class JwtUtils {

	SecretKey key = Jwts.SIG.HS256.key().build();

	@Value("${qtodo.app.jwtSessionExpirationMs}")
	long jwtSessionExpirationMs;

	@Value("${qtodo.app.jwtRefreshExpirationMs}")
	long jwtRefreshExpirationMs;
	
	public TokenResponse generateTokenForUser(UserDto userDetails) {
		
		String sessionToken = Jwts.builder()
				.subject(userDetails.getEmail())
				.claims(getClaimsMap(userDetails))
				.issuedAt(new Date())
				.expiration(new Date((new Date()).getTime() + jwtSessionExpirationMs))
				.signWith(key)
				.compact();
		
		String refreshToken = issueRefreshTokenForUser(userDetails.getEmail(), userDetails.getUserGroup());
		
		return new TokenResponse(userDetails.getUserGroup(), sessionToken, refreshToken);
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

	public String issueRefreshTokenForUser(String email, String userGroup) {
		return Jwts.builder()
				.subject(email)
				.claim("user_group",userGroup)
				.issuedAt(new Date())
				.expiration(new Date((new Date()).getTime() + jwtRefreshExpirationMs))
				.signWith(key)
				.compact();
	}

	public Map<String, Object> getClaimsMap(UserDto userDetails) {
		Map<String, Object> claims = new HashMap();

		UserRoles roles[] = { UserRoles.AUTHOR, UserRoles.AUDIENCE , UserRoles.ADMIN };

		UserPermissions permissions[] = { UserPermissions.READ, UserPermissions.WRITE, UserPermissions.EDIT,
				UserPermissions.DELETE , UserPermissions.SERVER_TOOLS};

		claims.put("email", userDetails.getEmail());
		claims.put("user_group", userDetails.getUserGroup());
		claims.put("first_name", userDetails.getFirstName());
		claims.put("last_name", userDetails.getLastName());
		claims.put("roles", roles);
		claims.put("permissions", permissions);

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

}
