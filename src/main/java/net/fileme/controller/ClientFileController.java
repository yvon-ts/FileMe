package net.fileme.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import net.fileme.domain.Result;
import net.fileme.domain.mapper.FileMapper;
import net.fileme.domain.mapper.FolderMapper;
import net.fileme.domain.pojo.File;
import net.fileme.domain.pojo.Folder;
import net.fileme.exception.BizException;
import net.fileme.exception.ExceptionEnum;
import net.fileme.service.CheckExistService;
import net.fileme.service.ClientFileService;
import net.fileme.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;


@Validated
@RestController
public class ClientFileController {
    @Autowired
    private ClientFileService clientFileService;
    @Autowired
    private CheckExistService checkExistService;
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private FolderMapper folderMapper;
    @Autowired
    private FolderService folderService;

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
    public Result upload(@RequestPart("file") MultipartFile clientFile
            , @RequestParam Long userId
            , @RequestParam Long folderId
            , @RequestParam Integer accessLevel){

        File file = clientFileService.createFile(clientFile, userId, folderId, accessLevel);
        fileMapper.insert(file);
        return clientFileService.upload(clientFile, file,false);
    }

    @PutMapping("/drive/folder")
    public Result putFolder(@RequestBody @Valid Folder folder){
        boolean success = clientFileService.saveOrUpdateFolder(folder);
        if(!success){
            throw new BizException(ExceptionEnum.UPDATE_DB_FAIL);
        }
        return Result.success();
    }
    @PostMapping("/drive/folder")
    public Result update(@PathVariable Long folderId, @RequestParam @Valid String folderName){
        //name可以塞空值
        LambdaUpdateWrapper<Folder> luw = new LambdaUpdateWrapper<>();
        luw.eq(Folder::getId, folderId).set(Folder::getFolderName, folderName);
        int update = folderMapper.update(null, luw);
        System.out.println("更新筆數： " + update);
        return Result.success();
    }

    /**
     * batch relocate folders
     * @param list：only folderId, parentId included
     * @return
     */
    @PostMapping("drive/folder/relocate")
    public Result relocateFolder(@RequestBody List<Folder> list){
       folderService.updateBatchById(list);
       return Result.success();
    }

}
