package de.dogneeds.backend.user.usergroup;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

public interface UserGroupRepository extends CrudRepository<UserGroup, Long> {
    @Query("SELECT ug FROM UserGroup ug WHERE ug.name = ?1")
    UserGroup findUserGroupByName(String name);

    @Query("SELECT ug.name FROM UserGroup ug")
    Collection<String> getUserGroupNames();
}
