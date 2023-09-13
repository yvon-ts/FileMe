package net.fileme.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.fileme.domain.Result;
import net.fileme.domain.mapper.FileMapper;
import net.fileme.domain.mapper.FolderMapper;
import net.fileme.domain.pojo.File;
import net.fileme.domain.pojo.Folder;
import net.fileme.exception.BizException;
import net.fileme.exception.ExceptionEnum;
import net.fileme.service.*;
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
    private DataTreeService dataTreeService;
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private FolderMapper folderMapper;
    @Autowired
    private FolderService folderService;
    @Autowired
    private FileService fileService;

    @PostMapping("/testtest")
    public String testtest(@RequestBody List<Long> fileIds){
       fileService.hardDelete(fileIds);
       return null;
    }
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
