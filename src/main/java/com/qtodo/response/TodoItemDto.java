package com.qtodo.response;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.qtodo.model.TodoItem;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class TodoItemDto {
	String subject;
	
	String subjectBeforeUpdate;

	String description;
	
	List<TagDto> tags;

	boolean completionStatus;

	boolean setForReminder;

	LocalDateTime eventStartDate;

	LocalDateTime eventEndDate;
	
	UserDefinedTypeDto userDefined;
	
	Instant creationTimestamp;
	
	Instant updationTimestamp;
	
	boolean deleted;

	

	public TodoItemDto(TodoItem itemEntity) {
			this.subject = itemEntity.getSubject();
			this.subjectBeforeUpdate = this.subject;
			this.description = itemEntity.getDescription();
			
			if(itemEntity.getTags() != null)
			this.tags = itemEntity.getTags().stream().map((t)->new TagDto(t)).collect(Collectors.toList());
			
			this.completionStatus = itemEntity.isCompletionStatus();
			this.setForReminder = itemEntity.isSetForReminder();
			this.eventStartDate = itemEntity.getEventStartDate();
			this.eventEndDate = itemEntity.getEventEndDate();
			this.deleted = itemEntity.isDeleted();
			
			if(itemEntity.getUserDefined() != null)
			this.userDefined = new UserDefinedTypeDto(itemEntity.getUserDefined());
			
			this.creationTimestamp = itemEntity.getCreationTimestamp();
			this.updationTimestamp = itemEntity.getUpdationTimestamp();
	}
	
}
