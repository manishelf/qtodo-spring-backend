package com.qtodo.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.hibernate.mapping.UserDefinedObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qtodo.auth.UserPermission;
import com.qtodo.model.TodoItem;
import com.qtodo.response.TodoItemDto;
import com.qtodo.response.ValidationException;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class TodoItemUpdateService extends ServiceBase {

	@Autowired
	TodoItemGetService getService;
	
	@Autowired
	TodoItemCreateService createService;
	
	public String update(List<TodoItemDto> forUpdateList) throws ValidationException {
		String responseMessage = "a";
		
		var isColab = getAuthenticatedUsersUserGroup().isColaboration();
		var permissions = getAuthenticatedUsersPermissions();
		
		System.out.println(forUpdateList.size());
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
							todoItemRepo.save(existingItem);
							todoItemRepo.flush(); // otherwise get exception for constraint on uuid
					}
					forUpdate.setVersion(forUpdate.getVersion()+1); 
					var ent = createService.saveOne(forUpdate, existingItem.getOwningUser(), existingItem.getOwningUserGroup());
					if(ent != null) {
						var action = existingItem.isDeleted()? "discarded and new created": "updated";
						responseMessage += "existing item "+action;
					}
				}else {
					responseMessage += "no permission - EDIT";
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
		
		System.out.println(responseMessage);
		return responseMessage;
	}

}