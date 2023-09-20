package net.fileme.controller;

import net.fileme.domain.Result;
import net.fileme.domain.mapper.DriveDtoMapper;
import net.fileme.domain.DriveDto;
import net.fileme.domain.pojo.File;
import net.fileme.domain.pojo.Folder;
import net.fileme.exception.BadRequestException;
import net.fileme.exception.BizException;
import net.fileme.utils.enums.ExceptionEnum;
import net.fileme.service.*;
import net.fileme.utils.enums.MimeEnum;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.*;

@RestController
public class DataManagerController {

    private Logger logger = LoggerFactory.getLogger(DataManagerController.class);

    @Value("${file.root.folderId}")
    private Long rootId;

    @Autowired
    private DataTreeService dataTreeService;
    @Autowired
    private CheckExistService checkExistService;
    @Autowired
    private FolderService folderService;
    @Autowired
    private FileService fileService;
    @Autowired
    private DriveDtoMapper driveDtoMapper;


    // ----------------------------------Create---------------------------------- //
    @PostMapping("/drive/upload")
    public Result upload(@RequestPart("file") MultipartFile part
            , @RequestParam Long userId
            , @RequestParam Long folderId) {

        boolean isValidFolder = checkExistService.checkValidFolder(userId, folderId);
        if(!isValidFolder){
            throw new BizException(ExceptionEnum.FOLDER_ERROR);
        }
        File file = fileService.handlePartFile(part);
        file.setUserId(userId);
        file.setFolderId(folderId);
        fileService.save(file);
        fileService.upload(part, file);

        return Result.success();
    }
    @PostMapping("/drive/folder")
    public Result createFolder(@RequestBody Map<String, String> data){
        Long userId = Long.valueOf(data.get("userId"));
        String name = data.get("name");
        Long parentId = Long.valueOf(data.get("parentId"));

        boolean isValid = checkExistService.checkValidFolder(userId, parentId);

        if(isValid){
            Folder folder = new Folder();
            folder.setUserId(userId);
            folder.setFolderName(name);
            folder.setParentId(parentId);
            folderService.save(folder);
        }
        return Result.success();
    }

    // ----------------------------------Read---------------------------------- //
    @GetMapping("/drive/my-drive")
    public Result myDrive(@RequestParam Long userId){
        List<DriveDto> driveDto = driveDtoMapper.getDriveDto(userId, rootId);
        return Result.success(driveDto);
    }

    @GetMapping("/drive/data")
    public Result DriveDto(@RequestParam Long userId, @RequestParam Long folderId){
        List<DriveDto> driveDto = driveDtoMapper.getDriveDto(userId, folderId);
        return Result.success(driveDto);
    }

    @GetMapping("/drive/preview")
    public ResponseEntity<?> preview(@RequestParam Long userId, @RequestParam Long fileId){

        String path = dataTreeService.findFilePath(userId, fileId);
        String ext = path.substring(path.lastIndexOf(".") + 1);

        // check if allowed to preview
        if(MimeEnum.valueOf(ext.toUpperCase()).allowPreview){
            java.io.File ioFile = new java.io.File(path);
            if(!ioFile.exists()){
                throw new BizException(ExceptionEnum.FILE_ERROR);
            }
            try{
                Tika tika = new Tika(); // mimeType library
                String mimeType = tika.detect(ioFile);

                byte[] bytes = FileCopyUtils.copyToByteArray(ioFile);

                return ResponseEntity
                        .ok()
                        .contentType(MediaType.valueOf(mimeType))
                        .contentLength((int)ioFile.length())
                        .body(bytes);

            }catch (IOException e){
                throw new BizException(ExceptionEnum.FILE_ERROR);
            }
        }
        // 這邊應該修成void, 丟error就好?
       return ResponseEntity
               .status(HttpStatus.BAD_REQUEST)
               .body(Result.error(ExceptionEnum.PREVIEW_NOT_ALLOWED));
    }

    // ----------------------------------Update: rename---------------------------------- //
    @PostMapping("/drive/rename")
    public ResponseEntity rename(@Valid @RequestBody DriveDto dto){
        int dataType = dto.getDataType();
        if(dataType == 0){
            folderService.rename(dto.getId(), dto.getDataName());
        }else if(dataType == 1){
            fileService.rename(dto.getId(), dto.getDataName());
        }else{
            throw new BadRequestException(ExceptionEnum.PARAM_ERROR);
        }
        return ResponseEntity.ok().body(Result.success());
    }
    // ----------------------------------Update: relocate---------------------------------- //

    @GetMapping("/drive/relocate/super")
    public Result getRelocateSuper(@RequestParam Long folderId){
        List<Folder> superFolders = dataTreeService.findSuperFolders(folderId);
        return Result.success(superFolders);
    }

    @GetMapping("/drive/relocate/sub")
    public Result getRelocateSub(@RequestParam Long userId, @RequestParam Long folderId){
        List<Folder> subFolders = dataTreeService.findSubFolders(userId, folderId);
        return Result.success(subFolders);
    }

    @PostMapping("/drive/relocate")
    public Result relocate(@RequestParam Long destId, @RequestBody Map<String, List<Long>> map){
        List<Long> folderIds = map.get("folders");
        List<Long> fileIds = map.get("files");

        if(folderIds == null && fileIds == null){
            throw new BizException(ExceptionEnum.PARAM_ERROR);
        }
        if(!folderIds.isEmpty()){
            folderService.relocate(destId, folderIds);
        }
        if(!fileIds.isEmpty()){
            fileService.relocate(destId, fileIds);
        }

        return Result.success();
    }

    // ----------------------------------Delete: clean & recover---------------------------------- //

    @PostMapping("/drive/trash")
    public Result gotoTrash(@RequestBody Map<String, List<Long>> map){
        List<Long> folderIds = map.get("folders");
        List<Long> fileIds = map.get("files");

        if(folderIds.isEmpty() && fileIds.isEmpty()){
            throw new BizException(ExceptionEnum.PARAM_ERROR);
        }

        folderService.gotoTrash(folderIds);
        fileService.gotoTrash(fileIds);

        return Result.success();
    }

    @PostMapping("/drive/recover")
    public Result recover(@RequestBody Map<String, List<Long>> map){
        List<Long> folderIds = map.get("folders");
        List<Long> fileIds = map.get("files");

        if(folderIds.isEmpty() && fileIds.isEmpty()){
            throw new BizException(ExceptionEnum.PARAM_ERROR);
        }

        folderService.recover(folderIds);
        fileService.recover(fileIds);

        return Result.success();
    }

    @PostMapping("/drive/clean") // 清空垃圾桶
    public Result clean(Long userId){
        List<Long> folders = folderService.getTrashIds(userId);
        List<Long> files = fileService.getTrashIds(userId);
        Map<String, List<Long>> map = new HashMap<>();
        map.put("folders", folders);
        map.put("files", files);
        softDelete(userId, map);

        return Result.success();
    }

    /**
     * 不管從垃圾桶或主表點選刪除都一樣，trashDel不會報錯
     * 甚至系統排程掃描包好id也可直接呼叫
     * @param userId
     * @param map
     */
    @PostMapping("/drive/softDelete")
    public Result softDelete(@RequestParam Long userId, @RequestBody Map<String, List<Long>> map){
        List<Long> foldersToDelete = map.get("folders");
        List<Long> filesToDelete = map.get("files");

        if(foldersToDelete.isEmpty() && filesToDelete.isEmpty()){
            throw new BizException(ExceptionEnum.PARAM_ERROR);
        }

        List<Long> tmpFolders = new ArrayList<>();
        List<Long> tmpFiles = new ArrayList<>();

        // handle sub data
        if(!foldersToDelete.isEmpty()){

            foldersToDelete.forEach(folder -> {
                Map<String, List<Long>> tmp = dataTreeService.findTreeIds(userId, folder);
                tmpFolders.addAll(tmp.get("subFolders"));
                tmpFiles.addAll(tmp.get("subFiles"));
            });

            foldersToDelete.addAll(tmpFolders);
            filesToDelete.addAll(tmpFiles);
            folderService.softDelete(foldersToDelete);
        }

        if(!filesToDelete.isEmpty()){
            fileService.softDelete(filesToDelete);
        }
        return Result.success();
    }

}
