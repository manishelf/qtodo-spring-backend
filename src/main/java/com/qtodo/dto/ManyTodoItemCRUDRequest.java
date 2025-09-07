package com.qtodo.dto;

import java.util.ArrayList;

import com.qtodo.response.TodoItemDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManyTodoItemCRUDRequest {

	ArrayList<TodoItemDto> itemList;

}
