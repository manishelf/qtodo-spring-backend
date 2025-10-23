package com.qtodo.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.qtodo.model.UserDefinedTypeKey;
import com.qtodo.model.userdefined.UserDefinedType;

public interface UserDefinedTypesRepo extends JpaRepository<UserDefinedType, UserDefinedTypeKey> {

}
