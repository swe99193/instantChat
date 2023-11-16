package com.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.util.ArrayList;
import java.util.Arrays;


@Configuration
public class S3Config {

    @Value("${spring.profiles.active}")
    private static String profile;

    private static final ArrayList<String> AWSProfileList = new ArrayList<String>(Arrays.asList("devAWS"));

    @Bean
    public S3Presigner s3Presigner(
            @Value("${amazon.aws.accesskey}") String awsAccessKey,
            @Value("${amazon.aws.secretkey}") String awsSecretKey
    ){

        if (AWSProfileList.contains(profile)) {
            // automatically load credentials by EC2
            return S3Presigner.builder()
                    .region(Region.US_EAST_1)
                    .build();
        }
        else {
            return S3Presigner.builder()
                    .region(Region.US_EAST_1)
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(awsAccessKey, awsSecretKey)))
                    .build();
        }
    }


}
