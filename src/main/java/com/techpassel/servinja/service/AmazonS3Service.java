package com.techpassel.servinja.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

@Service
public class AmazonS3Service {

    private AmazonS3 amazonS3;

    @Value("${endpointUrl}")
    private String endpointUrl;
    @Value("${bucketName}")
    private String bucketName;
    @Value("${accessKey}")
    private String accessKey;
    @Value("${secretKey}")
    private String secretKey;

    @PostConstruct
    private void initializeAmazon() {
        try {
            AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
            this.amazonS3 = new AmazonS3Client(credentials);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public String uploadFile(MultipartFile multipartFile) {
        String fileURL = "";
        try {
            File file = convertMultipartFileToFile(multipartFile);
            String fileName = multipartFile.getOriginalFilename();
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
            Random r = new Random();
            String fileNewName = "serv-"+((new Date()).getTime())+(r.nextInt(100))+"."+extension;
            fileURL = endpointUrl + "/" + bucketName + "/" + fileNewName;
            uploadFileToBucket(fileNewName, file);
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileURL;
    }

    private File convertMultipartFileToFile(MultipartFile file) throws IOException {
        File convertedFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convertedFile);
        fos.write(file.getBytes());
        fos.close();
        return convertedFile;
    }

    private void uploadFileToBucket(String fileName, File file) {
        amazonS3.putObject(new PutObjectRequest(bucketName, fileName, file)
                .withCannedAcl(CannedAccessControlList.PublicRead));
    }
}
