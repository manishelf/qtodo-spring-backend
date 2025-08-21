package com.qtodo.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public class EntityBase {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "document_seq_gen")
	@SequenceGenerator(name = "document_seq_gen", sequenceName = "document_sequence", allocationSize = 1) 
	// because of how h2 in hybernate dialect works Identity is working
	Long id;
	
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	LocalDateTime creationTimestamp;
	
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	LocalDateTime updationTimestamp;
	
	boolean deleted;
}
