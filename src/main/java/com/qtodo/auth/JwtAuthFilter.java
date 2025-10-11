package com.qtodo.auth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.qtodo.dao.UserRepo;
import com.qtodo.model.UserEntity;
import com.qtodo.response.ValidationException;
import com.qtodo.utils.JwtUtils;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter{

	@Autowired
	JwtUtils jwtUtils;
	
    @Autowired
    UserRepo userRepo;
    
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;
        Claims claims = null;
        
        System.out.println(request.getRequestURI());
        
        if (authHeader != null && authHeader.length()>7 && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            if(jwtUtils.isTokenExpired(token)) {
            	filterChain.doFilter(request, response);
            	return;
            }
            try {
				claims = jwtUtils.getUserClaimsFromJwtToken(token);
			} catch (ValidationException e) {
				throw new ServletException(e.getMessage());
			}
            email = claims.getSubject();
        }
       
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            String userGroup = (String) claims.get("user_group");
            List<String> permissions = (List<String>) claims.get("permissions");
            
            List<GrantedAuthority> authorities = new ArrayList<>();
            
            if (permissions != null) {
                permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
            }
                        
            UserEntity userEntity = userRepo.getByEmailInUserGroup(email, userGroup);
            
    		if(userEntity == null) {
    			filterChain.doFilter(request, response);
    			return;
    		}
    		
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            		new CustomUserDetails(userEntity, userGroup, authorities)
            		, null, authorities);
            
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
            SecurityContextHolder.getContext().setAuthentication(authToken);
            
        }
        
        filterChain.doFilter(request, response);
		
	}

}
