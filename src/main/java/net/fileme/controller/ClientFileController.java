package net.fileme.controller;

import net.fileme.mapper.FileMapper;
import net.fileme.mapper.FolderMapper;
import net.fileme.pojo.File;
import net.fileme.service.ClientFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ClientFileController {
    @Autowired
    private ClientFileService clientFileService;
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private FolderMapper folderMapper;

    /**
     *
     * @param clientFile
     * @param userId：之後要改從token拿取？
     * @param folderId
     * @return
     */
    @PostMapping("/drive/upload")
    public String upload(@RequestPart("file") MultipartFile clientFile
            , @RequestPart("userId") String userId
            , @RequestPart("folderId") String folderId){

        if(clientFile.isEmpty()){
            return "文件為空"; // 之後要封裝 - errcode: 上傳文件為空
        }
        Long userIdLong = (long) Integer.parseInt(userId);
        File file = clientFileService.createFile(clientFile);
        file.setUserId(userIdLong);
        file.setFolderId((long) Integer.parseInt(folderId));

        try{
            fileMapper.insert(file);
        }catch(DuplicateKeyException e){
            System.out.println(file.getFileName());
            System.out.println(file.getExt());
            e.printStackTrace();
            return "檔案已存在rrr"; // 之後要封裝 - errcode: 資料庫已有資料
        }

        clientFileService.upload(clientFile, userIdLong, file.getId(),false);
        //一樣要try-catch - errcode: server已有資料但沒有被資料庫catch

        return "上傳成功";
    }

}
