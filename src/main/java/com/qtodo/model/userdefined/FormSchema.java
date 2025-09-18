package com.qtodo.model.userdefined;

import java.util.List;

import com.qtodo.model.EntityBase;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class FormSchema extends EntityBase{

    @ElementCollection
    @CollectionTable(name = "form_fields", joinColumns = @JoinColumn(name = "form_schema_id"))
    List<FormField> fields;
	
	@OneToMany(mappedBy="formSchema")
	List<UserDefinedType> userDefined;
}
