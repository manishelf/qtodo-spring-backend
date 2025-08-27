package com.qtodo.dto;

import java.util.ArrayList;

import com.qtodo.model.TodoItem;
import com.qtodo.response.TodoItemDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoItemSaveRequest {

	ArrayList<TodoItemDto> itemList;

}
