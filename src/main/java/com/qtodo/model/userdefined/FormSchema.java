package com.qtodo.model.userdefined;

import java.util.List;
import java.util.Map;

import com.qtodo.model.EntityBase;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;

@Entity
public class FormSchema extends EntityBase{

    @ElementCollection
    @CollectionTable(name = "form_fields", joinColumns = @JoinColumn(name = "form_schema_id"))
    private List<FormField> fields;
	
	@OneToMany
	List<UserDefinedType> userDefined;
}
