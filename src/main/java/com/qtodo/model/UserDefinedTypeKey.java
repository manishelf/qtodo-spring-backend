package com.qtodo.model;

import java.io.Serializable;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDefinedTypeKey implements Serializable{
	
	Tag tag;
	
	TodoItem owningItem;

	@Override
	public int hashCode() {
		return Objects.hash(tag.name, owningItem.uuid);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if(!(obj instanceof UserDefinedTypeKey)) return false;
		
		var o = (UserDefinedTypeKey) obj;
		
		return tag.name.equals(o.tag.name) && owningItem.equals(o.owningItem);
	}
	
	
	
}
