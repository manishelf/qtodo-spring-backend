package com.qtodo.response;

import com.qtodo.model.userdefined.FormField;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class FormFieldDto {
	private String name;
    private String label;
    private String type;
    private String placeholder;
    private FormFieldValidationDto validation;
    private String options;
    private String defaultValue;
    
   public FormFieldDto(FormField f) {
	   this.name = f.getName();
	   this.label = f.getLabel();
	   this.type = f.getType();
	   this.placeholder = f.getPlaceholder();
	   
	   if(f.getValidation() != null)
	   this.validation = new FormFieldValidationDto(f.getValidation());
	   
	   this.options = f.getOptions();
	   this.defaultValue = f.getDefaultValue();
	}

   public FormField  store() {
	   FormField ff = new FormField();
	   ff.setName(name);
	   ff.setLabel(label);
	   ff.setType(type);
	   ff.setPlaceholder(placeholder);
	   
	   if(validation != null)
	   ff.setValidation(validation.store());
	   
	   ff.setOptions(options);
	   ff.setDefaultValue(defaultValue);
	   
	   return ff;
   }

}
