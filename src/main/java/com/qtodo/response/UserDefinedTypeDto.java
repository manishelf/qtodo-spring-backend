package com.qtodo.response;

import java.util.Map;

import com.qtodo.model.userdefined.FormSchema;
import com.qtodo.model.userdefined.UserDefinedType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDefinedTypeDto {

	TagDto tag;
	
	FormSchemaDto formSchema;
	
    Map<String, String> data;
    
    public UserDefinedTypeDto(UserDefinedType userDefined) {
    	this.tag = new TagDto(userDefined.getTag());
    	this.formSchema = new FormSchemaDto(userDefined.getFormSchema());
    	this.data = userDefined.getData();
    }

	public UserDefinedType toEntity() {
		UserDefinedType ud = new UserDefinedType();
		ud.setTag(tag.toEntity());
		ud.setFormSchema(formSchema.toEntity());
		ud.setData(data);
		return ud;
	}
}
