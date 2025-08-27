package com.qtodo.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;

import com.qtodo.response.TagDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchCriteria {

	String subjectQuery; // subject like

	ArrayList<String> inDescOrSubjOrUd;

	Boolean completionStatus;

	Boolean setForReminder;

	ArrayList<TagDto> includeTags;

	ArrayList<TagDto> excludeTags;

	LocalDateTime[] creationTimestampBetween;

	LocalDateTime[] updationTimestampBetween;

	LocalDateTime eventStartDate;

	LocalDateTime eventEndDate;

	Boolean deleted;

	Boolean exact;

	Integer pageNo;

	Integer limit;

	ArrayList<String> sortByField;
}
