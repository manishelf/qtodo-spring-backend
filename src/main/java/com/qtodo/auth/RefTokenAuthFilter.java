package com.qtodo.auth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.qtodo.utils.JwtUtils;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
//
//@Component
//public class RefTokenAuthFilter extends OncePerRequestFilter {
//
//	@Autowired
//	JwtUtils jwtUtils;
//
//	@Autowired
//	UserRepo userRepo;
//
//	@Override
//	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//			throws ServletException, IOException {
//		
//		if(SecurityContextHolder.getContext().getAuthentication() != null) {
//			filterChain.doFilter(request, response);
//			return;
//		}
//		
//		Cookie[] cookies = request.getCookies();
//		String email = null;
//		Claims claims = null;
//		
//		cookies = request.getCookies();
//
//		if (cookies == null)
//			return;
//		
//		Map<String, String> refTokenMap = new HashMap();
//		
//		for(Cookie cookie : cookies) {
//			var name = cookie.getName();
//			var val = cookie.getValue();
//			
//			if(name.startsWith("refresh_token_for_"))
//			refTokenMap.put(name, (String) val);
//		}
//
//		var anyToken = refTokenMap.entrySet().stream().findFirst();
//		
//		if(anyToken.isEmpty()) return;
//		
//		claims = jwtUtils.getUserClaimsFromJwtToken(anyToken.get().getValue());
//
//		email = claims.getSubject();
//
//		if (email != null) {
//
//			String userGroup = (String) claims.get("user_group");
//			List<String> permissions = (List<String>) claims.get("permissions");
//			List<String> roles = (List<String>) claims.get("roles");
//
//			List<GrantedAuthority> authorities = new ArrayList<>();
//			if (roles != null) {
//				roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
//			}
//			if (permissions != null) {
//				permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
//			}
//
//			UserEntity userEntity = userRepo.getByEmailInUserGroup(email, userGroup);
//
//			if (userEntity == null)
//				return;
//
//			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userEntity, null,
//					authorities);
//
//			authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//
//			SecurityContextHolder.getContext().setAuthentication(authToken);
//
//		}
//	}
//
//}
