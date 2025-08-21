package com.qtodo.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;

import com.qtodo.model.Tag;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchCriteria {

	String subjectQuery; // subject like

	ArrayList<String> inDescOrSubjOrUd;

	Boolean completionStatus;

	Boolean setForReminder;

	ArrayList<Tag> includeTags;

	ArrayList<Tag> excludeTags;

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
