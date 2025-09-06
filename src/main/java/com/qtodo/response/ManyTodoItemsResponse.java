package com.qtodo.response;

import java.util.List;

import org.springframework.http.HttpStatusCode;
import org.springframework.util.MultiValueMap;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManyTodoItemsResponse extends ApiResponseBase {
	
	List<TodoItemDto> items;

	public ManyTodoItemsResponse(HttpStatusCode status) {
		super(status);
	}

	public ManyTodoItemsResponse(List<TodoItemDto> items, HttpStatusCode status) {
		super(status);
		this.items = items;
	}

	public ManyTodoItemsResponse(List<TodoItemDto> items, MultiValueMap<String, String> headers,
			HttpStatusCode statusCode) {
		super(headers, statusCode);
		this.items = items;
	}

	public ManyTodoItemsResponse(MultiValueMap<String, String> headers, HttpStatusCode status) {
		super(headers, status);
	}
	
}
