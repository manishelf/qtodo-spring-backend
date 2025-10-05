package com.qtodo.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.qtodo.auth.UserPermission;
import com.qtodo.model.PermissionForUserInUserGroup;
import com.qtodo.model.PermissionForUserInUserGroupKey;

@Repository
public interface PermissionForUserInUserGroupRepo extends JpaRepository<PermissionForUserInUserGroup, PermissionForUserInUserGroupKey>{

	@Query("FROM PermissionForUserInUserGroup pg WHERE pg.user.email = ?1 AND pg.userGroup.groupTitle = ?2")
	List<PermissionForUserInUserGroup> getByUserEmailAndGroupTitle(String email, String groupTitle);

	@Query("SELECT pg.permission FROM PermissionForUserInUserGroup pg WHERE pg.user.email = ?1 AND pg.userGroup.groupTitle = ?2 AND pg.enabled = true ")
	List<UserPermission> getEnabledPermByUserEmailAndGroupTitle(String email, String groupTitle);
	
	@Query("SELECT CASE WHEN COUNT(pg) > 0 THEN TRUE ELSE FALSE END "
	     + "FROM PermissionForUserInUserGroup pg "
	     + "WHERE pg.user.email = :userEmail "
	     + "AND pg.userGroup.groupTitle = :groupTitle "
	     + "AND pg.permission = :permission")
	boolean doesUserHavePermissionInGroup(
	    @Param("userEmail") String userEmail, 
	    @Param("groupTitle") String groupTitle, 
	    @Param("permission") UserPermission permission
	);
}
