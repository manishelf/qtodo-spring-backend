package com.qtodo.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.qtodo.model.userdefined.UserDefinedType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@IdClass(TodoItemKey.class)
public class TodoItem extends EntityBase{
	
	@NotBlank
	@Id
	String subject;

	@Column(columnDefinition = "TEXT")
	String description;
	
	@ManyToMany(mappedBy="todoItems", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	List<Tag> tags = new ArrayList();

	boolean completionStatus;

	boolean setForReminder;

	LocalDateTime eventStartDate;

	LocalDateTime eventEndDate;
	
	@OneToOne(mappedBy = "owningItem", cascade = {CascadeType.ALL}, orphanRemoval = true)
	UserDefinedType userDefined;
	
	@Id
	@ManyToOne
	UserEntity owningUser;
	
	@Id
	@OneToOne
	UserGroup owningUserGroup;
}
