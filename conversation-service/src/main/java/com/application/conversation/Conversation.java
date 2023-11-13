package com.application.conversation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "conversation")
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "conversation_id", nullable=false, unique = true)
    public UUID id;

    // Note: string comparison follow user1 < user2
    @Column(name = "user1", nullable=false)
    public String user1;

    @Column(name = "user2", nullable=false)
    public String user2;

    /**
     * latest message
     */
    @Column(name = "latest_message", nullable=false)
    public String latestMessage;

    /**
     * timestamp of the latest message
     */
    @Column(name = "latest_timestamp", nullable=false)
    public Long latestTimestamp;

    /**
     * user1's last read timestamp
     */
    @Column(name = "timestamp_user1", nullable=false)
    public Long timestampUser1;

    /**
     * user2's last read timestamp
     */
    @Column(name = "timestamp_user2", nullable=false)
    public Long timestampUser2;

    public Conversation(String user1, String user2) {
        this.user1 = user1;
        this.user2 = user2;
        this.latestMessage = "";
        this.latestTimestamp = 0L;
        this.timestampUser1 = 0L;
        this.timestampUser2 = 0L;
    }

}
