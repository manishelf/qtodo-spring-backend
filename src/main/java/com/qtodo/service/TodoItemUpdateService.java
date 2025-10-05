package com.qtodo.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qtodo.auth.UserPermission;
import com.qtodo.model.TodoItem;
import com.qtodo.response.TodoItemDto;
import com.qtodo.response.ValidationException;

import jakarta.transaction.Transactional;

@Service
public class TodoItemUpdateService extends ServiceBase {

	@Autowired
	TodoItemGetService getService;
	
	@Autowired
	TodoItemCreateService createService;
	
	public String update(List<TodoItemDto> forUpdateList) throws ValidationException {
		String responseMessage = "";
		
		var isColab = getAuthenticatedUsersUserGroup().isColaboration();
		var permissions = getAuthenticatedUsersPermissions();
		
		for(TodoItemDto forUpdate : forUpdateList) {
			Optional<TodoItem> existing = todoItemRepo.getByUuid(forUpdate.getUuid());
			if (existing.isPresent()) {
				TodoItem existingItem = existing.get();
				if(permissions.contains(UserPermission.EDIT)) {
					if(!forUpdate.getSubject().equals(existingItem.getSubject())) {
							existingItem.setDeleted(true);
							var uuid = existingItem.getUuid();
							existingItem.setUuid(						
									"stale-"+uuid+"-"+LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
					}
					if(permissions.contains(UserPermission.EDIT)) {
						forUpdate.setVersion(forUpdate.getVersion()+1); 
						// frontend does not send a version update as order is save backend -> save frontend as subject can change
						var ent = createService.saveOne(forUpdate, existingItem.getOwningUser(), existingItem.getOwningUserGroup());
						if(ent != null) {
							var action = existingItem.isDeleted()? "deleted and new created": "updated";
							responseMessage += "existing item "+action;
						}else {
							responseMessage += "no permission - EDIT";
						}
					}else {
						responseMessage += "no permission - DELETE";
					}
				}
			} else {
				if(permissions.contains(UserPermission.WRITE)) {					
					if(createService.saveOne(forUpdate) != null) {						
						responseMessage += "non existing item saved";
					}else {
						responseMessage += "no permission - WRITE";
					}
				}
			}
			responseMessage += ", ";
		}
		
		return responseMessage;
	}

}