package com.qtodo.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.qtodo.model.userdefined.UserDefinedType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@IdClass(TodoItemKey.class)
public class TodoItem{
	
	@Id
	@NotBlank
	String subject;

	@Column(columnDefinition = "TEXT")
	String description;
	
	@ManyToMany(mappedBy="todoItems", cascade = {CascadeType.MERGE})
	List<Tag> tags = new ArrayList();

	boolean completionStatus;

	boolean setForReminder;

	LocalDateTime eventStartDate;

	LocalDateTime eventEndDate;
	
	@OneToOne(mappedBy = "owningItem", cascade = {CascadeType.ALL}, orphanRemoval = true)
	UserDefinedType userDefined;
	
	@Id
	@ManyToOne(cascade = CascadeType.MERGE)
	UserEntity owningUser;
	
	@Id
	@ManyToOne(cascade = CascadeType.MERGE)
	UserGroup owningUserGroup;
	

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	LocalDateTime creationTimestamp;
	
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	LocalDateTime updationTimestamp;
	
	boolean deleted;
}
