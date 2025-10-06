package com.qtodo.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
public class Tag{
	@Id
	@NotBlank
    String name;
	
	@ManyToMany
	@JsonIgnore
	List<TodoItem> todoItems = new ArrayList();
	
	@Override
	public String toString() {
		return "(name="+this.name+")";
	}
}