package com.qtodo.auth;

import java.time.Duration;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    JwtAuthFilter jwtAuthFilter;
    
    @Autowired
    UrlTokenAuthFilter urlAuthFilter;
    
    @Autowired
    UserDetailsServiceImpl userDetailsServiceImpl;
    
    @Value("${qtodo.app.frontend.host.domain}")
    String frontendDomain;
    
 	String[] allowPaths = {
 				"/qtodo-h2-console/**",
    			"/ang/**",
    			"/proto/**",
    			"/up","/user/usergroups","/user/login", "/user/signup", "/user/refresh","/user/logout",
    			};
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
    
    @Bean
    @Order(1)
    SecurityFilterChain urlTokenSecurityFilterChain(HttpSecurity http) throws Exception {
        http
             // This filter only applies to H2 console and checks if any refresh token available
        	.csrf(csrf -> csrf.disable())
        	.securityMatcher("/qtodo-h2-console/**","/swagger-ui/**", "/v3/api-docs/**", "/ws/**" , "/item/doc/**"
        			) // h2 uses java serverlets which I cant intercept 
            .authorizeHttpRequests(authorize -> authorize
                    .anyRequest().authenticated()
                )
            .addFilterBefore(urlAuthFilter, UsernamePasswordAuthenticationFilter.class)                    
            .authenticationProvider(authenticationProvider())       
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )            
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
            .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }   

    @Bean
    @Order(2)
    SecurityFilterChain jwtSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf->csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
            		.requestMatchers(allowPaths).permitAll()
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
       
        if(this.frontendDomain != null) {
        	configuration.setAllowedOrigins(Arrays.asList(frontendDomain.split(",")));
        	System.out.println("Allowing frontend Domains - "+frontendDomain);
        }
        else configuration.setAllowedOrigins(Arrays.asList("*"));
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE"));        
        
        configuration.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization"));
        
        configuration.setMaxAge(Duration.ofMinutes(5L));
        
        if(this.frontendDomain != null)
        configuration.setAllowCredentials(true); // set the allowedOrigins to specific domains
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
	
}
