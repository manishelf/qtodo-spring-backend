package com.qtodo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
		return saveOne(item, getAuthenticatedUser(), getAuthenticatedUserGroup());
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

	public void hardDeleteItem(TodoItem existing) {
		this.todoItemRepo.delete(existing);
		this.todoItemRepo.flush();
	}

	public String saveUserDoc(MultipartFile docSaveRequest, String fileType, String fileInfo, String fileName) throws ValidationException {
		try {
            Files.createDirectories(Paths.get(getFsDocUrl()));
            
            String email = getAuthenticatedUser().getEmail();
            String userGroup = getAuthenticatedUserGroup().getGroupTitle();
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
	            docEntity.setOwningUser(getAuthenticatedUser());
	            docEntity.setOwningUserGroup(getAuthenticatedUserGroup());
            	docRepo.save(docEntity);
            	
            	var ue = userRepo.getByEmailInUserGroup(email, userGroup);
            	ue.getDocs().add(docEntity); // as it is lazy
            	
            	return fileName;
            }
            
            
            return de.get().getRefUrl();
        } catch (IOException e) {
            e.printStackTrace();
            throw ValidationException.failedFor("document", "failed to save "+fileName);
        }
	}
}
