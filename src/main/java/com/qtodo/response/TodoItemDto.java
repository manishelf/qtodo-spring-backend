package com.qtodo.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.qtodo.model.TodoItem;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TodoItemDto {
	String subject;

	String description;
	
	List<TagDto> tags;

	boolean completionStatus;

	boolean setForReminder;

	LocalDateTime eventStartDate;

	LocalDateTime eventEndDate;
	
	UserDefinedTypeDto userDefined;
	
	LocalDateTime creationTimestamp;
	
	LocalDateTime updationTimestamp;
	
	boolean deleted;

	public TodoItemDto(TodoItem itemEntity) {
			this.subject = itemEntity.getSubject();
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
	
	public TodoItem toEntity() {
		TodoItem e = new TodoItem();
		e.setSubject(subject);
		e.setDescription(description);
		
		if(tags != null)
		e.setTags(tags.stream().map(t->t.toEntity()).collect(Collectors.toList()));
		
		e.setCompletionStatus(completionStatus);
		e.setSetForReminder(setForReminder);
		e.setEventStartDate(eventStartDate);
		e.setEventEndDate(eventEndDate);
		e.setDeleted(deleted);
		
		if(userDefined!=null)
		e.setUserDefined(userDefined.toEntity());
		
		e.setCreationTimestamp(creationTimestamp);
		e.setUpdationTimestamp(updationTimestamp);
		
		return e;
	}

}
