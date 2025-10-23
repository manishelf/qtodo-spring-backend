package com.qtodo.controller;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.qtodo.response.ApiResponseBase;
import com.qtodo.response.ValidationException;

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseBase> handleGenericException(Exception ex) {
    	ex.printStackTrace();
    	logger.error("Generic Exception", ex);
        return ApiResponseBase.asWrapped(new ApiResponseBase("An unexpected error occurred. - "+ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
