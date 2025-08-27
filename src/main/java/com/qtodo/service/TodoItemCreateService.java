package com.qtodo.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.qtodo.model.TodoItem;
import com.qtodo.model.UserEntity;
import com.qtodo.model.UserGroup;
import com.qtodo.response.TodoItemDto;

@Service
public class TodoItemCreateService extends TodoItemServiceBase {
	
	public List<TodoItem> saveAll(ArrayList<TodoItemDto> itemDtoList) {
		
		List<TodoItem> items = new LinkedList<>();
		UserEntity owner = getAuthenticatedUser();
		UserGroup owningGroup = getUserGroup();
		
		for(var item : itemDtoList) {
			var entity = item.toEntity();
			entity.setOwningUser(owner);
			entity.setOwningUserGroup(owningGroup);
			items.add(entity);
		}
		
		
		return todoItemRepo.saveAll(items);
	}

	public TodoItem saveOne(TodoItemDto item) {
		var entity = item.toEntity();
		entity.setOwningUser(getAuthenticatedUser());
		entity.setOwningUserGroup(getUserGroup());
		todoItemRepo.save(entity);
		return entity;
	}
}
