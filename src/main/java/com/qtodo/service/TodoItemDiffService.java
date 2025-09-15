package com.qtodo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.qtodo.dao.TodoItemRepo;
import com.qtodo.dto.DiffTodoItemResponse;
import com.qtodo.dto.TodoItemDiffRequest;
import com.qtodo.dto.TodoItemState;
import com.qtodo.model.TodoItem;
import com.qtodo.response.TodoItemDto;

@Service
public class TodoItemDiffService extends TodoItemServiceBase {
	
	@Autowired
	TodoItemRepo itemRepo;

	public DiffTodoItemResponse processDiff(TodoItemDiffRequest diffReq) {
		
		List<TodoItemDto> itemsForAdd = new ArrayList<>();
		List<TodoItemDto> itemsForUpdate = new ArrayList<>();
		List<TodoItemState> itemsForDelete = new ArrayList<>();
		List<TodoItemState> itemsForSync = new ArrayList<>();
		
		var itemsForMergeReq = diffReq.getMergeItems();
		var itemsForDeleteReq = diffReq.getDeleteItems();

		
		for(var itemForDelete : itemsForMergeReq) {
			var item = itemRepo.findByUuid(itemForDelete.getUuid());
			if(item.isPresent()) {
				var ent = item.get();
				if(itemForDelete.getVersion() > ent.getVersion()) {
					ent.setDeleted(true);
				}
			}
		}
		
		String username = getAuthenticatedUser().getEmail();
		String userGroup = getAuthenticatedUserGroup().getGroupTitle();
		
		var itemList = todoItemRepo.getByUserEmailAndGroupTitle(username,userGroup);
		
		Map<String, TodoItem> itemMap = new HashMap();
		
		itemList.stream().forEach(
			(item)->{
				itemMap.put(item.getUuid(), item);
				var itemState = new TodoItemState(item.getUuid());
				if(!(itemsForMergeReq.contains(itemState)
					|| itemsForDeleteReq.contains(itemState))
				) {
					itemsForAdd.add(new TodoItemDto(item));
				}
			}
		);
		
		for(var mergeItem : diffReq.getMergeItems()) {
			var item = itemMap.get(mergeItem.getUuid());
			if(item != null) {
				if(item.getVersion() < mergeItem.getVersion()) {
					itemsForSync.add(mergeItem);
				}else if(item.getVersion() > mergeItem.getVersion()) {
					var ent = item;
					if(ent.isDeleted()) {
						itemsForDelete.add(mergeItem);
					}else {
						itemsForUpdate.add(new TodoItemDto(ent));
					}
				}
			}else {
				itemsForSync.add(mergeItem);
			}
		}
		
		var resp = new DiffTodoItemResponse(HttpStatus.OK);
		resp.setItemsForAdd(itemsForAdd);
		resp.setItemsForUpdate(itemsForUpdate);
		resp.setItemsForSync(itemsForSync);
		resp.setItemsForDelete(itemsForDelete);
		return resp;
	}
	
}
