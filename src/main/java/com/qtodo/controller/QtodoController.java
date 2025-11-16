package com.qtodo.controller;

import java.time.Duration;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class QtodoController {
	
	protected LocalDateTime startupTime;

	protected Logger logger = LogManager.getLogger("com.qtodo.controller");
	
	public QtodoController(){
		startupTime = LocalDateTime.now();
	}
	
	@GetMapping("/up")
	public ResponseEntity<String> sanity(){
		return new ResponseEntity<>("QTODO-SERVER-UP-TIME-"+ Duration.between(startupTime,LocalDateTime.now()).toSeconds()+"S" , HttpStatus.OK);
	}
}
