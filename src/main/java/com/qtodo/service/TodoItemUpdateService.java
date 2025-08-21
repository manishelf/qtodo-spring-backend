package com.qtodo.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.qtodo.model.TodoItem;

@Service
public class TodoItemUpdateService extends TodoItemServiceBase {

	public String update(TodoItem forUpdate) {
		Optional<TodoItem> existing = this.todoItemRepo.findBySubject(forUpdate.getSubject());
		String responseMessage = "";
		if (existing.isPresent()) {
			TodoItem exisitgItem = existing.get();
			if (exisitgItem.getCreationTimestamp().isBefore(forUpdate.getUpdationTimestamp())) {
				responseMessage = "existing item updated";
			} else {
				responseMessage = "existing item is latest";
			}
		} else {
			responseMessage = "non existing item saved";
		}
		this.todoItemRepo.save(forUpdate);
		return responseMessage;
	}

}