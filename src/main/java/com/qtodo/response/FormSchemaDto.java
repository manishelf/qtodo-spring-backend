package com.qtodo.response;

import java.util.List;
import java.util.stream.Collectors;

import com.qtodo.model.userdefined.FormSchema;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class FormSchemaDto {

	List<FormFieldDto> fields;
	
	public FormSchemaDto(FormSchema formSchema) {
		this.fields = formSchema.getFields().stream().map((f)->new FormFieldDto(f)).collect(Collectors.toList());
	}
}
