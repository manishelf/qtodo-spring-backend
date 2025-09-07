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
			List<TodoItem> existing = getService.getItem(forUpdate.getSubjectBeforeUpdate());
			if (!existing.isEmpty()) {
				TodoItem existingItem = existing.get(0);
				responseMessage = "existing item updated";
				forUpdate.setCreationTimestamp(existingItem.getCreationTimestamp());
				if(!forUpdate.getSubject().equals(existingItem.getSubject())) {
					existingItem.setDeleted(true);
				}
				createService.saveOne(forUpdate, existingItem.getOwningUser(), existingItem.getOwningUserGroup());
			} else {
				responseMessage += "non existing item saved";
				createService.saveOne(forUpdate);
			}
			responseMessage += ", ";
		}
		
		return responseMessage;
	}

}