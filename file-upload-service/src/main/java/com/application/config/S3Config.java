package com.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.ArrayList;
import java.util.Arrays;


@Configuration
public class S3Config {

    @Value("${spring.profiles.active}")
    private static String profile;

    private static final ArrayList<String> AWSProfileList = new ArrayList<String>(Arrays.asList("devAWS"));


    /**
     * building S3 client (SDK v2)
     */
    @Bean
    public S3Client s3Client(
            @Value("${amazon.aws.accesskey}") String awsAccessKey,
            @Value("${amazon.aws.secretkey}") String awsSecretKey
    ){

        if (AWSProfileList.contains(profile)) {
            // automatically load credentials by EC2
            return S3Client.builder()
                    .region(Region.US_EAST_1)
                    .build();
        }
        else {
            return S3Client.builder()
                    .region(Region.US_EAST_1)
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(awsAccessKey, awsSecretKey)))
                    .build();
        }
    }
}
