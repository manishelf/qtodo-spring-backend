package com.qtodo.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
	
	@Autowired
	TodoItemCreateService createService;
	
	public String update(List<TodoItemDto> forUpdateList) throws ValidationException {
		String responseMessage = "";
		for(TodoItemDto forUpdate : forUpdateList) {
			Optional<TodoItem> existing = todoItemRepo.getByUuid(forUpdate.getUuid());
			if (existing.isPresent()) {
				TodoItem existingItem = existing.get();
				responseMessage = "existing item updated";
				if(!forUpdate.getSubject().equals(existingItem.getSubject())) {
					existingItem.setDeleted(true);
					var uuid = existingItem.getUuid();
					existingItem.setUuid(
							"stale-"+uuid+"-"+LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
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