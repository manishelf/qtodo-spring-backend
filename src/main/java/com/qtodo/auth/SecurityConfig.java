package com.qtodo.auth;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Autowired
    JwtAuthFilter jwtAuthFilter;
    
//    @Autowired
//    RefTokenAuthFilter h2AuthFilter;
    
    @Autowired
    UserDetailsServiceImpl userDetailsServiceImpl;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsServiceImpl);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
//    @Bean
//    SecurityFilterChain h2ConsoleSecurityFilterChain(HttpSecurity http) throws Exception {
//        http
//             // This filter only applies to H2 console and checks if any refresh token available
//        	.securityMatcher("/qtodo-h2-console/**")
//            .authorizeHttpRequests(authorize -> authorize
//                    .anyRequest().authenticated()
//                )
//            .addFilterBefore(h2AuthFilter, UsernamePasswordAuthenticationFilter.class)                    
//            .authenticationProvider(authenticationProvider())       
//            .sessionManagement(session -> session
//                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )            
//            .csrf(csrf -> csrf.disable())
//            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));
//
//        return http.build();
//    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf->csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers(
                    		"/up","/user/login", "/user/signup", "/user/refresh",
                    		"/qtodo-h2-console/**","/swagger-ui/**", "/v3/api-docs/**"
                    		).permitAll()
                    .anyRequest().authenticated()
                )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)                    
            .authenticationProvider(authenticationProvider())       
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
            .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        configuration.setAllowedOrigins(Arrays.asList("*"));
        
        configuration.setAllowedMethods(Arrays.asList("*"));
        
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        configuration.setMaxAge(Duration.ofMinutes(5L));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
	
}
