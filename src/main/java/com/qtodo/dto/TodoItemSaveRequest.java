package com.qtodo.dto;

import java.util.ArrayList;

import com.qtodo.model.TodoItem;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoItemSaveRequest {

	TodoItem item;

	ArrayList<TodoItem> itemList;

}
