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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class urlTokenAuthFilter extends OncePerRequestFilter {

	@Autowired
	JwtUtils jwtUtils;

	@Autowired
	UserRepo userRepo;

	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		if(SecurityContextHolder.getContext().getAuthentication() != null) {
			filterChain.doFilter(request, response);
			return;
		}
		
		String token = request.getParameter("sessionToken");
		
		Cookie cookies[] = request.getCookies();
		
		if(cookies != null && cookies.length>0 && token == null) {
			for(Cookie cookie : cookies) {
				if(cookie.getName().equals("token_for_url")) {
					token = cookie.getValue();
				}
			}
		}
		
        		
		if(token == null) {
			filterChain.doFilter(request, response);
			return;
		}
	
		
		Claims claims;
		try {
			claims = jwtUtils.getUserClaimsFromJwtToken(token);
		} catch (ValidationException e) {
			throw new ServletException(e.getMessage());
		}

		String email = claims.getSubject();
		
		boolean hasPermission = false;

		
		if (email != null) {
			
			String userGroup = (String) claims.get("user_group");
			List<String> permissions = (List<String>) claims.get("permissions");
			

			UserEntity userEntity = userRepo.getByEmailInUserGroup(email, userGroup);

			if (userEntity == null) {
				filterChain.doFilter(request, response);
				return;
			}
			
			if (permissions != null) {
				for(var perm: permissions) {
					if(perm.equals(UserPermissions.SERVER_TOOLS.toString()) 
						|| perm.equals(UserPermissions.COLAB.toString())) {
						hasPermission = true;
					}
				}
			}
			
			if(hasPermission) {
	            List<String> roles = (List<String>) claims.get("roles");
	            
	            
	            List<GrantedAuthority> authorities = new ArrayList<>();
	            
	            if (roles != null) {
	                roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
	            }
	            
	            if (permissions != null) {
	                permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
	            }


				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
						new CustomUserDetails(userEntity, userGroup),
						null, authorities);
	            
	            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
	                
	            SecurityContextHolder.getContext().setAuthentication(authToken);
	            		
	            Cookie cookie = new Cookie("token_for_url",token);
	            cookie.setHttpOnly(true);
	            cookie.setSecure(true);
	            cookie.setMaxAge(jwtUtils.getJwtSessionExpirationMs());
	            cookie.setPath(request.getRequestURI().split("\\?sessionToken")[0]);
	            
	            response.addCookie(cookie);
			}
		}
		filterChain.doFilter(request, response);
	}

}
