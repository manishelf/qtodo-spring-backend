package com.qtodo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.qtodo.response.ApiResponseBase;
import com.qtodo.response.ValidationException;

public class GlobalExceptionHandler {
	
	@ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponseBase> handleValidationException(ValidationException ve) {
		System.out.println(ve.getMessage());
        return ApiResponseBase.asWrapped(new ApiResponseBase(ve.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseBase> handleGenericException(Exception ex) {
		System.out.println(ex.getMessage());
        return ApiResponseBase.asWrapped(new ApiResponseBase("An unexpected error occurred. - "+ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
