package com.application.config;

// aws sdk v1
//import com.amazonaws.auth.AWSCredentials;
//import com.amazonaws.auth.BasicAWSCredentials;
//import com.amazonaws.regions.Regions;
//import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
//import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
//import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
//import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.ArrayList;
import java.util.Arrays;


// Dynamodb sdk v2 DynamoDbClient:
// https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/CodeSamples.Java.html
@Configuration
public class DynamoDBConfig {

    @Value("${spring.profiles.active}")
    private static String profile;

    private static final ArrayList<String> AWSProfileList = new ArrayList<String>(Arrays.asList("devAWS"));


    /**
     * building dynamodb client (SDK v2)
     */
    @Bean
    public DynamoDbClient dynamoDbClient(
            @Value("${amazon.aws.accesskey}") String awsAccessKey,
            @Value("${amazon.aws.secretkey}") String awsSecretKey
    ){

        if (AWSProfileList.contains(profile)) {
            return DynamoDbClient.builder()
                    .region(Region.US_EAST_1)
                    .build();
        }
        else {
            return DynamoDbClient.builder()
                    .region(Region.US_EAST_1)
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(awsAccessKey, awsSecretKey)))
                    .build();
        }
    }

    /* building dynamodb client SDK v1 */
//    @Bean
//    public AmazonDynamoDB amazonDynamoDB() {
//
//        if (AWSProfileList.contains(profile)) {
//            return AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
//
//            // try this if below doesn't work
//            // explicitly specify IAM as credential provider
//            // ref: https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/java-dg-roles.html
//
////            return AmazonDynamoDBClientBuilder.standard().withCredentials(new InstanceProfileCredentialsProvider(true)).withRegion(Regions.US_EAST_1).build();
//        }
//        else {
//            AmazonDynamoDB amazonDynamoDB
//                    = new AmazonDynamoDBClient(amazonAWSCredentials());
//
//            if (!StringUtils.isEmpty(amazonDynamoDBEndpoint)) {
//                amazonDynamoDB.setEndpoint(amazonDynamoDBEndpoint);
//            }
//
//            return amazonDynamoDB;
//        }
//    }

//    @Bean
//    public AWSCredentials amazonAWSCredentials() {
//
//        if (AWSProfileList.contains(profile))       // Note: IAM don't need credentials
//            return null;
//
//        return new BasicAWSCredentials(
//                amazonAWSAccessKey, amazonAWSSecretKey);
//    }

}
