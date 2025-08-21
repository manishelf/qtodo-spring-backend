package com.qtodo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.qtodo.dto.SearchCriteria;
import com.qtodo.dto.TodoItemSaveRequest;
import com.qtodo.model.TodoItem;
import com.qtodo.model.userdefined.UserDefinedType;
import com.qtodo.response.ApiResponseBase;
import com.qtodo.response.ManyTodoItemsResponse;
import com.qtodo.service.TodoItemCreateService;
import com.qtodo.service.TodoItemSearchService;
import com.qtodo.service.TodoItemUpdateService;

import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToOne;

@RestController
@RequestMapping("/item")
public class TodoItemController {

	@Autowired
	TodoItemCreateService todoItemCreateService;

	@Autowired
	TodoItemUpdateService todoItemUpdateService;

	@Autowired
	TodoItemSearchService todoItemSearchService;

	@OneToOne(cascade = CascadeType.PERSIST)
	UserDefinedType userDefined;

	@PostMapping("/save")
	public ApiResponseBase saveTodoItem(@RequestBody TodoItemSaveRequest itemSaveRequest) {
		System.out.print(itemSaveRequest);
		if (itemSaveRequest.getItem() != null) {
			todoItemCreateService.saveOne(itemSaveRequest.getItem());
		}
		if (itemSaveRequest.getItemList() != null) {
			todoItemCreateService.saveAll(itemSaveRequest.getItemList());
		}
		var response = new ApiResponseBase(HttpStatus.OK);
		response.setResponseMessage("Item Saved!");
		return response;
	}

	@GetMapping("/all")
	public ManyTodoItemsResponse getAllTodoItems(@RequestParam(name = "page", required = false) Integer pageNo,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "ord", required = false) ArrayList<String> sortOrder) {
		List<TodoItem> todoItemsList = null;

		if (pageNo != null && limit != null) {
			todoItemsList = todoItemCreateService.getAll(pageNo, limit, sortOrder);
		} else {
			todoItemsList = todoItemCreateService.getAll();
		}

		var code = HttpStatus.OK;
		if (todoItemsList.isEmpty()) {
			code = HttpStatus.NO_CONTENT;
		}
		var response = new ManyTodoItemsResponse(todoItemsList, code);
		return response;
	}

	@PostMapping("/search")
	public ManyTodoItemsResponse searchTodoItems(@RequestBody SearchCriteria query) {
		var todoItemsList = todoItemSearchService.searchByCriteria(query);

		var code = HttpStatus.OK;
		if (todoItemsList.isEmpty()) {
			code = HttpStatus.NO_CONTENT;
		}
		var response = new ManyTodoItemsResponse(todoItemsList, code);
		return response;
	}

	@PatchMapping("/update")
	public	ApiResponseBase  updateTodoItem(@RequestBody TodoItem item) {
		String responseMessage = this.todoItemUpdateService.update(item);

		var status = HttpStatus.OK;
		if (responseMessage == "existing item is latest") {
			status = HttpStatus.BAD_REQUEST;
		}
		var response = new ApiResponseBase(status);
		response.setResponseMessage(responseMessage);
		return response;
	}
}
