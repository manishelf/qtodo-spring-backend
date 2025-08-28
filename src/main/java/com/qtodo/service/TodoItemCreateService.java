package com.qtodo.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.qtodo.model.Tag;
import com.qtodo.model.TodoItem;
import com.qtodo.model.UserEntity;
import com.qtodo.model.UserGroup;
import com.qtodo.model.userdefined.FormSchema;
import com.qtodo.model.userdefined.UserDefinedType;
import com.qtodo.response.FormSchemaDto;
import com.qtodo.response.TagDto;
import com.qtodo.response.TodoItemDto;
import com.qtodo.response.UserDefinedTypeDto;

@Service
public class TodoItemCreateService extends TodoItemServiceBase {
	
	public List<TodoItem> saveAll(ArrayList<TodoItemDto> itemDtoList) {
		
		List<TodoItem> items = new LinkedList<>();
		
		for(var item : itemDtoList) {
			var entity = saveOne(item);
			items.add(entity);
		}
		
		
		return todoItemRepo.saveAll(items);
	}

	public TodoItem saveOne(TodoItemDto item, UserEntity owner, UserGroup owningGroup) {
		
	    TodoItem e = new TodoItem();
		e.setSubject(item.getSubject());
		e.setDescription(item.getDescription());
		
		if(item.getTags() != null) {
			var tagEntities = item.getTags().stream().map(t->saveTag(t)).collect(Collectors.toList()); 
			e.setTags(tagEntities);
		}
		
		e.setCompletionStatus(item.isCompletionStatus());
		e.setSetForReminder(item.isSetForReminder());
		e.setEventStartDate(item.getEventStartDate());
		e.setEventEndDate(item.getEventEndDate());
		e.setDeleted(item.isDeleted());
		
		
		e.setOwningUser(owner);
		e.setOwningUserGroup(owningGroup);
		
		e = todoItemRepo.save(e);

		if(item.getUserDefined()!=null) {			
			var ud = saveUserDefined(item.getUserDefined());
			ud.setOwningItem(e);
			e.setUserDefined(ud);
		}
		
		return e;
	}
	
	public TodoItem saveOne(TodoItemDto item) {
		return saveOne(item, getAuthenticatedUser(), getUserGroup());
	}

	public UserDefinedType saveUserDefined(UserDefinedTypeDto userDefined) {

		UserDefinedType ud = new UserDefinedType();
		var tagEntity = this.tagRepo.findByName(userDefined.getTag().getName());
		if(!tagEntity.isPresent()) {
			tagEntity = Optional.of(saveTag(userDefined.getTag()));
		}
		
		ud.setTag(tagEntity.get());
		ud.setFormSchema(saveFormControlSchema(userDefined.getFormControlSchema()));
		ud.setData(userDefined.getData());
		
		ud = udtRepo.save(ud);
		
		tagEntity.get().setUserDefined(ud);
		
		return ud;
	}

	public FormSchema saveFormControlSchema(FormSchemaDto formControlSchema) {
		FormSchema f = new FormSchema();
		f.setFields(formControlSchema.getFields().stream().map(ff->ff.store()).collect(Collectors.toList()));
		return fsRepo.save(f);
	}

	public Tag saveTag(TagDto tag) {
		Tag t = new Tag();
		t.setName(tag.getName());
		return this.tagRepo.save(t);
	}
}
