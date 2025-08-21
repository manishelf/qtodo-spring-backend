package com.qtodo.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class UserGroup extends EntityBase {

    @NotBlank
    @Column(unique = true, nullable = false)
    String groupTitle;
    
    // ... other fields

    @ManyToMany
    @JoinTable(
        name = "user_group_participants", 
        joinColumns = @JoinColumn(name = "user_group_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    List<UserEntity> participantUsers = new ArrayList();

    @ManyToMany
    @JoinTable(
        name = "user_group_owners",
        joinColumns = @JoinColumn(name = "user_group_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    List<UserEntity> owningUsers = new ArrayList();
}
