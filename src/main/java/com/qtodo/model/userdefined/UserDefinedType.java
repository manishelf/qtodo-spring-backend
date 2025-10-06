package com.qtodo.model.userdefined;

import java.util.Map;

import com.qtodo.model.Tag;
import com.qtodo.model.TodoItem;
import com.qtodo.model.UserDefinedTypeKey;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@IdClass(UserDefinedTypeKey.class)
public class UserDefinedType{
	
	@Id
	@ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
	Tag tag;
	
	@Id
	@OneToOne
	TodoItem owningItem;
	
	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	FormSchema formSchema;
   
	
	@ElementCollection
    @CollectionTable(name = "user_defined_data")
    @MapKeyColumn(name = "data_key")
    @Column(name = "data_value")
    Map<String, String> data;

}