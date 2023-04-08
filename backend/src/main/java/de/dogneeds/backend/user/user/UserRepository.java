package de.dogneeds.backend.user.user;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

public interface UserRepository extends CrudRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.email = ?1 AND u.password = ?2")
    User findByEmailAndPassword(String email, String password);

    @Query("SELECT u FROM User u WHERE u.email = ?1")
    User isEmailRegistered(String email);

    @Query("SELECT u FROM User u")
    Collection<User> getAllUsers();

    @Query("SELECT u FROM User u WHERE u.uid = ?1")
    User getUserById(Long uid);
}
