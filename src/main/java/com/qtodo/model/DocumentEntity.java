package com.qtodo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class DocumentEntity extends EntityBase{
	
	@Column(unique = true, nullable = false)
	String refUrl;
	
	String info;
	
	String dataType;

	@Lob
	@Column(columnDefinition = "BLOB")
	byte[] content;
	
	@ManyToOne
	UserEntity owningUser;
	
	@ManyToOne
	UserGroup owningUserGroup;
}
