package net.fileme.controller;

import net.fileme.domain.dto.FileFolderDto;
import net.fileme.domain.Result;
import net.fileme.domain.mapper.DriveDtoMapper;
import net.fileme.domain.dto.DriveDto;
import net.fileme.domain.pojo.Folder;
import net.fileme.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.*;

@RestController
@PropertySource("classpath:credentials.properties")
public class DataManagerController {

    @Value("${file.root.folderId}")
    private Long rootId;

    @Autowired
    private DataTreeService dataTreeService;
    @Autowired
    private FolderService folderService;
    @Autowired
    private FileService fileService;
    @Autowired
    private DriveDtoService dtoService;
    @Autowired
    private DriveDtoMapper driveDtoMapper;

    // ----------------------------------Create---------------------------------- //
    @PostMapping("/drive/file")
    public Result createFile(@NotNull @RequestPart("file") MultipartFile part
            , @NotNull @RequestParam Long userId
            , @NotNull @RequestParam Long folderId) {
        fileService.createFile(part, userId, folderId);
        return Result.success();
    }
    @PostMapping("/drive/folder")
    public ResponseEntity createFolder(@Valid @RequestBody Folder folder){
        folderService.createFolder(folder);
        return ResponseEntity.ok().body(Result.success());
    }

    // ----------------------------------Read---------------------------------- //
    @GetMapping("/drive/my-drive")
    public Result myDrive(@NotNull @RequestParam Long userId){
        List<DriveDto> driveDto = driveDtoMapper.getDriveDto(userId, rootId);
        return Result.success(driveDto);
    }

    @GetMapping("/drive/data")
    public Result DriveDto(@NotNull @RequestParam Long userId, @NotNull @RequestParam Long folderId){
        List<DriveDto> driveDto = driveDtoMapper.getDriveDto(userId, folderId);
        return Result.success(driveDto);
    }

    @GetMapping("/drive/preview")
    public ResponseEntity preview(@NotNull @RequestParam Long userId, @NotNull @RequestParam Long fileId){
        return dtoService.preview(userId, fileId);
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
    public ResponseEntity gotoTrash(@NotNull @RequestBody FileFolderDto dto){
        dtoService.gotoTrash(dto);
        return ResponseEntity.ok().body(Result.success());
    }

    @PostMapping("/drive/recover")
    public ResponseEntity recover(@NotNull @RequestBody FileFolderDto dto){
        dtoService.recover(dto);
        return ResponseEntity.ok().body(Result.success());
    }

    @PostMapping("/drive/clean") // 清空垃圾桶
    public ResponseEntity clean(@NotNull @RequestParam Long userId){
        dtoService.clean(userId);
        return ResponseEntity.ok().body(Result.success());
    }

    @PostMapping("/drive/softDelete")
    public ResponseEntity softDelete(@NotNull @RequestParam Long userId, @NotNull @RequestBody FileFolderDto dto){
        dtoService.softDelete(userId, dto);
        return ResponseEntity.ok().body(Result.success());
    }

}
