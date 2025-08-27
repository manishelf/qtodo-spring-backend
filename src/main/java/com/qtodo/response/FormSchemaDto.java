package com.qtodo.response;

import java.util.List;
import java.util.stream.Collectors;

import com.qtodo.model.userdefined.FormSchema;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FormSchemaDto {

	List<FormFieldDto> fields;

	public FormSchemaDto(FormSchema formSchema) {
		this.fields = formSchema.getFields().stream().map((f)->new FormFieldDto(f)).collect(Collectors.toList());
	}

	public FormSchema toEntity() {
		FormSchema f = new FormSchema();
		f.setFields(fields.stream().map(ff->ff.toEntity()).collect(Collectors.toList()));
		return f;
	}
}
