package com.application.user_data;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class UserDataService {
    private final UserDataRepository userDataRepository;

    @Value("${amazon.s3.bucketname}")
    private String bucketName;

    private final S3Client s3Client;

    @Autowired
    public UserDataService(UserDataRepository userDataRepository, S3Client s3Client) {
        this.userDataRepository = userDataRepository;
        this.s3Client = s3Client;
    }

    public UserData getUserData(String username){
        return userDataRepository.findById(username).get();
    }


    /**
     * get profile picture
     */
    public byte[] getProfilePicture(String username){
        String objectName = userDataRepository.findById(username).get().getProfilePicture();
        return getFile(objectName);
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
}
