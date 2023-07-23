package com.application.channel_mapping;

import com.application.registration.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface ChannelMappingRepository extends JpaRepository<ChannelMapping, UUID> {


    // how to use @Query: https://www.baeldung.com/spring-data-jpa-query
    @Query(
            value = "SELECT channel_id FROM channel_mapping m WHERE m.user1 = :user1 AND m.user2 = :user2",
            nativeQuery = true)
    String findChannelIdByUsers(@Param("user1") String user1, @Param("user2") String user2);
}