package com.qtodo.model;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qtodo.model.userdefined.UserDefinedType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class TodoItem extends EntityBase{
	
	@Column(unique = true, nullable = false)
	@NotBlank
	String subject;

	@Column(columnDefinition = "TEXT")
	String description;
	
	@ManyToMany(mappedBy="todoItems", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	List<Tag> tags;

	boolean completionStatus;

	boolean setForReminder;

	LocalDateTime eventStartDate;

	LocalDateTime eventEndDate;
	
	@OneToOne(mappedBy = "owningItem", cascade = {CascadeType.ALL}, orphanRemoval = true)
	UserDefinedType userDefined;
	
	@ManyToOne
	UserEntity owningUser;
}
