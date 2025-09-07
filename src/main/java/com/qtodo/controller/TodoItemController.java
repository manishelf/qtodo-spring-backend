package com.qtodo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.qtodo.dto.SearchCriteria;
import com.qtodo.dto.ManyTodoItemCRUDRequest;
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
	public ApiResponseBase saveTodoItem(@RequestBody ManyTodoItemCRUDRequest itemSaveRequest) {
		var response = new ApiResponseBase(HttpStatus.OK);
		if (itemSaveRequest.getItemList() != null) {
			var items = todoItemCreateService.saveAll(itemSaveRequest.getItemList());
			response.setResponseMessage("Item Saved! - "+items.size());
		}else {
			return new ApiResponseBase(HttpStatus.NO_CONTENT);
		}
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
	public	ApiResponseBase  updateTodoItem(@RequestBody ManyTodoItemCRUDRequest updateItemsRequest) throws ValidationException {
		
		if(updateItemsRequest.getItemList().isEmpty()) return new ApiResponseBase(HttpStatus.NO_CONTENT);
		
		String responseMessage = this.todoItemUpdateService.update(updateItemsRequest.getItemList());
		return new ApiResponseBase(responseMessage, HttpStatus.OK);
	}
	
	@PostMapping("/delete")
	public ApiResponseBase deleteTodoItems(@RequestBody ManyTodoItemCRUDRequest deleteItemsRequest) throws ValidationException {
		
		if(deleteItemsRequest.getItemList().isEmpty()) return new ApiResponseBase(HttpStatus.NO_CONTENT);
		
		deleteItemsRequest.getItemList().stream().forEach(item->{
			item.setDeleted(true);
		});
		
		this.todoItemUpdateService.update(deleteItemsRequest.getItemList());
		
		return new ApiResponseBase("deleted - "+deleteItemsRequest.getItemList().size(), HttpStatus.OK);
	}

	public ApiResponseBase addDocumentForUser() {
		
		return new ApiResponseBase(HttpStatus.OK);
	}
}
