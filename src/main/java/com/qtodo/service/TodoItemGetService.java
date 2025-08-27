package com.qtodo.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.qtodo.model.TodoItem;
import com.qtodo.model.UserEntity;
import com.qtodo.model.UserGroup;
import com.qtodo.response.TodoItemDto;

@Component
public class TodoItemGetService extends TodoItemServiceBase{
	
	public List<TodoItemDto> getAll(Integer pageNo, Integer limit, ArrayList<String> sortOrder) {

		PageRequest pagingInfo = PageRequest.of(pageNo, limit, Sort.by(sortOrder.toArray(new String[0])));

		Page<TodoItem> pagedResult = todoItemRepo.findAll(pagingInfo);

		if (pagedResult.hasContent()) {
			var res = pagedResult.getContent();
			return res.stream().map(i->new TodoItemDto(i)).collect(Collectors.toList());
		} else {
			return new ArrayList<>();
		}
	}

	public List<TodoItemDto> getAll() {

		UserEntity user = getAuthenticatedUser();
		
		UserGroup userGroup = getUserGroup();
		
		List<TodoItem> items = null;
		
		if(userGroup.isColaboration())
			items = todoItemRepo.getByUserGroupTitle(userGroup.getGroupTitle());
		else items = todoItemRepo.getByUserEmailAndGroupTitle(user.getEmail(), userGroup.getGroupTitle());

		Collections.sort(items, (x, y) -> {
			// Prioritize incomplete items over completed ones
			if (x.isCompletionStatus() && y.isCompletionStatus())
				return -1;
			if (x.isCompletionStatus() && !y.isCompletionStatus())
				return 1;

			// If completion status is the same, prioritize items with reminders
			if (x.isSetForReminder() && !y.isCompletionStatus())
				return -1;
			if (!x.isSetForReminder() && y.isSetForReminder())
				return 1;

			return 0;
		});

		return items.stream().map(i->new TodoItemDto(i)).collect(Collectors.toList());
	}
	
	public Optional<TodoItem> getItem(String subject){
		Optional<TodoItem> item = Optional.of(null);
		
		UserEntity user = getAuthenticatedUser();
		UserGroup userGroup = getUserGroup();
		
		if(userGroup.isColaboration()) {			
			item = this.todoItemRepo.findBySubjectAndUserGroup(subject, userGroup.getGroupTitle());
		}
		else {
			item = this.todoItemRepo.findBySubjectAndUserEmailAndUserGroup(subject, user.getEmail(), userGroup.getGroupTitle());
		}
		
		return item;
	}
}
