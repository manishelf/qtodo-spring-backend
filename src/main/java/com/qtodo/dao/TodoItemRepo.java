package com.qtodo.dao;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // New required import for @Param
import org.springframework.stereotype.Repository;

import com.qtodo.model.TodoItem;
import com.qtodo.model.TodoItemKey;

@Repository
public interface TodoItemRepo extends JpaRepository<TodoItem, TodoItemKey>, JpaSpecificationExecutor<TodoItem> {

	// use nameed params as it causes binding issues otherwise
	
	@Query("SELECT i FROM TodoItem i WHERE i.subject = :subject AND i.owningUserGroup.groupTitle = :userGroupTitle")
	List<TodoItem> findBySubjectAndUserGroup(@Param("subject") String subject, @Param("userGroupTitle") String userGroupTitle);

	@Query("SELECT i FROM TodoItem i WHERE i.owningUserGroup.groupTitle = :groupTitle")
	List<TodoItem> getByUserGroupTitle(@Param("groupTitle") String groupTitle);

	@Query("SELECT i FROM TodoItem i WHERE i.owningUserGroup.groupTitle = :groupTitle AND i.updationTimestamp >= :lastChange")
	List<TodoItem> getByUserGroupTitleAfter(@Param("groupTitle") String groupTitle, @Param("lastChange") Instant lastChange);
	
	@Query("SELECT i FROM TodoItem i WHERE i.owningUser.email = :email AND i.owningUserGroup.groupTitle = :groupTitle")
	List<TodoItem> getByUserEmailAndGroupTitle(@Param("email") String email, @Param("groupTitle") String groupTitle);

	@Query("SELECT i FROM TodoItem i WHERE i.owningUser.email = :email AND i.owningUserGroup.groupTitle = :groupTitle AND i.updationTimestamp >= :lastChange")
	List<TodoItem> getByUserEmailAndGroupTitleAfter(@Param("email") String email, @Param("groupTitle") String groupTitle, @Param("lastChange") Instant lastChange);
	
	@Query("SELECT i FROM TodoItem i WHERE i.subject = :subject AND i.owningUser.email = :email AND i.owningUserGroup.groupTitle = :groupTitle")
	Optional<TodoItem> findBySubjectAndUserEmailAndUserGroup(@Param("subject") String subject, @Param("email") String email, @Param("groupTitle") String groupTitle);
	
	Optional<TodoItem> findByUuid(String uuid);

	Optional<TodoItem> getByUuid(String uuid);
}
