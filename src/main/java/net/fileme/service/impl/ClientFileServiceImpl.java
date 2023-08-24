package net.fileme.service.impl;

import net.fileme.pojo.File;

import net.fileme.service.ClientFileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ClientFileServiceImpl implements ClientFileService {

    @Value("${file.upload.dir}")
    private String uploadServerPath;
    @Override
    public File createFile(MultipartFile clientFile){
        File file = new File();
        String clientFilename = clientFile.getOriginalFilename();
        //要卡控否則產生NullPointer
        file.setFileName(clientFilename.substring(0, clientFilename.lastIndexOf(".")));
        file.setExt(clientFilename.substring(clientFilename.lastIndexOf(".") + 1));
        file.setSize(clientFile.getSize());
        return file;
    }

    @Override
    public String upload(MultipartFile clientFile
            , Long userId
            , Long fileId
            , boolean toRemote){

        if(!toRemote){
            String clientFileName = clientFile.getOriginalFilename();
            String ext = clientFileName.substring(clientFileName.lastIndexOf("."));
            java.io.File tmpFile = new java.io.File(uploadServerPath + "/" + userId + "/" + fileId + ext);
            if(!tmpFile.getParentFile().exists()){
                tmpFile.getParentFile().mkdirs();
            }
            if(tmpFile.exists()){
                //應該直接exception丟出來
                return "檔案已存在server"; // 之後要封裝 - errcode: server已有資料
            }
            try{
                clientFile.transferTo(tmpFile);
            }catch(Exception e){
                return "檔案寫入異常"; // 之後要封裝 - errcode: 檔案寫入異常
            }
            return "上傳成功";
        }
        return null;
    }
}
