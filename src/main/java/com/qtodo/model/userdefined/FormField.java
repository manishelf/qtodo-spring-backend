package com.qtodo.model.userdefined;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class FormField {
	private String name;
    private String label;
    private String type;
    private String placeholder;
    @Embedded
    private FormFieldValidation validation;
    private String options;
    
    @Column(columnDefinition = "TEXT")
    private String defaultValue;
}
