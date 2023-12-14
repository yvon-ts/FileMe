package net.fileme.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import net.fileme.domain.pojo.File;
import net.fileme.service.RemoteDataService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;


@Service
public class AwsDataServiceImpl implements RemoteDataService {
    @Value("${s3.bucketName}")
    private String bucketName;
    @Autowired
    private AmazonS3 s3;

    @Override
    public void upload(MultipartFile part, File file) {
        java.io.File fileObj = convertToFile(part);
        String fileName = file.getUserId() + "_" + file.getId();
        s3.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
        fileObj.delete();
        System.out.println("ya finishh!!!!!!!");
    }

    @Override
    public byte[] download(String fileName){
        S3Object s3Object = s3.getObject(bucketName, fileName);
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        try {
            byte[] content = IOUtils.toByteArray(inputStream);
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public void delete(String fileName){
        s3.deleteObject(bucketName, fileName);
        System.out.println("deleted!!!!!!");
    }
    private java.io.File convertToFile(MultipartFile part){
        java.io.File convertedFile = new java.io.File(part.getOriginalFilename());
        try(FileOutputStream fos = new FileOutputStream(convertedFile)){
            fos.write(part.getBytes());
        }catch (IOException e){
            e.printStackTrace();
        }
        return convertedFile;
    }
}
