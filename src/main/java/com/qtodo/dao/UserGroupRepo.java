package com.qtodo.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.qtodo.model.UserEntity;
import com.qtodo.model.UserGroup;

@Repository
public interface UserGroupRepo extends JpaRepository<UserGroup, Long> {

	UserGroup getByGroupTitle(String string);

	@Query("SELECT ug.participantUsers from UserGroup ug where ug.groupTitle = ?1")
	List<UserEntity> getParticipantUsersByGroupTitle(String userGroupTitle);

	@Query("SELECT ug from UserGroup ug where ug.open = true")
	List<UserGroup> findAllOpen();

}
