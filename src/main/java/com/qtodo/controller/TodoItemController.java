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
import com.qtodo.model.userdefined.UserDefinedType;
import com.qtodo.response.ApiResponseBase;
import com.qtodo.response.ManyTodoItemsResponse;
import com.qtodo.response.TodoItemDto;
import com.qtodo.response.ValidationException;
import com.qtodo.service.TodoItemCreateService;
import com.qtodo.service.TodoItemGetService;
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
	
	@Autowired
	TodoItemGetService todoItemGetService;

	@OneToOne(cascade = CascadeType.PERSIST)
	UserDefinedType userDefined;

	@PostMapping("/save")
	public ApiResponseBase saveTodoItem(@RequestBody TodoItemSaveRequest itemSaveRequest) {
		if (itemSaveRequest.getItemList() != null) {
			todoItemCreateService.saveAll(itemSaveRequest.getItemList());
		}else {
			return new ApiResponseBase(HttpStatus.NO_CONTENT);
		}
		var response = new ApiResponseBase(HttpStatus.OK);
		response.setResponseMessage("Item Saved!");
		return response;
	}

	@GetMapping("/all")
	public ManyTodoItemsResponse getAllTodoItems(@RequestParam(name = "page", required = false) Integer pageNo,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "ord", required = false) ArrayList<String> sortOrder) {
		List<TodoItemDto> todoItemsList = null;

		if (pageNo != null && limit != null) {
			todoItemsList = todoItemGetService.getAll(pageNo, limit, sortOrder);
		} else {
			todoItemsList = todoItemGetService.getAll();
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
	public	ApiResponseBase  updateTodoItem(@RequestBody TodoItemDto item) throws ValidationException {
		String responseMessage = this.todoItemUpdateService.update(item);
		return new ApiResponseBase(responseMessage, HttpStatus.OK);
	}
}
