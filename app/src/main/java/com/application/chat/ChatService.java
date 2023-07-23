package com.application.chat;

import MessageServiceLib.ListMessageReply;
import MessageServiceLib.ListMessageRequest;
import MessageServiceLib.MessageServiceGrpc.MessageServiceBlockingStub;
import MessageServiceLib.SaveMessageReply;
import MessageServiceLib.SaveMessageRequest;
import com.application.channel_mapping.ChannelMappingService;
//import com.application.message_storage.Message;
import com.application.message_storage.Message;
import com.application.message_storage.MessageService;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {

    @GrpcClient("local-grpc-server")
    private MessageServiceBlockingStub messageService;

    private final ChannelMappingService channelMappingService;

    @Autowired
    public ChatService(ChannelMappingService channelMappingService) {
        this.channelMappingService = channelMappingService;
    }

    public void saveMessage(String sender, String receiver, String content){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String channel_id = channelMappingService.findChannelId(sender, receiver);

//        Message message = new Message(channel_id, timestamp.getTime(), sender, receiver, "text", content);

        // gRPC
        MessageServiceLib.Message message = MessageServiceLib.Message.newBuilder()
                .setChannelId(channel_id)
                .setTimestamp(timestamp.getTime())
                .setSender(sender)
                .setReceiver(receiver)
                .setContentType("text")
                .setContent(content)
                .build();
        SaveMessageRequest req = SaveMessageRequest.newBuilder().setMessage(message).build();
        SaveMessageReply reply = messageService.saveMessage(req);
        boolean isSuccess = reply.getIsSuccess();
    }

    public List<Message> listMessage(String sender, String receiver){

        String channel_id = channelMappingService.findChannelId(sender, receiver);

        // gRPC
        ListMessageRequest req = ListMessageRequest.newBuilder().setChannelId(channel_id).build();
        ListMessageReply reply = messageService.listMessage(req);
        List<MessageServiceLib.Message> mList = reply.getMessageList();

        // convert to Message Class
        List<Message> messageList = new ArrayList<Message>();
        for (MessageServiceLib.Message m: mList) {
            Message message = new Message(
                    m.getChannelId(),
                    m.getTimestamp(),
                    m.getSender(),
                    m.getReceiver(),
                    m.getContentType(),
                    m.getContent());
            messageList.add(message);
        }

        return messageList;
//		return new ArrayList<Message>(); // test: empty list
    }

}
