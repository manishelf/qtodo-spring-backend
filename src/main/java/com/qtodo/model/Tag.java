package com.qtodo.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qtodo.model.userdefined.UserDefinedType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Tag extends EntityBase{
	@Column(unique = true, nullable = false)
	@NotBlank
    String name;
	
	@ManyToMany
	@JsonIgnore
	List<TodoItem> todoItems = new ArrayList();
	
	@OneToOne
	@JsonIgnore
	UserDefinedType userDefined;
}