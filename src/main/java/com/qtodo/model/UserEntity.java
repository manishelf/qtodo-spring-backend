package com.qtodo.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class UserEntity extends EntityBase {

	String firstName;

	String LastName;

	@NotBlank
	@Column(nullable = false)
	String email;

	String encryptedPassword;

	@OneToMany
	List<DocumentEntity> docs = new ArrayList<>();

	@OneToMany(mappedBy = "owningUser")
	List<TodoItem> todoItems;
	
	@ManyToMany(mappedBy = "participantUsers")
	List<UserGroup> participantInUserGroups;
	
	@ManyToMany(mappedBy="owningUsers")
	List<UserGroup> ownerOfUserGroups;
}
