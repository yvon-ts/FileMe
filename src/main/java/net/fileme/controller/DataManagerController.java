package net.fileme.controller;

import net.fileme.domain.MyUserDetails;
import net.fileme.domain.Result;
import net.fileme.domain.dto.DriveDto;
import net.fileme.enums.ExceptionEnum;
import net.fileme.exception.BadRequestException;
import net.fileme.exception.UnauthorizedException;
import net.fileme.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.*;

@RestController
@PropertySource("classpath:credentials.properties")
public class DataManagerController {

    @Value("${file.root.folderId}")
    private Long rootId;
    @Value("${file.trash.folderId}")
    private Long trashId;

    @Autowired
    private DataTreeService dataTreeService;
    @Autowired
    private FolderService folderService;
    @Autowired
    private FileService fileService;
    @Autowired
    private DriveDtoService driveDtoService;
    @Autowired
    private ValidateService validateService;

    // ----------------------------------Create---------------------------------- //
    @PostMapping("/drive/file")
    public Result createFile(@NotNull @RequestPart("file") MultipartFile part
            , @NotNull @RequestParam Long folderId
            , @AuthenticationPrincipal MyUserDetails myUserDetails) {

        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        Long userId = myUserDetails.getUser().getId();

        validateService.checkFolder(userId, folderId);
        fileService.createFile(part, userId, folderId);
        return Result.success();
    }
    @PostMapping("/drive/folder")
    public Result createFolder(@AuthenticationPrincipal MyUserDetails myUserDetails
            , @NotNull Long parentId, @NotBlank String name){

        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        Long userId = myUserDetails.getUser().getId();

        validateService.checkFolder(userId, parentId);
        folderService.createFolder(userId, parentId, name);
        return Result.success();
    }

    // ----------------------------------Read---------------------------------- //
    @GetMapping("/drive/my-drive")
    @PreAuthorize("hasAuthority('admin') OR authentication.principal.user.getId().equals(#userId)")
    public Result getMyDrive(@NotNull @RequestParam Long userId) {
        return driveDtoService.getSub(userId, rootId);
    }
    @GetMapping("/pub/drive/data") // TODO: 如何避免攻擊?
    public Result getPublicSub(@NotNull @RequestParam Long folderId){
        // no public access for user's root folder
        if(folderId == 0) throw new BadRequestException(ExceptionEnum.PARAM_ERROR);
        return driveDtoService.publicData(folderId);
    }
    @GetMapping("/drive/data")
    @PreAuthorize("hasAuthority('admin') OR authentication.principal.user.getId().equals(#userId)")
    public Result getSub(@NotNull @RequestParam Long userId, @NotNull @RequestParam Long folderId){
        return driveDtoService.getSub(userId, folderId);
    }
    @GetMapping("/pub/drive/preview")
    public ResponseEntity publicPreview(@NotNull @RequestParam Long fileId){
        return driveDtoService.previewPublic(fileId);
    }
    @GetMapping("/drive/preview")
    @PreAuthorize("hasAuthority('admin') OR authentication.principal.user.getId().equals(#userId)")
    public ResponseEntity preview(@NotNull @RequestParam Long userId, @NotNull @RequestParam Long fileId){
        return driveDtoService.previewPersonal(userId, fileId);
    }

    // TODO: 少了垃圾桶！

    // ----------------------------------Update: rename---------------------------------- //
    @PostMapping("/drive/rename")
    public ResponseEntity rename(@Valid @RequestBody DriveDto dto
            , @AuthenticationPrincipal MyUserDetails myUserDetails){

        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        Long userId = myUserDetails.getUser().getId();

        driveDtoService.rename(dto, userId);
        return ResponseEntity.ok().body(Result.success());
    }
    // ----------------------------------Update: relocate---------------------------------- //

    @GetMapping("/drive/relocate/super")
    public Result getRelocateSuper(@NotNull @RequestParam Long folderId
            , @AuthenticationPrincipal MyUserDetails myUserDetails){

        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);

        Long userId = myUserDetails.getUser().getId();
        // TODO: 是否改dtoService就好 不用用到tree 且是否要排除自身folder?
        List<DriveDto> superFolderDtos = dataTreeService.getSuperFolderTree(userId, folderId);
        return Result.success(superFolderDtos);
    }

    @GetMapping("/drive/relocate/sub")
    public Result getRelocateSub(@NotNull @RequestParam Long folderId
            , @AuthenticationPrincipal MyUserDetails myUserDetails){

        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        if(rootId.equals(folderId) || trashId.equals(folderId)) throw new BadRequestException(ExceptionEnum.PARAM_ERROR);

        Long userId = myUserDetails.getUser().getId();
        List<DriveDto> subFolderDtos = dataTreeService.getSubFolders(userId, folderId);
        return Result.success(subFolderDtos);
    }

    @PostMapping("/drive/relocate")
    public Result relocate(@NotNull @RequestParam Long destId, @NotNull @RequestBody List<DriveDto> listDto
            , @AuthenticationPrincipal MyUserDetails myUserDetails){
        
        Long userId = validateService.checkUserAndListDto(myUserDetails, listDto);

        validateService.checkFolder(userId, destId);
        driveDtoService.relocate(destId, listDto, userId);
        return Result.success();
    }

    // ----------------------------------Delete: clean & recover---------------------------------- //

    @PostMapping("/drive/trash")
    public Result gotoTrash(@NotNull @RequestBody List<DriveDto> listDto
            , @AuthenticationPrincipal MyUserDetails myUserDetails){
        
        Long userId = validateService.checkUserAndListDto(myUserDetails, listDto);
        driveDtoService.gotoTrash(userId, listDto);
        return Result.success();
    }

    @PostMapping("/drive/recover")
    public Result recover(@NotNull @RequestBody List<DriveDto> listDto
            , @AuthenticationPrincipal MyUserDetails myUserDetails){
        
        Long userId = validateService.checkUserAndListDto(myUserDetails, listDto);
        driveDtoService.recover(userId, listDto);
        return Result.success();
    }

    @PostMapping("/drive/clean") // clean up trashcan
    public Result clean(@AuthenticationPrincipal MyUserDetails myUserDetails){
        
        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        Long userId = myUserDetails.getUser().getId();
        driveDtoService.clean(userId);
        return Result.success();
    }

    @PostMapping("/drive/softDelete")
    public Result softDelete(@NotNull @RequestBody List<DriveDto> listDto
    , @AuthenticationPrincipal MyUserDetails myUserDetails){
        
        Long userId = validateService.checkUserAndListDto(myUserDetails, listDto);
        driveDtoService.softDelete(userId, listDto);
        return Result.success();
    }

}
