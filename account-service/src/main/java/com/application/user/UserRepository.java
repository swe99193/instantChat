package com.application.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    User findByUsername(String username);
    Boolean existsByUsername(String username);

    @Modifying(clearAutomatically = true)
    @Query("update User t set t.password = :password where t.username = :username")
    void updatePassword(@Param("username") String username, @Param("password") String password);

}
