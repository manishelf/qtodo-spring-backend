package com.qtodo.response;

import com.qtodo.model.userdefined.FormFieldValidation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FormFieldValidationDto {
	 private Boolean require;
	 private Boolean readonly;
	 private Integer minLength;
	 private Integer maxLength;
	 private String pattern;
	 private String min;
	 private String max;
	 private String step;

	public FormFieldValidationDto(FormFieldValidation validation) {
		this.require = validation.getRequire();
		this.readonly = validation.getReadonly();
		this.minLength = validation.getMinLength();
		this.maxLength = validation.getMaxLength();
		this.pattern = validation.getPattern();
		this.min = validation.getMin();
		this.max = validation.getMax();
		this.step = validation.getStep();
	}

	public FormFieldValidation store() {
		FormFieldValidation fe = new FormFieldValidation();
		fe.setRequire(require);
		fe.setReadonly(readonly);
		fe.setMinLength(minLength);
		fe.setMaxLength(maxLength);
		fe.setMin(min);
		fe.setMax(max);
		fe.setStep(step);
		fe.setPattern(pattern);
		return fe;
	}
}
