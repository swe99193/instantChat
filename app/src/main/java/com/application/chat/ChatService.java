package com.application.chat;

import com.application.channel_mapping.ChannelMappingService;
import com.application.message_storage.Message;
import com.application.message_storage.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.sql.Timestamp;
import java.util.List;

@Service
public class ChatService {

    private final MessageService messageService;
    private final ChannelMappingService channelMappingService;

    @Autowired
    public ChatService(MessageService messageService, ChannelMappingService channelMappingService) {
        this.messageService = messageService;
        this.channelMappingService = channelMappingService;
    }

    public void saveMessage(String sender, String receiver, String content){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String channel_id = channelMappingService.findChannelId(sender, receiver);

        Message message = new Message(channel_id, timestamp.getTime(), sender, receiver, "text", content);
        messageService.saveMessage(message);    // TODO: gRPC
    }

    public List<Message> listMessage(String sender, String receiver){

        String channel_id = channelMappingService.findChannelId(sender, receiver);

        List<Message> messageList = messageService.listMessage(channel_id);	// TODO: gRPC
        return messageList;
//		return new ArrayList<Message>(); // test: empty list
    }

}
