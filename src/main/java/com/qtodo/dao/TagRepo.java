package com.qtodo.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.qtodo.model.Tag;

@Repository
public interface TagRepo extends JpaRepository<Tag, String> {
	
	public Optional<Tag> findByName(String name); 

}
