package com.qtodo.dto;

import org.springframework.core.io.Resource;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentDto {
	String dataType;
	String info;
	Resource data;
}
