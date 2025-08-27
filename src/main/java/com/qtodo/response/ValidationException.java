package com.qtodo.response;

import lombok.Getter;

@Getter
public class ValidationException extends Exception{
	
	String field;
	String reason;
	
	public ValidationException() {
		super();
	}
	public ValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}
	public ValidationException(String message) {
		super(message);
	}
	public ValidationException(Throwable cause) {
		super(cause);
	}
	
	public static ValidationException failedFor(String field, String reason) {
		var e = new ValidationException();
		e.field = field;
		e.reason = reason;
		return e;
	}
	
	@Override
	public String getMessage() {
		return "validation failed for "+field+" reason "+reason;
	}
}
