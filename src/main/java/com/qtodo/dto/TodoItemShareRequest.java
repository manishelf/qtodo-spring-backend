package com.qtodo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoItemShareRequest {
	String reciepientEmail;
	String todoItemUUID;
}
