package com.application.channel_mapping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChannelMappingService {

    @Autowired
    private final ChannelMappingRepository channelMappingRepository;

    public ChannelMappingService(ChannelMappingRepository channelMappingRepository) {
        this.channelMappingRepository = channelMappingRepository;
    }

    /**
     * get channel id by users
     *
     * if channel id not found, create a new one
    */
    public String findChannelId(String user1, String user2){
        // swap to ensure user1 < user2
        if (user1.compareTo(user2) > 0){
            String tmp = user1;
            user1 = user2;
            user2 = tmp;
        }

        String channel_id = channelMappingRepository.findChannelIdByUsers(user1, user2);

        if (channel_id == null){
            // create a new channel mapping
            ChannelMapping channelMapping = new ChannelMapping(user1, user2);
            channelMappingRepository.save(channelMapping);
            channel_id = channelMappingRepository.findChannelIdByUsers(user1, user2);
            return channel_id;
        }else{
            return channel_id;
        }
    }

}
