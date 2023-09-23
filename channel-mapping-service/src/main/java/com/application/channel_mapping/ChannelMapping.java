package com.application.channel_mapping;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "channel_mapping")
//@IdClass(ChannelMappingId.class)
public class ChannelMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable=false, unique = true)
    private UUID channel_id;

    // Note: string comparison follow user1 < user2
    @Column(nullable=false)
    private String user1;

    @Column(nullable=false)
    private String user2;


    public ChannelMapping(String user1, String user2) {
        this.user1 = user1;
        this.user2 = user2;
    }

}
