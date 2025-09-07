package com.qtodo.response;

import com.qtodo.model.Tag;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class TagDto {
	

	String name;
	public TagDto(Tag t) {
		this.name = t.getName();
	}

}
