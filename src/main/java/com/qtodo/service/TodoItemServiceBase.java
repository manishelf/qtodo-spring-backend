package com.qtodo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qtodo.dao.TagRepo;
import com.qtodo.dao.TodoItemRepo;

import lombok.Getter;

@Service
@Getter
public class TodoItemServiceBase {

	@Autowired
	protected TodoItemRepo todoItemRepo;

	@Autowired
	protected TagRepo tagRepo;
}
