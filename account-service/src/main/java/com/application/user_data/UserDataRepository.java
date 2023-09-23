package com.application.user_data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserDataRepository extends JpaRepository<UserData, String> {

    @Modifying(clearAutomatically = true)
    @Query(value = "update UserData t set t.profilePicture = :profilePicture where t.username = :username")
    void updateProfilePicture(@Param("username") String username, @Param("profilePicture") String profilePicture);

    @Modifying(clearAutomatically = true)
    @Query(value = "update UserData t set t.statusMessage = :statusMessage where t.username = :username")
    void updateStatusMessage(@Param("username") String username, @Param("statusMessage") String statusMessage);
}