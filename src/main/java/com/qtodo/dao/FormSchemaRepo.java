package com.qtodo.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.qtodo.model.userdefined.FormSchema;

public interface FormSchemaRepo extends JpaRepository<FormSchema, Long>{

}
