package com.qtodo.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.persistence.CascadeType;
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

	String alias;

	@NotBlank
	@Column(nullable = false)
	String email;

	String encryptedPassword;

	@OneToMany(mappedBy="owningUser")
	List<DocumentEntity> docs = new ArrayList<>();

	@OneToMany(mappedBy = "owningUser")
	List<TodoItem> todoItems = new ArrayList();
	
	@ManyToMany(mappedBy = "participantUsers", cascade = CascadeType.MERGE)
	List<UserGroup> participantInUserGroups = new ArrayList();
	
	@ManyToMany(mappedBy="owningUsers", cascade = CascadeType.MERGE)
	List<UserGroup> ownerOfUserGroups = new ArrayList();

	@Override
	public int hashCode() {
		return Objects.hash(email, participantInUserGroups.stream().map(ug->ug.groupTitle).collect(Collectors.toList()).toString());
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if(!(obj instanceof UserEntity)) return false;
		
		var o = (UserEntity)obj;
		return o.hashCode() == this.hashCode();
	}
	
}
