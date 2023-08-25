package net.fileme.controller;

import net.fileme.domain.mapper.FileMapper;
import net.fileme.domain.mapper.FolderMapper;
import net.fileme.domain.pojo.File;
import net.fileme.exception.BizException;
import net.fileme.exception.ExceptionEnum;
import net.fileme.service.impl.ClientFileServiceImpl;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ClientFileController {
    @Autowired
    private ClientFileServiceImpl clientFileService;
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private FolderMapper folderMapper;

//    @GetMapping("/mock")
//    public String mock(@RequestParam("tmpName") String tmpName, @RequestParam("size") int tmpSize){
//        return clientFileService.mockCreate(tmpName, tmpSize);
//    }

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

        if(clientFile.isEmpty() || StringUtils.isBlank(userId) || StringUtils.isBlank(folderId)){
            throw new BizException(ExceptionEnum.EMPTY_PARAM);
        }
        Long userIdLong = (long) Integer.parseInt(userId); //卡控轉型失敗?
        File file = clientFileService.createFile(clientFile);
        //這邊要卡控
        file.setUserId(userIdLong);
        file.setFolderId((long) Integer.parseInt(folderId));


            fileMapper.insert(file);
//        }catch(DuplicateKeyException e){
//            System.out.println(file.getFileName());
//            System.out.println(file.getExt());
//            e.printStackTrace();
//            return "檔案已存在rrr"; // 之後要封裝 - errcode: 資料庫已有資料


        clientFileService.upload(clientFile, userIdLong, file.getId(),false);
        //一樣要try-catch - errcode: server已有資料但沒有被資料庫catch

        return "上傳成功";
    }

}
