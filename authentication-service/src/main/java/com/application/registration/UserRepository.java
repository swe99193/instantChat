package com.application.registration;

import com.application.registration.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<com.application.registration.User, String> {

    User findByUsername(String username);
}
