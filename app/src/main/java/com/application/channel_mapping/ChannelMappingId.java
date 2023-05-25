package com.application.channel_mapping;

import java.io.Serializable;

// Composite Primary Keys in JPA (This class is not used now)
public class ChannelMappingId implements Serializable {
    private String user1;

    private String user2;

    public ChannelMappingId(String user1, String user2) {
        this.user1 = user1;
        this.user2 = user2;
    }
}
