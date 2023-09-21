package net.fileme.controller;

import net.fileme.domain.FileFolderDto;
import net.fileme.domain.Result;
import net.fileme.domain.mapper.DriveDtoMapper;
import net.fileme.domain.DriveDto;
import net.fileme.domain.pojo.File;
import net.fileme.domain.pojo.Folder;
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
import javax.validation.constraints.NotNull;
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
    private DtoService dtoService;
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
        dtoService.rename(dto);
        return ResponseEntity.ok().body(Result.success());
    }
    // ----------------------------------Update: relocate---------------------------------- //

    @GetMapping("/drive/relocate/super")
    public ResponseEntity getRelocateSuper(@NotNull @RequestParam Long folderId){
        List<Folder> superFolders = dataTreeService.findSuperFolders(folderId);
        return ResponseEntity.ok().body(Result.success(superFolders));
    }

    @GetMapping("/drive/relocate/sub")
    public ResponseEntity getRelocateSub(@NotNull @RequestParam Long userId, @NotNull @RequestParam Long folderId){
        List<Folder> subFolders = dataTreeService.findSubFolders(userId, folderId);
        return ResponseEntity.ok().body(Result.success(subFolders));
    }

    @PostMapping("/drive/relocate")
    public ResponseEntity relocate(@NotNull @RequestParam Long destId, @NotNull @RequestBody FileFolderDto dto){
        dtoService.relocate(destId, dto);
        return ResponseEntity.ok().body(Result.success());
    }

    // ----------------------------------Delete: clean & recover---------------------------------- //

    @PostMapping("/drive/trash")
    public ResponseEntity gotoTrash(@RequestBody FileFolderDto dto){
        dtoService.gotoTrash(dto);
        return ResponseEntity.ok().body(Result.success());
    }

    @PostMapping("/drive/recover")
    public ResponseEntity recover(@RequestBody FileFolderDto dto){
        dtoService.recover(dto);
        return ResponseEntity.ok().body(Result.success());
    }

    @PostMapping("/drive/clean") // 清空垃圾桶
    public ResponseEntity clean(Long userId){
        List<Long> folderIds = folderService.getTrashIds(userId);
        List<Long> fileIds = fileService.getTrashIds(userId);
        FileFolderDto dto = new FileFolderDto();
        dto.setFolderIds(folderIds);
        dto.setFileIds(fileIds);

        softDelete(userId, dto);

        return ResponseEntity.ok().body(Result.success());
    }

    @PostMapping("/drive/softDelete")
    public Result softDelete(@RequestParam Long userId, @RequestBody FileFolderDto dto){
        dtoService.softDelete(userId, dto);
        return Result.success();
    }

}
