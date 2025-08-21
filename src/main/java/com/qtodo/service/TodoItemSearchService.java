package com.qtodo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.qtodo.dao.TodoItemRepo;
import com.qtodo.dto.SearchCriteria;
import com.qtodo.model.Tag;
import com.qtodo.model.TodoItem;

import jakarta.persistence.criteria.Predicate;

@Service
public class TodoItemSearchService {

	@Autowired
	private TodoItemRepo todoItemRepo;

	public List<TodoItem> searchByCriteria(SearchCriteria query) {
		Specification<TodoItem> spec = buildSpecification(query);
		Sort sort = Sort.by(query.getSortByField().toArray(new String[0]));
		Pageable pageable = PageRequest.of(query.getPageNo(), query.getLimit(), sort);
		return todoItemRepo.findAll(spec);
	}

	private Specification<TodoItem> buildSpecification(SearchCriteria query) {
		return (root, criteriaQuery, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			// 1. Subject Like Query
			if (query.getSubjectQuery() != null && !query.getSubjectQuery().isEmpty()) {
				String likePattern = "%" + query.getSubjectQuery().toLowerCase() + "%";
				predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("subject")), likePattern));
			}

			// 2. Search Text Query (in subject, description, or userDefined)
			if (query.getInDescOrSubjOrUd() != null && !query.getInDescOrSubjOrUd().isEmpty()) {
				for (String searchTerm : query.getInDescOrSubjOrUd()) {
					String likePattern = "%" + searchTerm.toLowerCase() + "%";
					predicates.add(criteriaBuilder.or(
							criteriaBuilder.like(criteriaBuilder.lower(root.get("subject")), likePattern),
							criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likePattern),
							criteriaBuilder.like(criteriaBuilder.lower(root.get("userDefined")), likePattern)));
				}
			}

			// 3. Includes Tag
			if (query.getIncludeTags() != null && !query.getIncludeTags().isEmpty()) {
				predicates.add(root.join("tags").get("name").in(getTagNames(query.getIncludeTags())));
			}

			// 4. Excludes Tag
			if (query.getExcludeTags() != null && !query.getExcludeTags().isEmpty()) {
				predicates.add(
						criteriaBuilder.not(root.join("tags").get("name").in(getTagNames(query.getExcludeTags()))));
			}

			// 5. Completion Status
			if (query.getCompletionStatus() != null) {
				predicates.add(criteriaBuilder.equal(root.get("completionStatus"), query.getCompletionStatus()));
			}

			// 6. Set For Reminder
			if (query.getSetForReminder() != null) {
				predicates.add(criteriaBuilder.equal(root.get("setForReminder"), query.getSetForReminder()));
			}

			// 7. Creation Timestamp Range
			if (query.getCreationTimestampBetween() != null && query.getCreationTimestampBetween().length == 2) {
				predicates.add(criteriaBuilder.between(root.get("creationTimestamp"),
						query.getCreationTimestampBetween()[0], query.getCreationTimestampBetween()[1]));
			}

			// 8. Updation Timestamp Range
			if (query.getUpdationTimestampBetween() != null && query.getUpdationTimestampBetween().length == 2) {
				predicates.add(criteriaBuilder.between(root.get("updationTimestamp"),
						query.getUpdationTimestampBetween()[0], query.getUpdationTimestampBetween()[1]));
			}

			// 9. Event Start Date
			if (query.getEventStartDate() != null) {
				predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventStart"), query.getEventStartDate()));
			}

			// 10. Event End Date
			if (query.getEventEndDate() != null) {
				predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("eventEnd"), query.getEventEndDate()));
			}

			// 11. Deleted Status
			if (query.getDeleted() != null) {
				predicates.add(criteriaBuilder.equal(root.get("deleted"), query.getDeleted()));
			}

			// Combine all predicates with AND
			if (query.getExact()) {
				return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
			} else {
				return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
			}
		};
	}

	private List<String> getTagNames(List<Tag> tags) {
		List<String> tagNames = new ArrayList<>();
		if (tags != null) {
			for (Tag tag : tags) {
				tagNames.add(tag.getName());
			}
		}
		return tagNames;
	}
}
