package com.application.user_data;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
public class UserDataService {
    private final UserDataRepository userDataRepository;

    @Value("${amazon.s3.bucketname}")
    private String bucketName;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Autowired
    public UserDataService(UserDataRepository userDataRepository, S3Client s3Client, S3Presigner s3Presigner) {
        this.userDataRepository = userDataRepository;
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    public UserData getUserData(String username){
        return userDataRepository.findById(username).get();
    }


    /**
     * Get profile picture url.
     */
    public String getProfilePictureUrl(String username){
        String objectName = userDataRepository.findById(username).get().getProfilePicture();
        return getPresignedUrl(objectName);
    }


    /**
     * update profile picture
     * <p></p>
     * filename format: profile-picture/(UUID)_(original filename)
     */
    @Transactional
    public void updateProfilePicture(String username, MultipartFile file) throws IOException {
        // delete old
        String _objectName = userDataRepository.findById(username).get().getProfilePicture();

        if(!_objectName.startsWith("profile-picture/default")){     // avoid delete default picture
            deleteFile(_objectName);
        }

        String objectName = String.format("profile-picture/%s_%s", UUID.randomUUID().toString(), file.getOriginalFilename());
        saveFile(objectName, file);
        userDataRepository.updateProfilePicture(username, objectName);
    }


    @Transactional
    public void updateStatusMessage(String username, String statusMessage) {

        userDataRepository.updateStatusMessage(username, statusMessage);
    }


    /**
     * upload file to S3
     */
    private void saveFile(String objectName, MultipartFile file) throws IOException {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectName)
                .build();

        InputStream inputStream = file.getInputStream();
        s3Client.putObject(objectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
        inputStream.close();
    }


    /**
     * download file from s3
     */
    public byte[] getFile(String objectName){

        //  download from S3
        GetObjectRequest objectRequest = GetObjectRequest
                .builder()
                .bucket(bucketName)
                .key(objectName)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(objectRequest);

        return objectBytes.asByteArray();
    }

    /**
     * delete file from s3
     */
    public void deleteFile(String objectName){
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectName)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    /**
     * Get S3 temporary download url (Get Object).
     */
    // ref: https://docs.aws.amazon.com/AmazonS3/latest/userguide/example_s3_Scenario_PresignedUrl_section.html
    private String getPresignedUrl(String objectName) {

        // get presigned url
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectName)
                .build();

        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(10))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
        String url = presignedGetObjectRequest.url().toString();
        log.info("✅ presign url (GetObject): " + url);
        return url;
    }
}
