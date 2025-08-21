package com.qtodo.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.qtodo.model.TodoItem;

@Repository
public interface TodoItemRepo extends JpaRepository<TodoItem, Long>, JpaSpecificationExecutor<TodoItem> {

	Optional<TodoItem> findBySubject(String subject);

}
