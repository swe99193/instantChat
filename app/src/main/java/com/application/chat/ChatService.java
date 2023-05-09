package com.application.chat;

import com.application.channel_mapping.ChannelMappingService;
import com.application.message_storage.Message;
import com.application.message_storage.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@Service
public class ChatService {

    @Autowired
    private MessageService messageService;

    @Autowired
    private ChannelMappingService channelMappingService;

    public void saveMessage(String sender, String receiver, String content){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String channel_id = channelMappingService.findChannelId(sender, receiver);

        Message message = new Message(channel_id, timestamp.getTime(), sender, receiver, "text", content);
        messageService.saveMessage(message);
    }
}
