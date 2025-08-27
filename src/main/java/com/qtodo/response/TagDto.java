package com.qtodo.response;

import com.qtodo.model.Tag;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TagDto {
	

	String name;
	
	public TagDto(Tag t) {
		this.name = t.getName();
	}

	public Tag toEntity() {
		Tag t = new Tag();
		t.setName(name);
		return t;
	}
}
