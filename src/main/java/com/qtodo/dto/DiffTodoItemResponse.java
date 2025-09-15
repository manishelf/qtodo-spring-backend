package com.qtodo.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseCookie;

import com.qtodo.response.ApiResponseBase;
import com.qtodo.response.TodoItemDto;

import jakarta.servlet.http.Cookie;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiffTodoItemResponse extends ApiResponseBase {
	
	List<TodoItemDto> itemsForAdd = new ArrayList<>();
	List<TodoItemDto> itemsForUpdate = new ArrayList<>();
	List<TodoItemState> itemsForDelete = new ArrayList<>();
	List<TodoItemState> itemsForSync = new ArrayList<>();

	public DiffTodoItemResponse(HttpStatusCode status) {
		super(status);
	}
	
	public DiffTodoItemResponse(String responseMessage, HttpStatus status, Cookie[] existingCookie,
			ResponseCookie newCookie) {
		super(responseMessage, status, existingCookie, newCookie);
	}

	public DiffTodoItemResponse(String responseMessage, HttpStatusCode status) {
		super(responseMessage, status);
	}
	
}
