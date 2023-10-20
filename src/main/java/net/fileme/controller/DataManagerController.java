package net.fileme.controller;

import net.fileme.domain.MyUserDetails;
import net.fileme.domain.dto.FileFolderDto;
import net.fileme.domain.Result;
import net.fileme.domain.mapper.DriveDtoMapper;
import net.fileme.domain.dto.DriveDto;
import net.fileme.domain.pojo.Folder;
import net.fileme.enums.ExceptionEnum;
import net.fileme.exception.BadRequestException;
import net.fileme.exception.UnauthorizedException;
import net.fileme.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private DriveDtoService driveDtoService;
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
    public Result myDrive(@AuthenticationPrincipal MyUserDetails myUserDetails){
        Long userId = myUserDetails.getUser().getId();
        List<DriveDto> driveDto = driveDtoMapper.getPrivateData(userId, rootId);
        return Result.success(driveDto);
    }
    @GetMapping("/pub/drive/data") // TODO: 如何避免攻擊?
    public Result publicData(@NotNull @RequestParam Long folderId){
        // no public access for user's root folder
        if(folderId == 0) throw new BadRequestException(ExceptionEnum.PARAM_ERROR);
        return driveDtoService.publicData(folderId);
    }
    @GetMapping("/drive/data")
    public Result privateData(@AuthenticationPrincipal MyUserDetails myUserDetails
            , @NotNull @RequestParam Long folderId){
        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        Long userId = myUserDetails.getUser().getId();
        if(Objects.isNull(userId)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);

        return driveDtoService.privateData(userId, folderId);
    }
    @GetMapping("/pub/drive/preview")
    public ResponseEntity publicPreview(@NotNull @RequestParam Long fileId){
        return driveDtoService.previewPublic(fileId);
    }
    @GetMapping("/drive/preview")
    public ResponseEntity preview(@AuthenticationPrincipal MyUserDetails myUserDetails
            ,@NotNull @RequestParam Long fileId){
        if(Objects.isNull(myUserDetails)){
            throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        }
        Long userId = myUserDetails.getUser().getId();
        if(Objects.isNull(userId)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);

        return driveDtoService.previewPersonal(userId, fileId);
    }

    // ----------------------------------Update: rename---------------------------------- //
    @PostMapping("/drive/rename")
    public ResponseEntity rename(@Valid @RequestBody DriveDto dto){
        driveDtoService.rename(dto);
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
        driveDtoService.relocate(destId, dto);
        return ResponseEntity.ok().body(Result.success());
    }

    // ----------------------------------Delete: clean & recover---------------------------------- //

    @PostMapping("/drive/trash")
    public ResponseEntity gotoTrash(@NotNull @RequestBody FileFolderDto dto){
        driveDtoService.gotoTrash(dto);
        return ResponseEntity.ok().body(Result.success());
    }

    @PostMapping("/drive/recover")
    public ResponseEntity recover(@NotNull @RequestBody FileFolderDto dto){
        driveDtoService.recover(dto);
        return ResponseEntity.ok().body(Result.success());
    }

    @PostMapping("/drive/clean") // 清空垃圾桶
    public ResponseEntity clean(@NotNull @RequestParam Long userId){
        driveDtoService.clean(userId);
        return ResponseEntity.ok().body(Result.success());
    }

    @PostMapping("/drive/softDelete")
    public ResponseEntity softDelete(@NotNull @RequestParam Long userId, @NotNull @RequestBody FileFolderDto dto){
        driveDtoService.softDelete(userId, dto);
        return ResponseEntity.ok().body(Result.success());
    }

}
