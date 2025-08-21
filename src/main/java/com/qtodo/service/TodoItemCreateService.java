package com.qtodo.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.qtodo.model.TodoItem;

@Service
public class TodoItemCreateService extends TodoItemServiceBase {

	public void saveAll(List<TodoItem> items) {
		todoItemRepo.saveAll(items);
	}

	public void saveOne(TodoItem item) {
		todoItemRepo.save(item);
	}

	public List<TodoItem> getAll(Integer pageNo, Integer limit, ArrayList<String> sortOrder) {

		PageRequest pagingInfo = PageRequest.of(pageNo, limit, Sort.by(sortOrder.toArray(new String[0])));

		Page<TodoItem> pagedResult = todoItemRepo.findAll(pagingInfo);

		if (pagedResult.hasContent()) {
			return pagedResult.getContent();
		} else {
			return new ArrayList<>();
		}
	}

	public List<TodoItem> getAll() {

		var items = todoItemRepo.findAll();

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

		return items;
	}
}
