package com.qtodo.model.userdefined;

import java.util.Map;

import com.qtodo.model.EntityBase;
import com.qtodo.model.Tag;
import com.qtodo.model.TodoItem;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class UserDefinedType extends EntityBase{
	
	@OneToOne(mappedBy = "userDefined", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	Tag tag;
	
	@OneToOne
	TodoItem owningItem;
	
	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	FormSchema formSchema;
   
	
	@ElementCollection
    @CollectionTable(name = "user_defined_data", joinColumns = @JoinColumn(name = "user_defined_id"))
    @MapKeyColumn(name = "data_key")
    @Column(name = "data_value")
    Map<String, String> data;

}