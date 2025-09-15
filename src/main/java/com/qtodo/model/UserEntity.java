package com.qtodo.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class UserEntity extends EntityBase {

	String alias;

	@NotBlank
	@Column(nullable = false)
	String email;

	String encryptedPassword;

	@OneToMany
	List<DocumentEntity> docs = new ArrayList<>();

	@OneToMany(mappedBy = "owningUser")
	List<TodoItem> todoItems = new ArrayList();
	
	@ManyToMany(mappedBy = "participantUsers", cascade = CascadeType.MERGE)
	List<UserGroup> participantInUserGroups = new ArrayList();
	
	@ManyToMany(mappedBy="owningUsers", cascade = CascadeType.MERGE)
	List<UserGroup> ownerOfUserGroups = new ArrayList();

}
