package net.fileme.controller;

import net.fileme.domain.Result;
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
    public Result<String> upload(@RequestPart("file") MultipartFile clientFile
            , @RequestPart("userId") String userId
            , @RequestPart("folderId") String folderId
            , @RequestPart("accessLevel") String accessLevel){

        if(clientFile.isEmpty() || StringUtils.isBlank(userId)
                || StringUtils.isBlank(folderId) || StringUtils.isBlank(accessLevel)){
            throw new BizException(ExceptionEnum.PARAM_EMPTY);
        }

        File file = clientFileService.createFile(clientFile, userId, folderId, accessLevel);
        fileMapper.insert(file);
        return clientFileService.upload(clientFile, file,false);
    }

}
