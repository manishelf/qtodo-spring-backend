package com.qtodo.auth;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.qtodo.dao.UserRepo;
import com.qtodo.model.UserEntity;
import com.qtodo.response.ValidationException;
import com.qtodo.utils.JwtUtils;

import io.jsonwebtoken.Claims;

@Component
public class WSJwtInterceptor implements ChannelInterceptor {
	

    @Autowired
    UserRepo userRepo;
    
	@Autowired
	JwtUtils jwtUtils;
    
	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel){
		StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		System.out.println("hello");
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            System.out.println(token);
            Authentication authentication;
			try {
				authentication = validateTokenAndGetAuthentication(token);
				accessor.setUser(authentication);
			} catch (ValidationException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
        }
	
		return message;
	}
	
	private Authentication validateTokenAndGetAuthentication(String token) throws ValidationException {
		if(token == null)
			return null;
		
		if(jwtUtils.isTokenExpired(token))
			return null;
		
		Claims claims = jwtUtils.getUserClaimsFromJwtToken(token);
		
		String email = claims.getSubject();
		
		if(email == null) 
			return null;
		
		String userGroup = (String) claims.get("user_group");
        List<String> permissions = (List<String>) claims.get("permissions");
        List<String> roles = (List<String>) claims.get("roles");
        
        
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (roles != null) {
            roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
        }
        if (permissions != null) {
            permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
        }
        
        UserEntity userEntity = userRepo.getByEmailInUserGroup(email, userGroup);
        if(userEntity == null) {
        	return null;
		}
        System.out.println(userGroup);
        
        return new UsernamePasswordAuthenticationToken(new CustomUserDetails(userEntity), userGroup, authorities);
    }

}
