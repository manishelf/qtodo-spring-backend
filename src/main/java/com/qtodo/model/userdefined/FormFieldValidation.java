package com.qtodo.model.userdefined;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class FormFieldValidation {
    private Boolean require;
    private Boolean readonly;
    private Integer minLength;
    private Integer maxLength;
    private String pattern;
    private String min;
    private String max;
    private String step;
}