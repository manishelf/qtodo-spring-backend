package com.qtodo.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode
@IdClass(TodoItemKey.class)
public class TodoItem{
	
	@Column(unique = true, nullable = false)
	@NotBlank
	String uuid;
	
	@Id
	@NotBlank
	String subject;
	
	long version;

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
	@JsonIgnore
	UserEntity owningUser;
	
	@Id
	@ManyToOne(cascade = CascadeType.MERGE)
	@JsonIgnore
	UserGroup owningUserGroup;
	

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	Instant creationTimestamp;
	
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	Instant updationTimestamp;
	
	boolean deleted;
}
