package com.qtodo.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TodoItemDiffRequest {
	
	boolean partial = false;
	List<TodoItemState> mergeItems = new ArrayList<>();
	List<TodoItemState> deleteItems = new ArrayList<>();
}
