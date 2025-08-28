package com.qtodo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qtodo.model.TodoItem;
import com.qtodo.response.TodoItemDto;
import com.qtodo.response.ValidationException;

@Service
public class TodoItemUpdateService extends TodoItemServiceBase {

	@Autowired
	TodoItemGetService getService;
	
	@Autowired
	TodoItemCreateService createService;
	
	public String update(List<TodoItemDto> forUpdateList) throws ValidationException {
		
		String responseMessage = "";
		for(TodoItemDto forUpdate : forUpdateList) {
			List<TodoItem> existing = getService.getItem(forUpdate.getSubjectBeforUpdate());
			
			if (!existing.isEmpty()) {
				TodoItem exisitgItem = existing.get(0);
				if (exisitgItem.getCreationTimestamp().isBefore(forUpdate.getCreationTimestamp())) {
					responseMessage = "existing item updated";
					createService.saveOne(forUpdate, exisitgItem.getOwningUser(), exisitgItem.getOwningUserGroup());
				} else {
					responseMessage += "existing item is latest";
					throw ValidationException.failedFor("update item", "item not the latest version, please refresh page to update item state");
				}
			} else {
				responseMessage += "non existing item saved";
				createService.saveOne(forUpdate);
			}
			responseMessage += ", ";
		}
		
		return responseMessage;
	}

}