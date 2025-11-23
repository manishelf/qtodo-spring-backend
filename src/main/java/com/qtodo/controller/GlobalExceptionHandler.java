package com.qtodo.controller;


import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.qtodo.response.ApiResponseBase;
import com.qtodo.response.ValidationException;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
	
	Logger logger = LogManager.getLogger("Exception Handler");
	
	@ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponseBase> handleValidationException(ValidationException ve) {
//		ve.printStackTrace();
		logger.error("Validation exception ", ve);
        return ApiResponseBase.asWrapped(new ApiResponseBase(ve.getMessage(), HttpStatus.BAD_REQUEST));
    }
	
	@ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponseBase> handleAccessDeniedException(AuthorizationDeniedException ex) {
//    	ex.printStackTrace();
    	logger.error("Auth Exception", ex);
        return ApiResponseBase.asWrapped(new ApiResponseBase("Unable to process request "+ex.getMessage(), HttpStatus.FORBIDDEN));
    }

    
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity handleResourceNotFound(NoResourceFoundException ex, HttpServletRequest request) {
        logger.error("Resource not found: "+ ex.getMessage());
        String requestUrl = request.getRequestURI();
        
        if (requestUrl.startsWith("/ang/")) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", "/ang/index.html");

            // Return a 302 redirect to /ang/index.html
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);  
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseBase> handleGenericException(Exception ex) {
    	ex.printStackTrace();
    	logger.error("Generic Exception", ex);
        return ApiResponseBase.asWrapped(new ApiResponseBase("An unexpected error occurred. - "+ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
