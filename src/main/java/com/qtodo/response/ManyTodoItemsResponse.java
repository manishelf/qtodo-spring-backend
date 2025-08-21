package com.qtodo.response;

import java.util.List;

import org.springframework.http.HttpStatusCode;
import org.springframework.util.MultiValueMap;

import com.qtodo.model.TodoItem;

public class ManyTodoItemsResponse extends ApiResponseBase {
	
	List<TodoItem> items;

	public ManyTodoItemsResponse(HttpStatusCode status) {
		super(status);
	}

	public ManyTodoItemsResponse(List<TodoItem> items, HttpStatusCode status) {
		super(status);
		this.items = items;
	}

	public ManyTodoItemsResponse(List<TodoItem> items, MultiValueMap<String, String> headers,
			HttpStatusCode statusCode) {
		super(headers, statusCode);
		this.items = items;
	}

	public ManyTodoItemsResponse(MultiValueMap<String, String> headers, HttpStatusCode status) {
		super(headers, status);
	}
	
}
