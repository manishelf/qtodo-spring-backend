package com.qtodo.response;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.servlet.http.Cookie;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ApiResponseBase {

	LocalDateTime timeStamp = LocalDateTime.now();

	String responseMessage;

	HttpStatusCode status;

	@JsonIgnore
	ResponseCookie newCookie;

	@JsonIgnore
	Cookie[] existingCookies;

	Object body;

	public ApiResponseBase(HttpStatusCode status) {
		this.status = status;
	}

	public ApiResponseBase(Object body, HttpStatusCode status) {
		this(status);
		this.body = body;
	}

	public ApiResponseBase(String responseMessage, HttpStatusCode status) {
		this(status);
		this.responseMessage = responseMessage;
	}

	public ApiResponseBase(String responseMessage, HttpStatus status, Cookie[] existingCookie,
			ResponseCookie newCookie) {
		this(responseMessage, status);
		this.existingCookies = existingCookie;
		this.newCookie = newCookie;
	}

	public static <T extends ApiResponseBase> ResponseEntity<T> asWrapped(T response) {
		if ((response.newCookie != null || response.existingCookies != null) && response.status.equals(HttpStatus.OK)) {
			var resp = ResponseEntity.status(response.status);
			
			Map<String, ResponseCookie> existingCookies = new HashMap();
			
			if (response.existingCookies != null) {

				for (Cookie cookie : response.existingCookies) {
					if(cookie.getName().startsWith("refresh_token_for_")) {	
						var respCookie = ResponseCookie.from(cookie.getName(), cookie.getValue())
								.path("/")
								.secure(true)
								.httpOnly(true)
								.value(cookie.getValue())
								.build();
						
						existingCookies.put(cookie.getName(),respCookie);
					}
				}
			}
			
			existingCookies.put(response.newCookie.getName(), response.getNewCookie());
			
			for(Map.Entry<String, ResponseCookie> ent : existingCookies.entrySet()) {
				resp.header(HttpHeaders.SET_COOKIE, ent.getValue().toString());
			}

			return resp.body(response);
		}
		return new ResponseEntity<>(response, response.status);
	}
}
