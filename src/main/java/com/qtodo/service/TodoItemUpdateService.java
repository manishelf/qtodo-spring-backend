package com.qtodo.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qtodo.model.TodoItem;
import com.qtodo.response.TodoItemDto;
import com.qtodo.response.ValidationException;

@Service
public class TodoItemUpdateService extends TodoItemServiceBase {

	@Autowired
	TodoItemGetService getService;
	
	public String update(TodoItemDto forUpdate) throws ValidationException {
		
		Optional<TodoItem> existing = getService.getItem(forUpdate.getSubject());
		
		String responseMessage = "";
		TodoItem item = forUpdate.toEntity();
		if (existing.isPresent()) {
			TodoItem exisitgItem = existing.get();
			if (exisitgItem.getCreationTimestamp().isBefore(forUpdate.getCreationTimestamp())) {
				responseMessage = "existing item updated";
			} else {
				responseMessage = "existing item is latest";
				throw ValidationException.failedFor("update item", "item not the latest version, please refresh page to update item state");
			}
			item.setId(exisitgItem.getId());
		} else {
			responseMessage = "non existing item saved";
		}
		this.todoItemRepo.save(item);
		return responseMessage;
	}

}