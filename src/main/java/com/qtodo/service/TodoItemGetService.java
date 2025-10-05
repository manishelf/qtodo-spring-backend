package com.qtodo.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.qtodo.dto.DocumentDto;
import com.qtodo.model.TodoItem;
import com.qtodo.model.UserEntity;
import com.qtodo.model.UserGroup;
import com.qtodo.response.TodoItemDto;
import com.qtodo.response.ValidationException;


@Component
public class TodoItemGetService extends ServiceBase{
	
	public List<TodoItemDto> getAll(Integer pageNo, Integer limit, ArrayList<String> sortOrder) {

		PageRequest pagingInfo = PageRequest.of(pageNo, limit, Sort.by(sortOrder.toArray(new String[0])));

		Page<TodoItem> pagedResult = todoItemRepo.findAll(pagingInfo);

		if (pagedResult.hasContent()) {
			var res = pagedResult.getContent();
			return res.stream().map(i->new TodoItemDto(i)).collect(Collectors.toList());
		} else {
			return new ArrayList<>();
		}
	}

	public List<TodoItemDto> getAll() {

		UserEntity user = getAuthenticatedUser();
		
		UserGroup userGroup = getAuthenticatedUsersUserGroup();
		
		List<TodoItem> items = null;
		
		if(userGroup.isColaboration())
			items = todoItemRepo.getByUserGroupTitle(userGroup.getGroupTitle());
		else items = todoItemRepo.getByUserEmailAndGroupTitle(user.getEmail(), userGroup.getGroupTitle());

		Collections.sort(items, (x, y) -> {
			// Prioritize incomplete items over completed ones
			if (x.isCompletionStatus() && y.isCompletionStatus())
				return -1;
			if (x.isCompletionStatus() && !y.isCompletionStatus())
				return 1;

			// If completion status is the same, prioritize items with reminders
			if (x.isSetForReminder() && !y.isCompletionStatus())
				return -1;
			if (!x.isSetForReminder() && y.isSetForReminder())
				return 1;

			return 0;
		});

		return items.stream().map(i->new TodoItemDto(i)).collect(Collectors.toList());
	}
	
	public List<TodoItem> getItem(String subject){
		List<TodoItem> item = new ArrayList<>();
		
		UserEntity user = getAuthenticatedUser();
		UserGroup userGroup = getAuthenticatedUsersUserGroup();
		
		if(userGroup.isColaboration()) {			
			item = this.todoItemRepo.findBySubjectAndUserGroup(subject, userGroup.getGroupTitle());
		}
		else {
			var opt = this.todoItemRepo.findBySubjectAndUserEmailAndUserGroup(subject, user.getEmail(), userGroup.getGroupTitle());
			if(opt.isPresent()) {
				item = Arrays.asList(opt.get());
			}
		}
		
		return item;
	}
	
	public DocumentDto getDocument(String refUrl) throws ValidationException {
		DocumentDto dto = new DocumentDto();
		var doc = docRepo.findByRefUrl("/"+refUrl);
		if(doc.isPresent()) {
			var docEnt = doc.get();
//			if(docEnt.getOwningUser().getEmail().equals(getAuthenticatedUser().getEmail())) { 
				var ext = docEnt.getDataType().split("/")[1];
				Path path = Paths.get(getFsDocUrl()).resolve(refUrl+'.'+ext).normalize();
				try {
					dto.setData(new UrlResource(path.toUri()));
					dto.setInfo(docEnt.getInfo());
					String contentType;
					try {
					    contentType = Files.probeContentType(path);
					} catch (IOException e) {
					    contentType = "application/octet-stream";
					}
					if(contentType == null) {
						contentType = docEnt.getDataType();
					}
					dto.setDataType(contentType);
					return dto;
				} catch (MalformedURLException e) {
					e.printStackTrace();
					throw ValidationException.failedFor("document", "file - "+refUrl+" not found");
				}
//			}
		}
		return null;
	}
}
