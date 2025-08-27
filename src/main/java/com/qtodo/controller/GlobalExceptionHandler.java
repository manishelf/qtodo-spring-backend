package com.qtodo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.qtodo.response.ApiResponseBase;
import com.qtodo.response.ValidationException;

@ControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponseBase> handleValidationException(ValidationException ve) {
		ve.printStackTrace();
        return ApiResponseBase.asWrapped(new ApiResponseBase(ve.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseBase> handleGenericException(Exception ex) {
    	ex.printStackTrace();
        return ApiResponseBase.asWrapped(new ApiResponseBase("An unexpected error occurred. - "+ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
