package net.fileme.service.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import net.fileme.domain.mapper.RemoveListMapper;
import net.fileme.domain.pojo.File;
import net.fileme.enums.ExceptionEnum;
import net.fileme.enums.MimeEnum;
import net.fileme.exception.InternalErrorException;
import net.fileme.service.RemoteDataService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


@Service
public class AwsDataServiceImpl implements RemoteDataService {
    @Value("${s3.bucketName}")
    private String bucketName;
    @Autowired
    private AmazonS3 s3;
    @Autowired
    private RemoveListMapper removeListMapper;

    @Override
    public void uploadRemote(MultipartFile part, File file) {
        StringBuilder builder = new StringBuilder();
        builder.append(file.getUserId()).append("/").append(file.getId()).append(".").append(file.getExt());
        String fileName = builder.toString();
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(MimeEnum.getMimeType(file.getExt())); // workaround
        objectMetadata.setContentLength(file.getSize());
        try{
            s3.putObject(bucketName, fileName, part.getInputStream(), objectMetadata);
        }catch (IOException e){
            throw new InternalErrorException(ExceptionEnum.FILE_IO_ERROR);
        }catch (AmazonServiceException e){
            throw new InternalErrorException(ExceptionEnum.REMOTE_ERROR);
        }catch (SdkClientException e){
            throw new InternalErrorException(ExceptionEnum.REMOTE_ERROR);
        }
        // 需要加setObjectAcl public read?
//        s3.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
//        fileObj.delete();
    }

    @Override
    public byte[] getRemoteByteArray(String fileName){
        S3Object s3Object = s3.getObject(bucketName, fileName);
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        try {
            byte[] content = IOUtils.toByteArray(inputStream);
            return content;
        } catch (IOException e) {
            throw new InternalErrorException(ExceptionEnum.FILE_IO_ERROR);
        }
    }
    @Override
    public void handleRemoteDelete(Long userId, List<Long> fileIds){
        List<String> remoteKeys = removeListMapper.getRemoteKeys(userId, fileIds);
        ObjectTagging tag = new ObjectTagging(Arrays.asList(new Tag("Delete", "")));

        try{

            for(String key : remoteKeys){
                s3.setObjectTagging(new SetObjectTaggingRequest(bucketName, key, tag));
            }

        }catch (AmazonServiceException e){
            throw new InternalErrorException(ExceptionEnum.REMOTE_ERROR);
        }catch (SdkClientException e){
            throw new InternalErrorException(ExceptionEnum.REMOTE_ERROR);
        }
    }
}
