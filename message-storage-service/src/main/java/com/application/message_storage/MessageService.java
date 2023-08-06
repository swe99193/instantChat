package com.application.message_storage;

import MessageServiceLib.*;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.*;


// Dynamodb sdk ref:
// https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/examples-dynamodb-items.html
// https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBMapper.QueryScanExample.html

@GrpcService // also include @Service
public class MessageService extends MessageServiceGrpc.MessageServiceImplBase {

    private final AmazonDynamoDB client;

    @Autowired
    public MessageService(AmazonDynamoDB client) {
        this.client = client;
    }

    public void saveMessage(Message message){
        DynamoDBMapper mapper = new DynamoDBMapper(client);
        mapper.save(message);
    }

    /**
     * gRPC method, wrap internal function {@link MessageService#saveMessage(Message) saveMessage}
     */
    @Override
    public void saveMessage(SaveMessageRequest req, StreamObserver<SaveMessageResponse> responseObserver) {
        System.out.printf("🟢 gRPC message-storage-service: saveMessage   ---  %s\n", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));

        MessageServiceLib.Message m = req.getMessage();

        // convert to Message class
        Message message = new Message(m.getChannelId(), m.getTimestamp(),m.getSender(),m.getReceiver(), m.getContentType(), m.getContent());

        saveMessage(message);

        SaveMessageResponse res = SaveMessageResponse.newBuilder().setIsSuccess(true).build();
        responseObserver.onNext(res);
        responseObserver.onCompleted();
    }

    /**
    * query the database based on the partition key "channel id"
    * */
    public List<Message> listMessage(String channel_id){
        DynamoDBMapper mapper = new DynamoDBMapper(client);

        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":val1", new AttributeValue().withS(channel_id));

        DynamoDBQueryExpression<Message> queryExpression = new DynamoDBQueryExpression<Message>().withKeyConditionExpression("channel_id = :val1")
                .withExpressionAttributeValues(eav);

        // Note: mapper.query has support for pagination
        return mapper.query(Message.class, queryExpression);
    }

    /**
     * gRPC method, wrap internal function {@link MessageService#listMessage(String) listMessage}
     */
    @Override
    public void listMessage(ListMessageRequest req, StreamObserver<ListMessageResponse> responseObserver){
        System.out.printf("🟢 gRPC message-storage-service: listMessage   ---  %s\n", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));

        String channel_id = req.getChannelId();

        List<Message> messageList = listMessage(channel_id);

        // convert to gRPC Message class
        List<MessageServiceLib.Message> _messageList = new ArrayList<MessageServiceLib.Message>();
        for(Message message: messageList) {
            MessageServiceLib.Message m = MessageServiceLib.Message.newBuilder()
                    .setChannelId(message.getChannel_id())
                    .setTimestamp(message.getTimestamp())
                    .setSender(message.getSender())
                    .setReceiver(message.getReceiver())
                    .setContentType(message.getContentType())
                    .setContent(message.getContent())
                    .build();
            _messageList.add(m);
        }

        ListMessageResponse res = ListMessageResponse.newBuilder().addAllMessage(_messageList).build();
        responseObserver.onNext(res);
        responseObserver.onCompleted();
    }
}
