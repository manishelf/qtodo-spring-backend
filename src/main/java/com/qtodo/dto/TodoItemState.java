package com.qtodo.dto;

import java.time.Instant;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TodoItemState{
	String uuid;
	Instant updationTimestamp;
	Long version;
	
	public TodoItemState(String uuid) {
		this.uuid = uuid;
		updationTimestamp = Instant.EPOCH;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if(!(obj instanceof TodoItemState)) return false;
		
		var o = (TodoItemState)obj;
		
		return this.uuid.equals(o.uuid);
	}
	
	@Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}