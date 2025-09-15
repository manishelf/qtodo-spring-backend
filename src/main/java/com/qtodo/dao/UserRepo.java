package com.qtodo.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.qtodo.model.DocumentEntity;
import com.qtodo.model.UserEntity;

@Repository
public interface UserRepo extends JpaRepository<UserEntity, Long>{

	UserEntity getByEmail(String email);

	@Query("SELECT u.id FROM UserEntity u WHERE u.email = ?1")
	Long getIdByEncryptedEmail(String email);

	@Query("SELECT d FROM UserEntity u JOIN u.docs d WHERE u.id = ?1 AND d.info = ?2")
	Optional<DocumentEntity> getProfilePicByUserId(Long id, String info);
	
	@Query("SELECT u FROM UserEntity u JOIN u.participantInUserGroups ug WHERE u.email = ?1 AND ug.groupTitle = ?2")
	UserEntity getByEmailInUserGroup(String email, String userGroupTitle);
}
