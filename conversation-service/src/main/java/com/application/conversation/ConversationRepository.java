package com.application.conversation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    List<Conversation> findByUser1OrUser2OrderByLatestTimestampDesc(String user1, String user2);

    // how to use @Query: https://www.baeldung.com/spring-data-jpa-query
    @Query(
            value = "SELECT conversation_id FROM conversation WHERE user1 = :user1 AND user2 = :user2",
            nativeQuery = true)
    String findConversationIdByUsers(@Param("user1") String user1, @Param("user2") String user2);


    @Modifying
    @Query(
            value = "UPDATE conversation SET latest_message = :latest_message, latest_timestamp = :latest_timestamp WHERE user1 = :user1 AND user2 = :user2",
            nativeQuery = true)
    void updateLatestMessage(@Param("user1") String user1, @Param("user2") String user2, @Param("latest_message") String latestMessage, @Param("latest_timestamp") Long latestTimestamp);


}