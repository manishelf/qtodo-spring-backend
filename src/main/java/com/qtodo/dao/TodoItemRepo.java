package com.qtodo.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.qtodo.model.TodoItem;
import com.qtodo.model.TodoItemKey;

@Repository
public interface TodoItemRepo extends JpaRepository<TodoItem, TodoItemKey>, JpaSpecificationExecutor<TodoItem> {

	Optional<TodoItem> findBySubject(String subject);

	@Query("SELECT i FROM TodoItem i WHERE i.subject = ?1AND i.owningUserGroup.groupTitle = ?2")
	Optional<TodoItem> findBySubjectAndUserGroup(String subject, String userGroupTitle);

	
	@Query("SELECT i FROM TodoItem i WHERE i.owningUserGroup.groupTitle = ?1")
	List<TodoItem> getByUserGroupTitle(String groupTitle);

	@Query("SELECT i FROM TodoItem i WHERE i.owningUser.email = ?1 AND i.owningUserGroup.groupTitle = ?2")
	List<TodoItem> getByUserEmailAndGroupTitle(String email, String groupTitle);

	
	@Query("SELECT i FROM TodoItem i WHERE i.owningUser.email = ?1 AND i.owningUserGroup.groupTitle = ?2AND i.subject = ?1")
	Optional<TodoItem> findBySubjectAndUserEmailAndUserGroup(String subject, String email, String groupTitle);

}
