package com.qtodo.response;

import java.util.Map;

import com.qtodo.model.userdefined.UserDefinedType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserDefinedTypeDto {

	TagDto tag;
	
	FormSchemaDto formControlSchema;
	
    Map<String, byte[]> data;
    
    public UserDefinedTypeDto(UserDefinedType userDefined) {
    	this.tag = new TagDto(userDefined.getTag());
    	this.formControlSchema = new FormSchemaDto(userDefined.getFormSchema());
    	this.data = userDefined.getData();
    }

}
