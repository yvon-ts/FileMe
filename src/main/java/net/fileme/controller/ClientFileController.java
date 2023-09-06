package net.fileme.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.fileme.domain.Result;
import net.fileme.domain.mapper.FileMapper;
import net.fileme.domain.mapper.FolderMapper;
import net.fileme.domain.pojo.File;
import net.fileme.domain.pojo.Folder;
import net.fileme.exception.BizException;
import net.fileme.exception.ExceptionEnum;
import net.fileme.service.CheckExistService;
import net.fileme.service.ClientFileService;
import net.fileme.service.FileService;
import net.fileme.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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
    @Autowired
    private FileService fileService;

    @PostMapping("/drive/batch/relocate")
    public Result testPost(@RequestBody Map<String, List<Long>> map){
        Long userId = map.get("userId").get(0); //這行之後應該可以移除
        List<Long> destId = map.get("destId");
        List<Long> folderIds = map.get("folders");
        List<Long> fileIds = map.get("files");

        if(destId.isEmpty() || destId.size() != 1){
            throw new BizException(ExceptionEnum.PARAM_ERROR);
        }
        if(folderIds.isEmpty() && fileIds.isEmpty()){
            throw new BizException(ExceptionEnum.PARAM_ERROR);
        }


        if(destId.equals(999)){
            // do delete
            folderIds.forEach(folderId -> {
                Result resultFolder = deleteFolder(userId, folderId); //需要改寫batch delete嗎
            });

        }else{
            Result resultFolder = relocateFolder(destId.get(0), folderIds);
            //do relocate files
            //判斷兩個都relocate成功才丟最終result
        }

        return Result.success();
    }

    // 檢查跟delete要拆開！！！！！
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

    /**
     * 右鍵刪除？
     * @param folderId
     * @return
     */
    @DeleteMapping("/drive/folder")
    public Result deleteFolder(@RequestParam Long userId, @RequestParam Long folderId){
        Map<String, Integer> contents = checkExistService.checkContents(userId, folderId);
        if(!contents.isEmpty()){
            return Result.error(ExceptionEnum.FOLDER_NOT_EMPTY, contents);
        }
        boolean success = folderService.removeById(folderId);
        if(!success){
            throw new BizException(ExceptionEnum.UPDATE_DB_FAIL);
        }
        return Result.success();
    }

//    @DeleteMapping("/drive/folder")
//    public Result deleteBatch(){
//        return Result.success();
//    }
    @PostMapping("drive/relocate/folder")
    public Result relocateFolder(@RequestParam("destId") Long parentId, @RequestBody List<Long> folderIds){

        // check to prevent nested structure
        if(folderIds.contains(parentId)){
            throw new BizException(ExceptionEnum.PARAM_ERROR);
        }

        LambdaQueryWrapper<Folder> lqw = new LambdaQueryWrapper<>();
        List<Folder> folders = new ArrayList<>();
        folderIds.forEach(folderId -> {
            Folder folder = new Folder();
            folder.setId(folderId);
            folder.setParentId(parentId);
            folders.add(folder);
        });

        boolean success = folderService.updateBatchById(folders);
        if(!success){
            throw new BizException(ExceptionEnum.UPDATE_DB_FAIL);
        }

        return Result.success();
    }



    @PostMapping("drive/display/folder")
    public List<Folder> displayFolder(@RequestParam Long userId, @RequestParam("currentFolderId") Long parentId){
        LambdaQueryWrapper<Folder> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Folder::getUserId, userId).eq(Folder::getParentId, parentId);
        List<Folder> folders = folderService.list(lqw);
        System.out.println(folders);
        return folders;
    }
    @PostMapping("drive/display/file")
    public List<File> displayFile(@RequestParam Long userId, @RequestParam Long folderId){
        LambdaQueryWrapper<File> lqw = new LambdaQueryWrapper<>();
        lqw.eq(File::getUserId, userId).eq(File::getFolderId, folderId);
        List<File> files = fileService.list(lqw);
        return files;
    }



}
