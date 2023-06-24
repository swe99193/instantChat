package com.application.message_storage;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;

@Configuration
@EnableDynamoDBRepositories
        (basePackages = "com.application.message_storage")
public class DynamoDBConfig {

    @Value("${spring.profiles.active}")
    private String profile;

    @Value("${amazon.dynamodb.endpoint}")
    private String amazonDynamoDBEndpoint;

    @Value("${amazon.aws.accesskey}")
    private String amazonAWSAccessKey;

    @Value("${amazon.aws.secretkey}")
    private String amazonAWSSecretKey;

    private final ArrayList<String> AWSProfileList = new ArrayList<String>(Arrays.asList("devAWS"));

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {

        if (AWSProfileList.contains(profile)) {
            return AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_EAST_1).build();

            // try this if below doesn't work
            // explicitly specify IAM as credential provider
            // ref: https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/java-dg-roles.html

//            return AmazonDynamoDBClientBuilder.standard().withCredentials(new InstanceProfileCredentialsProvider(true)).withRegion(Regions.US_EAST_1).build();
        }
        else {
            AmazonDynamoDB amazonDynamoDB
                    = new AmazonDynamoDBClient(amazonAWSCredentials());

            if (!StringUtils.isEmpty(amazonDynamoDBEndpoint)) {
                amazonDynamoDB.setEndpoint(amazonDynamoDBEndpoint);
            }

            return amazonDynamoDB;
        }
    }

    @Bean
    public AWSCredentials amazonAWSCredentials() {

        if (AWSProfileList.contains(profile))       // Note: IAM don't need credentials
            return null;

        return new BasicAWSCredentials(
                amazonAWSAccessKey, amazonAWSSecretKey);
    }
}
