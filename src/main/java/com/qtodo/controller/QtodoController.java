package com.qtodo.controller;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qtodo.auth.UserAuthService;
import com.qtodo.utils.JwtUtils;

@RestController
@RequestMapping("/")
public class QtodoController {
	
	LocalDateTime startupTime;
	
	@Autowired
	UserAuthService userAuthService;
	
	@Autowired
	JwtUtils jwtUtils;
	
	public QtodoController(){
		startupTime = LocalDateTime.now();
	}
	
	@GetMapping("/up")
	public ResponseEntity<String> sanity(){
		return new ResponseEntity<>("QTODO-SERVER-UP-TIME-"+ Duration.between(startupTime,LocalDateTime.now()).toSeconds()+"S" , HttpStatus.OK);
	}
}
