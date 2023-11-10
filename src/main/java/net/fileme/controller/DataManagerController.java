package net.fileme.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.fileme.domain.MyUserDetails;
import net.fileme.domain.Result;
import net.fileme.domain.dto.DriveDto;
import net.fileme.domain.dto.DataOwnerDto;
import net.fileme.enums.ExceptionEnum;
import net.fileme.exception.BadRequestException;
import net.fileme.exception.UnauthorizedException;
import net.fileme.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

import javax.validation.constraints.NotNull;
import java.util.*;

@Tag(name = "File/Folder API")
@RestController
@PropertySource("classpath:credentials.properties")

public class DataManagerController {

    @Value("${file.root.folderId}")
    private Long rootId;
    @Value("${file.trash.folderId}")
    private Long trashId;
    @Value("${regex.folder.name}")
    private String folderRegex;

    @Autowired
    private FolderService folderService;
    @Autowired
    private FileService fileService;
    @Autowired
    private DriveDtoService driveDtoService;
    @Autowired
    private ValidateService validateService;

    // ----------------------------------Create---------------------------------- //
    @Operation(summary = "[Create] 新增檔案")
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
    @Operation(summary = "[Create] 新增目錄")
    @PostMapping("/drive/folder")
    public Result createFolder(@Validated(DriveDto.Create.class) @RequestBody DriveDto dto
            , @AuthenticationPrincipal MyUserDetails myUserDetails){

        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        Long userId = myUserDetails.getUser().getId();

        validateService.checkFolder(userId, dto.getParentId());
        folderService.createFolder(userId, dto.getParentId(), dto.getDataName());
        return Result.success();
    }
    // ----------------------------------Read---------------------------------- //

    @PostMapping("/drive/my-drive")
    @Operation(summary = "[Read] 瀏覽根目錄", description = "[version 1.0]")
    @PreAuthorize("hasAuthority('admin') OR authentication.principal.user.getId().equals(#userId)")
    public Result<List<DriveDto>> getMyDrive(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "用戶ID",
            content = @Content(
                    schema = @Schema(type = "string"),
                    examples = {@ExampleObject("1710573934860890113")}
            )
    ) @org.springframework.web.bind.annotation.RequestBody @NotNull Long userId) {
        return driveDtoService.getSub(userId, rootId);
    }

    @GetMapping("/pub/drive/{folderId}") // TODO: 如何避免攻擊?
    @Operation(summary = "[Read] 瀏覽單個公開目錄", description = "[version 1.0] <br> 僅顯示該目錄內的公開資料")
    public Result<List<DriveDto>> getPublicSub(@Parameter(
            description = "目錄ID，範例：1698350322036805633",
            schema = @Schema(type = "string")) @PathVariable @NotNull Long folderId){
        validateService.checkPublicFolder(folderId);
        return driveDtoService.getPublicSub(folderId);
    }

    @PostMapping("/drive/data")
    @Operation(summary = "[Read] 瀏覽單個目錄所有資料", description = "[version 1.0]")
    @PreAuthorize("hasAuthority('admin') OR authentication.principal.user.getId().equals(#userId)")
    public Result<List<DriveDto>> getSub(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "傳入用戶及目錄ID")
                             @org.springframework.web.bind.annotation.RequestBody @NotNull DataOwnerDto dto){
        return driveDtoService.getSub(dto.getUserId(), dto.getDataId());
    }

    @Operation(summary = "[Read] 預覽單個公開檔案")
    @GetMapping("/pub/drive/preview/{fileId}")
    public ResponseEntity previewPublic(@NotNull @PathVariable Long fileId){
        return driveDtoService.previewPublic(fileId);
    }
    @Operation(summary = "[Read] 預覽單個檔案")
    @PostMapping("/drive/preview")
    @PreAuthorize("hasAuthority('admin') OR authentication.principal.user.getId().equals(#userId)")
    public ResponseEntity previewPersonal(@NotNull @RequestParam Long userId, @NotNull @RequestParam Long fileId){
        return driveDtoService.previewPersonal(userId, fileId);
    }
    @Operation(summary = "[Read] 瀏覽垃圾桶資料")
    @PostMapping("/drive/my-trash")
    @PreAuthorize("hasAuthority('admin') OR authentication.principal.user.getId().equals(#userId)")
    public Result getMyTrash(@NotNull @RequestParam Long userId) {
        return driveDtoService.getSub(userId, trashId);
    }

    // ----------------------------------Download---------------------------------- //
    @GetMapping("/pub/drive/download/{fileId}")
    @Operation(summary = "[Read] 下載單個公開檔案")
    public ResponseEntity<ByteArrayResource> downloadPublic(@NotNull @PathVariable @Parameter(description = "欲下載檔案ID，範例：1698350322036805633"
            , schema = @Schema(type = "string"))Long fileId
            , HttpServletResponse response){
        return driveDtoService.downloadPublic(fileId, response);
    }
    @PostMapping("/drive/download")
    @Operation(summary = "[Read] 下載單個檔案")
    @PreAuthorize("hasAuthority('admin') OR authentication.principal.user.getId().equals(#userId)")
    public ResponseEntity<ByteArrayResource> downloadPersonal(@NotNull @RequestParam @Parameter(description = "檔案擁有者ID，範例：1698350322036805633"
            , schema = @Schema(type = "string")) Long userId
            , @NotNull @RequestParam @Parameter(description = "欲下載檔案ID，範例：1698350322036805633"
            , schema = @Schema(type = "string")) Long fileId
            , HttpServletResponse response){
        return driveDtoService.downloadPersonal(userId, fileId, response);
    }
    // ----------------------------------Update: access level & shared link---------------------------------- //
    @PostMapping("/drive/access-control")
    @Operation(summary = "[Update] 變更單個資料權限", description = "會根據目前狀態自動進行權限toggle，例如private -> public"
            )
    public Result accessControl(@NotNull @RequestParam @Parameter(
            description = "欲變更權限的檔案或目錄ID，範例：1698350322036805633") Long dataId
            , @AuthenticationPrincipal MyUserDetails myUserDetails){
        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        Long userId = myUserDetails.getUser().getId();

        DriveDto data = validateService.checkData(userId, dataId);
        driveDtoService.accessControl(data, userId);
        return Result.success();
    }

    // ----------------------------------Update: rename---------------------------------- //
    @PostMapping("/drive/rename")
    @Operation(summary = "[Update] 變更單個資料名稱")
    public Result rename(@Validated(DriveDto.Update.class) @org.springframework.web.bind.annotation.RequestBody
                             @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "傳入欲改名的檔案或目錄ID及變更後的名稱，並註明資料種類",
                                     content = @Content(examples = {
                                             @ExampleObject(value = "{\"id\": \"1710573934860890113\", \"dataName\": \"範例名稱\", \"dataType\": \"0\"}")
                                     })) DriveDto dto
            , @AuthenticationPrincipal MyUserDetails myUserDetails){

        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        Long userId = myUserDetails.getUser().getId();

        driveDtoService.rename(dto, userId);
        return Result.success();
    }
    // ----------------------------------Update: relocate---------------------------------- //
    @GetMapping("/drive/folder/super")
    @Operation(summary = "[Read] 取得所有父目錄 (self included)", description = "列出包含自己以上所有父目錄ID及名稱List，不包含根目錄(ID=0)，由最外層開始向下排列")
    public Result getRelocateSuper(@NotNull @RequestParam @Parameter(description = "欲移動資料的「父目錄」ID，範例：1698350322036805633"
            , schema = @Schema(type = "string")) Long folderId
            , @AuthenticationPrincipal MyUserDetails myUserDetails){

        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        Long userId = myUserDetails.getUser().getId();

        List<DriveDto> superFolders = driveDtoService.getSuperFolderTree(userId, folderId);
        return Result.success(superFolders);
    }

    @Operation(summary = "[Read] 取得所有子目錄 (self excluded)", description = "列出有相同父目錄(=同一層)的所有目錄")
    @GetMapping("/drive/folder/sub")
    public Result getRelocateSub(@NotNull @RequestParam @Parameter(description = "欲移動資料的「父目錄」ID，範例：1698350322036805633"
            , schema = @Schema(type = "string")) Long folderId
            , @AuthenticationPrincipal MyUserDetails myUserDetails){

        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        if(rootId.equals(folderId) || trashId.equals(folderId)) throw new BadRequestException(ExceptionEnum.PARAM_ERROR);

        Long userId = myUserDetails.getUser().getId();
        List<DriveDto> subFolders = driveDtoService.getSubFolders(userId, folderId);
        return Result.success(subFolders);
    }

    @Operation(summary = "[Update] 批次移動資料")
    @PostMapping("/drive/relocate")
    public Result relocate(@NotNull @RequestParam @Parameter(description = "目的地目錄ID，範例：1698350322036805633"
            , schema = @Schema(type = "string"))Long destId
            , @NotNull @org.springframework.web.bind.annotation.RequestBody
                               @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "以List傳入欲移至垃圾桶的檔案或目錄ID，並註明資料種類",
                                       content = @Content(examples = {
                                               @ExampleObject(value = "[{\"id\": \"1710573934860890113\", \"dataType\": \"0\"}, {\"id\": \"1716111892070346754\", \"dataType\": \"1\"}]")
                                       })) List<DriveDto> listDto
            , @AuthenticationPrincipal MyUserDetails myUserDetails){
        Long userId = validateService.checkUserAndListDto(myUserDetails, listDto);
        validateService.checkFolder(userId, destId);
        driveDtoService.relocate(destId, listDto, userId);
        return Result.success();
    }

    // ----------------------------------Delete: clean & recover---------------------------------- //
    @Operation(summary = "[Update] 批次移至垃圾桶")
    @PostMapping("/drive/trash")
    public Result gotoTrash(@NotNull @org.springframework.web.bind.annotation.RequestBody
                                @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "以List傳入欲移至垃圾桶的檔案或目錄ID，並註明資料種類",
                                        content = @Content(examples = {
                                                @ExampleObject(value = "[{\"id\": \"1710573934860890113\", \"dataType\": \"0\"}, {\"id\": \"1716111892070346754\", \"dataType\": \"1\"}]")
                                        }))List<DriveDto> listDto
            , @AuthenticationPrincipal MyUserDetails myUserDetails){
        
        Long userId = validateService.checkUserAndListDto(myUserDetails, listDto);
        driveDtoService.gotoTrash(userId, listDto);
        return Result.success();
    }

    @Operation(summary = "[Update] 批次從垃圾桶還原")
    @PostMapping("/drive/recover")
    public Result recover(@NotNull @org.springframework.web.bind.annotation.RequestBody
                              @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "以List傳入欲還原的檔案或目錄ID，並註明資料種類",
                                      content = @Content(examples = {
                                              @ExampleObject(value = "[{\"id\": \"1710573934860890113\", \"dataType\": \"0\"}, {\"id\": \"1716111892070346754\", \"dataType\": \"1\"}]")
                                      })) List<DriveDto> listDto
            , @AuthenticationPrincipal MyUserDetails myUserDetails){
        
        Long userId = validateService.checkUserAndListDto(myUserDetails, listDto);
        driveDtoService.recover(userId, listDto);
        return Result.success();
    }

    @Operation(summary = "[Delete] 清空垃圾桶")
    @PostMapping("/drive/clean")
    public Result clean(@AuthenticationPrincipal MyUserDetails myUserDetails){
        
        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        Long userId = myUserDetails.getUser().getId();
        driveDtoService.clean(userId);
        return Result.success();
    }

    @Operation(summary = "[Delete] 批次立即刪除")
    @PostMapping("/drive/softDelete")
    public Result softDelete(@NotNull @org.springframework.web.bind.annotation.RequestBody
                                 @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "以List傳入欲立即刪除的檔案或目錄ID，並註明資料種類",
                                         content = @Content(examples = {
                                                 @ExampleObject(value = "[{\"id\": \"1710573934860890113\", \"dataType\": \"0\"}, {\"id\": \"1716111892070346754\", \"dataType\": \"1\"}]")
                                         })) List<DriveDto> listDto
    , @AuthenticationPrincipal MyUserDetails myUserDetails){
        
        Long userId = validateService.checkUserAndListDto(myUserDetails, listDto);
        driveDtoService.softDelete(userId, listDto);
        return Result.success();
    }

}
