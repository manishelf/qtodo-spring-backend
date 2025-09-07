package com.qtodo.model;

import java.io.Serializable;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TodoItemKey implements Serializable {
	
	String subject;
	
	UserGroup owningUserGroup;
	
	UserEntity owningUser;

	@Override
	public int hashCode() {
		return Objects.hash(subject, owningUserGroup, owningUser);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		
		if(obj == this) return true;
		
		if(!(obj instanceof TodoItemKey)) {
			return false;
		}
		
		TodoItemKey o = (TodoItemKey) obj;
		
		boolean match = true;
	
		match &= o.subject.equals(subject);
		match &= o.owningUserGroup.getGroupTitle().equals(owningUserGroup.getGroupTitle());
		match &= o.getOwningUser().getEmail().equals(owningUser.getEmail());
		
		return match;
	}
	
	
}
