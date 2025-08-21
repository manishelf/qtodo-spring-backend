package com.qtodo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.qtodo.model.Tag;

@Repository
public interface TagRepo extends JpaRepository<Tag, Long> {
	
	public Tag findByName(String name); 

}
