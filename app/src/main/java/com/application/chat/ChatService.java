package com.application.chat;

import ChannelMappingServiceLib.ChannelMappingServiceGrpc.ChannelMappingServiceBlockingStub;
import ChannelMappingServiceLib.findChannelIdRequest;
import ChannelMappingServiceLib.findChannelIdResponse;
import MessageServiceLib.ListMessageResponse;
import MessageServiceLib.ListMessageRequest;
import MessageServiceLib.MessageServiceGrpc.MessageServiceBlockingStub;
import MessageServiceLib.SaveMessageResponse;
import MessageServiceLib.SaveMessageRequest;
import com.application.message_storage.Message;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {
    // FIXME: gRPC client cannot be Autowired

    @GrpcClient("grpc-server-message")
    private MessageServiceBlockingStub messageService;

    @GrpcClient("grpc-server-channel-mapping")
    private ChannelMappingServiceBlockingStub channelMappingService;

//    @Autowired
//    public ChatService(MessageServiceBlockingStub messageService, ChannelMappingServiceBlockingStub channelMappingService) {
//        this.messageService = messageService;
//        this.channelMappingService = channelMappingService;
//    }

    public void saveMessage(String sender, String receiver, String content){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//        String channel_id = channelMappingService.findChannelId(sender, receiver);

//        Message message = new Message(channel_id, timestamp.getTime(), sender, receiver, "text", content);

        // gRPC
        findChannelIdRequest _req = findChannelIdRequest.newBuilder().setUser1(sender).setUser2(receiver).build();
        findChannelIdResponse _res = channelMappingService.findChannelId(_req);
        String channel_id = _res.getChannelId();

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
        SaveMessageResponse res = messageService.saveMessage(req);
        boolean isSuccess = res.getIsSuccess();
    }

    public List<Message> listMessage(String sender, String receiver){

//        String channel_id = channelMappingService.findChannelId(sender, receiver);

        // gRPC
        findChannelIdRequest _req = findChannelIdRequest.newBuilder().setUser1(sender).setUser2(receiver).build();
        findChannelIdResponse _res = channelMappingService.findChannelId(_req);
        String channel_id = _res.getChannelId();

        // gRPC
        ListMessageRequest req = ListMessageRequest.newBuilder().setChannelId(channel_id).build();
        ListMessageResponse res = messageService.listMessage(req);
        List<MessageServiceLib.Message> mList = res.getMessageList();

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
