package com.qtodo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.qtodo.model.DocumentEntity;

@Repository
public interface DocumentRepo extends JpaRepository<DocumentEntity, Long>{


}
