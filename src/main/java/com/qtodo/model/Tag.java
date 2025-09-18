package com.qtodo.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qtodo.model.userdefined.UserDefinedType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
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