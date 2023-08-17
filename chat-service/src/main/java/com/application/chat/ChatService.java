package com.application.chat;

import ChannelMappingServiceLib.ChannelMappingServiceGrpc.ChannelMappingServiceBlockingStub;
import ChannelMappingServiceLib.findChannelIdRequest;
import ChannelMappingServiceLib.findChannelIdResponse;
import com.application.message_storage.Message;
import com.application.message_storage.MessageService;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class ChatService {
    // FIXME: gRPC client cannot be Autowired

    @GrpcClient("grpc-server-channel-mapping")
    private ChannelMappingServiceBlockingStub channelMappingService;

    private final MessageService messageService;


    @Autowired
    public ChatService(MessageService messageService) {
        this.messageService = messageService;
    }

    public void saveMessage(String sender, String receiver, String content){
//        String channel_id = channelMappingService.findChannelId(sender, receiver);

        // gRPC, get channel_id
        findChannelIdRequest _req = findChannelIdRequest.newBuilder().setUser1(sender).setUser2(receiver).build();
        findChannelIdResponse _res = channelMappingService.findChannelId(_req);
        String channel_id = _res.getChannelId();

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Message message = new Message(channel_id, timestamp.getTime(), sender, receiver, "text", content);

        messageService.saveMessage(message);
    }

    public List<Message> listMessage(String sender, String receiver){

//        String channel_id = channelMappingService.findChannelId(sender, receiver);

        // gRPC, get channel_id
        findChannelIdRequest _req = findChannelIdRequest.newBuilder().setUser1(sender).setUser2(receiver).build();
        findChannelIdResponse _res = channelMappingService.findChannelId(_req);
        String channel_id = _res.getChannelId();

        List<Message> messageList = messageService.listMessage(channel_id);

        return messageList;
    }

}
