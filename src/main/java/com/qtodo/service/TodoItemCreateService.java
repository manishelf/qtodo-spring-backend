package com.qtodo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.qtodo.auth.UserPermission;
import com.qtodo.dto.TodoItemShareRequest;
import com.qtodo.dto.UserDto;
import com.qtodo.model.DocumentEntity;
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
import com.qtodo.response.ValidationException;

import jakarta.transaction.Transactional;

@Service
public class TodoItemCreateService extends ServiceBase {
	
	public List<TodoItem> saveAll(ArrayList<TodoItemDto> itemDtoList) {
		
		List<TodoItem> items = new LinkedList<>();
		
		for(var item : itemDtoList) {
			var entity = saveOne(item);
			if(entity != null) {				
				items.add(entity);
			}
		}
	
		return items;
	}

	public TodoItem saveOne(TodoItemDto item, UserEntity owner, UserGroup owningGroup) {
		
	    TodoItem e = new TodoItem();
		e.setSubject(item.getSubject());
		e.setUuid(item.getUuid());
		e.setVersion(item.getVersion());
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
		return saveOne(item, getAuthenticatedUser(), getAuthenticatedUsersUserGroup());
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

	public void hardDeleteItem(TodoItem existing) {
		this.todoItemRepo.delete(existing);
		this.todoItemRepo.flush();
	}

	public String saveUserDoc(MultipartFile docSaveRequest, String fileType, String fileInfo, String fileName) throws ValidationException {
		try {
            Files.createDirectories(Paths.get(getFsDocUrl()));
            
            String email = getAuthenticatedUser().getEmail();
            String userGroup = getAuthenticatedUsersUserGroup().getGroupTitle();
            fileName = userGroup+"_"+email.replace(".", "_").replace("@", "_")+"_"+fileName;
            String[] extension = fileType.split("/");
            if(extension.length!=2) {
            	extension = new String[2];
            	extension[1]="file";
            }
            Files.copy(docSaveRequest.getInputStream(), 
                       Paths.get(getFsDocUrl()).resolve(fileName+'.'+extension[1]), 
                       StandardCopyOption.REPLACE_EXISTING);
            
            var de = docRepo.findByRefUrl(fileName);
            if(de.isEmpty()) {
	            DocumentEntity docEntity = new DocumentEntity();
	            docEntity.setDataType(fileType);
	            docEntity.setInfo(fileInfo);
	            docEntity.setRefUrl("/"+fileName);
            	
            	var ue = getAuthenticatedUser();
            	var ug = getAuthenticatedUsersUserGroup();
            	
            	docEntity.setOwningUser(ue);
            	docEntity.setOwningUserGroup(ug);
            	docRepo.save(docEntity);
            	
            	ue.getDocs().add(docEntity);
            	
            	return fileName;
            }
            
            
            return de.get().getRefUrl();
        } catch (IOException e) {
            e.printStackTrace();
            throw ValidationException.failedFor("document", "failed to save "+fileName);
        }
	}

	public List<UserDto> shareAll(ArrayList<TodoItemShareRequest> itemShareRequest) {
		var reciepients = new ArrayList<UserDto>();
		var userGroup = getAuthenticatedUsersUserGroup();
		var user = getAuthenticatedUser();
		
		if(!userGroup.isColaboration())
		itemShareRequest.forEach((req)->{
			var reciepient = userRepo.getByEmailInUserGroup(req.getReciepientEmail(), userGroup.getGroupTitle());
			if(reciepient != null) {
				var todoItem = todoItemRepo.findByUuid(req.getTodoItemUUID());
				if(todoItem.isPresent()) {
					var todoItemEnt = todoItem.get();
					if(todoItemEnt.getOwningUser().equals(user)) {
						var newTodoItem = todoItemEnt.clone();
						var newUUID = "SHARED-"+Instant.now().toEpochMilli()+"-"+todoItemEnt.getUuid();
						
						newTodoItem.setOwningUser(reciepient);
						newTodoItem.setOwningUserGroup(userGroup);
						newTodoItem.setUuid(newUUID);
						
						todoItemRepo.save(newTodoItem);
						
						reciepient.getTodoItems().add(todoItemEnt);
						reciepients.add(new UserDto(reciepient, userGroup));
					};
				}
			}
		});
		
		return reciepients;
	}
}
