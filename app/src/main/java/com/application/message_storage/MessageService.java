package com.application.message_storage;

import MessageServiceLib.*;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

    @Override
    public void saveMessage(SaveMessageRequest req, StreamObserver<SaveMessageResponse> responseObserver) {
        MessageServiceLib.Message m = req.getMessage();

        // convert to Message class
        Message message = new Message(m.getChannelId(), m.getTimestamp(),m.getSender(),m.getReceiver(), m.getContentType(), m.getContent());

        DynamoDBMapper mapper = new DynamoDBMapper(client);
        mapper.save(message);

        SaveMessageResponse res = SaveMessageResponse.newBuilder().setIsSuccess(true).build();
        responseObserver.onNext(res);
        responseObserver.onCompleted();
    }

    /**
    * query the database based on the partition key "channel id"
    * */
    @Override
    public void listMessage(ListMessageRequest req, StreamObserver<ListMessageResponse> responseObserver){
        String channel_id = req.getChannelId();

        DynamoDBMapper mapper = new DynamoDBMapper(client);
        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":val1", new AttributeValue().withS(channel_id));
        DynamoDBQueryExpression<Message> queryExpression = new DynamoDBQueryExpression<Message>().withKeyConditionExpression("channel_id = :val1")
                .withExpressionAttributeValues(eav);

        // Note: mapper.query has support for pagination
        List<Message> messageList = mapper.query(Message.class, queryExpression);

        // convert to gRPC Message class
        List<MessageServiceLib.Message> mList = new ArrayList<MessageServiceLib.Message>();
        for(Message message: messageList) {
            MessageServiceLib.Message m = MessageServiceLib.Message.newBuilder()
                    .setChannelId(message.getChannel_id())
                    .setTimestamp(message.getTimestamp())
                    .setSender(message.getSender())
                    .setReceiver(message.getReceiver())
                    .setContentType(message.getContentType())
                    .setContent(message.getContent())
                    .build();
            mList.add(m);
        }

        ListMessageResponse res = ListMessageResponse.newBuilder().addAllMessage(mList).build();
        responseObserver.onNext(res);
        responseObserver.onCompleted();
    }
}
