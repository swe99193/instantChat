package com.application.channel_mapping;

import com.application.conversation_list.ConversationUser;
import com.application.conversation_list.ConversationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChannelMappingService {

    private final ChannelMappingRepository channelMappingRepository;
    private final ConversationUserService conversationUserService;

    @Autowired
    public ChannelMappingService(ChannelMappingRepository channelMappingRepository, ConversationUserService conversationUserService) {
        this.channelMappingRepository = channelMappingRepository;
        this.conversationUserService = conversationUserService;
    }

    /**
     * Get channel id given two users.
     * If channel id not found, create a new one, and create a new conversationUser record.
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

            // create an entry for each user
            conversationUserService.saveConversation(new ConversationUser(user1, user2));
            conversationUserService.saveConversation(new ConversationUser(user2, user1));
            return channel_id;
        }else{
            return channel_id;
        }
    }

}
