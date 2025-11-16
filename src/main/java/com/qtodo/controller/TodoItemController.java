package com.qtodo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.qtodo.dto.DiffTodoItemResponse;
import com.qtodo.dto.ManyTodoItemCRUDRequest;
import com.qtodo.dto.SearchCriteria;
import com.qtodo.dto.TodoItemDiffRequest;
import com.qtodo.dto.TodoItemShareRequest;
import com.qtodo.response.ApiResponseBase;
import com.qtodo.response.ManyTodoItemsResponse;
import com.qtodo.response.TodoItemDto;
import com.qtodo.response.ValidationException;
import com.qtodo.service.TodoItemCreateService;
import com.qtodo.service.TodoItemDiffService;
import com.qtodo.service.TodoItemGetService;
import com.qtodo.service.TodoItemSearchService;
import com.qtodo.service.TodoItemUpdateService;

import jakarta.servlet.http.HttpServletRequest;

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
	
	@Autowired
	TodoItemDiffService todoItemDiffService;
	
	@PostMapping("/save")
	@PreAuthorize("hasAuthority('WRITE')")
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
	
	@PostMapping("/share")
	@PreAuthorize("hasAuthority('SHARE')")
	public ApiResponseBase shareTodoItem(@RequestBody ArrayList<TodoItemShareRequest> itemShareRequest) {
		var response = new ApiResponseBase(HttpStatus.OK);
		var reciepients = todoItemCreateService.shareAll(itemShareRequest);
		response.setResponseMessage("Item Shared to ! - "+reciepients.size());
		return response;
	}
	
	@PostMapping("/save/document")
	@PreAuthorize("hasAnyAuthority('WRITE', 'EDIT', 'READ')") // READ as otherwise profile pic cannot be saved for new user
	public ApiResponseBase saveDocuments(
				@RequestPart("file") MultipartFile docSaveRequest,
				@RequestPart("fileType") String fileType,
				@RequestPart("fileInfo") String fileInfo,
				@RequestPart("fileName") String fileName
			) throws ValidationException {
		
		String refUrl = this.todoItemCreateService.saveUserDoc(docSaveRequest, fileType, fileInfo, fileName);
		
		return new ApiResponseBase((Object)refUrl,HttpStatus.OK);
	}
	
	@PostMapping("/getdiff")
	@PreAuthorize("hasAuthority('READ')")
	public DiffTodoItemResponse getDiffTodoItems(@RequestBody TodoItemDiffRequest diffReq) {
		return this.todoItemDiffService.processDiff(diffReq);
	}

	@GetMapping("/all")
	@PreAuthorize("hasAuthority('READ')")
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
	
	@GetMapping("/doc/**")
	@PreAuthorize("hasAuthority('GET_DOCUMENT')")
	public ResponseEntity<Resource> getDocument(HttpServletRequest request) throws ValidationException {
		String uri = request.getRequestURI();
		String refUrl = uri.substring(uri.indexOf("/doc/") + 4);
		
		var dto =  this.todoItemGetService.getDocument(refUrl);
		
		if(dto == null) return ResponseEntity.ofNullable(null).status(HttpStatus.NOT_FOUND).build();
		
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(dto.getDataType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + dto.getData().getFilename() + "\"")
                .body(dto.getData());
	}
	
	@PostMapping("/search")
	@PreAuthorize("hasAuthority('READ')")
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
	@PreAuthorize("hasAnyAuthority('EDIT')")
	public	ApiResponseBase  updateTodoItem(@RequestBody ManyTodoItemCRUDRequest updateItemsRequest) throws ValidationException {
		
		if(updateItemsRequest.getItemList().isEmpty()) return new ApiResponseBase(HttpStatus.NO_CONTENT);
		
		String responseMessage = this.todoItemUpdateService.update(updateItemsRequest.getItemList());
		var status = HttpStatus.OK;
		if(responseMessage.contains("no permission")) {
			status = HttpStatus.FORBIDDEN;
		}
		return new ApiResponseBase(responseMessage, status);
	}
	
	@PostMapping("/delete")
	@PreAuthorize("hasAuthority('DELETE')")
	public ApiResponseBase deleteTodoItems(@RequestBody ManyTodoItemCRUDRequest deleteItemsRequest) throws ValidationException {
		
		if(deleteItemsRequest.getItemList().isEmpty()) return new ApiResponseBase(HttpStatus.NO_CONTENT);
		
		deleteItemsRequest.getItemList().stream().forEach(item->{
			item.setDeleted(true);
		});
		
		this.todoItemUpdateService.update(deleteItemsRequest.getItemList());
		
		return new ApiResponseBase("deleted - "+deleteItemsRequest.getItemList().size(), HttpStatus.OK);
	}
}
