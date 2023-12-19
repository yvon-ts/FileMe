package net.fileme.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.fileme.domain.MyUserDetails;
import net.fileme.domain.Result;
import net.fileme.domain.dto.*;
import net.fileme.enums.ExceptionEnum;
import net.fileme.exception.BadRequestException;
import net.fileme.exception.UnauthorizedException;
import net.fileme.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.*;

@Tag(name = "File / Folder")
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
    @PostMapping("/drive/file")
    @Operation(summary = "[Create] 新增檔案", description = "[version 1.0] <br> location: 0本地, 1雲端 <br> 可接受的檔案類型：<ul><li>JPG/GIF/PNG</li><li>ZIP</li><li>JSON/XML/HTML/JS/CSS/SQL/LOG</li><li>PDF/TXT/CSV</li><li>DOC/XLS/DOCX/XLSX/PPT/PPTX</li></ul>",
            responses = {@ApiResponse(responseCode = "200", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Regex not matched or File type not Allowed", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Parent folder not found", content = @Content)})
    public Result createFile(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "檔案",
            content = @Content(mediaType = "multipart/form-data",
            schema = @Schema(type = "object"),
                    schemaProperties = {
                    @SchemaProperty(name = "file", schema = @Schema(type = "string", format = "binary")),
                    @SchemaProperty(name = "folderId", schema = @Schema(type = "string", example = "1698350322036805633")),
                    @SchemaProperty(name = "location", schema = @Schema(type = "string", example = "0"))}))
            @RequestPart("file") @NotNull MultipartFile part,
                             @RequestPart @NotNull Long folderId,
                             @RequestPart @NotNull Integer location,
                             @AuthenticationPrincipal MyUserDetails myUserDetails) {

        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        Long userId = myUserDetails.getUser().getId();

        validateService.checkFolder(userId, folderId);
        driveDtoService.createFile(part, userId, folderId, location);
        return Result.success();
    }
    @PostMapping("/drive/folder")
    @Operation(summary = "[Create] 新增目錄", description = "[version 1.0]",
            responses = {@ApiResponse(responseCode = "200", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Regex not matched", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Parent folder not found", content = @Content)})
    public Result createFolder(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "目錄名稱及父目錄ID", content = @Content(
            examples = {@ExampleObject(value = "{\"dataName\": \"範例名稱\", \"parentId\": \"0\"}")}))
                                   @org.springframework.web.bind.annotation.RequestBody @Validated(DriveDto.Create.class) DriveDto dto,
                               @AuthenticationPrincipal MyUserDetails myUserDetails){

        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        Long userId = myUserDetails.getUser().getId();

        validateService.checkFolder(userId, dto.getParentId());
        folderService.createFolder(userId, dto.getParentId(), dto.getDataName());
        return Result.success();
    }
    // ----------------------------------Read: Search---------------------------------- //
    @PostMapping("/drive/search")
    @Operation(summary = "[Read] 查詢", description = "[version 1.0] <br><ul><li>目前僅使用前兩個關鍵字查詢</li><li>第三個以後的關鍵字會暫時被系統排除</li><li>回傳結果依相關度排序</li></ul>",
        responses = {@ApiResponse(responseCode = "200", content = @Content),
                @ApiResponse(responseCode = "404", description = "No such file or folder", content = @Content)})
    public Result<List<DriveDto>> search(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "關鍵字",
            content = @Content(schema = @Schema(implementation = SearchDto.class)))
                                             @org.springframework.web.bind.annotation.RequestBody @NotNull SearchDto dto,
            @AuthenticationPrincipal MyUserDetails myUserDetails){
        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);

        Long userId = myUserDetails.getUser().getId();
        List<String> keywords = validateService.filterKeyword(dto.getKeywords());
        return driveDtoService.search(userId, keywords);
    }
    // ----------------------------------Read & Preview---------------------------------- //

    @GetMapping("/drive/my-drive")
    @Operation(summary = "[Read] 瀏覽根目錄", description = "[version 1.0]", responses = {
            @ApiResponse(responseCode = "200", content = @Content),
            @ApiResponse(responseCode = "404", description = "Empty folder", content = @Content(
                    schema = @Schema(implementation = Result.class),
                    examples = @ExampleObject("{\"code\": 13010, \"msg\": \"暫無資料\", \"data\": null}")))})
    public Result<List<DriveDto>> getMyDrive(@AuthenticationPrincipal MyUserDetails myUserDetails) {
        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        Long userId = myUserDetails.getUser().getId();

        return driveDtoService.getSub(userId, rootId);
    }
    @GetMapping("/pub/drive/{folderId}")
    @Operation(summary = "[Read] 瀏覽單個公開目錄", description = "[version 1.0] <br><ul><li>回傳該目錄名稱及內容</li><li>內容僅顯示該目錄內的公開資料</li></ul>", responses = {
            @ApiResponse(responseCode = "200", description = "id: requested folderId, name: requested folder name"),
            @ApiResponse(responseCode = "404", description = "Folder not public or valid", content = @Content(
                    schema = @Schema(implementation = Result.class),
                    examples = @ExampleObject("{\"code\": 13020, \"msg\": \"查無資料\", \"data\": null}")))})
    public Result<ListDriveDto> getPublicFolder(@Parameter(description = "目錄ID，範例：1698350322036805633", schema = @Schema(type = "string"))
                                               @PathVariable @NotNull Long folderId){
        validateService.checkPublicFolder(folderId);
        return driveDtoService.getPublicFolder(folderId);
    }

    @GetMapping("/drive/{folderId}")
    @Operation(summary = "[Read] 瀏覽單個目錄所有資料", description = "[version 1.0]", responses = {
            @ApiResponse(responseCode = "200", content = @Content),
            @ApiResponse(responseCode = "404", description = "Empty folder", content = @Content(
                    schema = @Schema(implementation = Result.class),
                    examples = @ExampleObject("{\"code\": 13010, \"msg\": \"暫無資料\", \"data\": null}")))})
    public Result<List<DriveDto>> getSub(@Parameter(description = "目錄ID，範例：1698350322036805633", schema = @Schema(type = "string"))
                                             @PathVariable @NotNull Long folderId,
                                         @AuthenticationPrincipal MyUserDetails myUserDetails){
        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        if(rootId.equals(folderId) || trashId.equals(folderId)) throw new BadRequestException(ExceptionEnum.PARAM_ERROR);
        Long userId = myUserDetails.getUser().getId();
        return driveDtoService.getSub(userId, folderId);
    }
    @GetMapping("/drive/my-trash")
    @Operation(summary = "[Read] 瀏覽垃圾桶資料", description = "[version 1.0]", responses = {
            @ApiResponse(responseCode = "200", content = @Content),
            @ApiResponse(responseCode = "404", description = "Empty folder", content = @Content(
                    schema = @Schema(implementation = Result.class),
                    examples = @ExampleObject("{\"code\": 13010, \"msg\": \"暫無資料\", \"data\": null}")))})
    public Result<List<DriveDto>> getMyTrash(@AuthenticationPrincipal MyUserDetails myUserDetails) {
        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        Long userId = myUserDetails.getUser().getId();

        return driveDtoService.getSub(userId, trashId);
    }

    @GetMapping("/pub/drive/preview/{fileId}")
    @Operation(summary = "[Read] 預覽單個公開檔案", description = "[version 1.0] <br> 僅能預覽圖檔 (JPG/GIF/PNG)",
            responses = {@ApiResponse(responseCode = "200", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Preview not Allowed", content = @Content)})
    public ResponseEntity previewPublic(@Parameter(description = "檔案ID，範例：1698350322036805633", schema = @Schema(type = "string"))
                                            @NotNull @PathVariable Long fileId){

        return driveDtoService.previewPublic(fileId);
    }
    @GetMapping("/drive/preview/{fileId}")
    @Operation(summary = "[Read] 預覽單個檔案", description = "[version 1.0] <br> 僅能預覽圖檔 (JPG/GIF/PNG)",
            responses = {@ApiResponse(responseCode = "200", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Preview not Allowed", content = @Content)})
    public ResponseEntity previewPersonal(@Parameter(description = "檔案ID，範例：1698350322036805633", schema = @Schema(type = "string"))
                                              @NotNull @PathVariable Long fileId,
                                          @AuthenticationPrincipal MyUserDetails myUserDetails){
        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        Long userId = myUserDetails.getUser().getId();

        return driveDtoService.previewPersonal(userId, fileId);
    }
    // ----------------------------------Download---------------------------------- //
    @GetMapping("/pub/drive/download/{fileId}")
    @Operation(summary = "[Read] 下載單個公開檔案", description = "[version 1.0]", responses = {
            @ApiResponse(responseCode = "200", content = @Content)})
    public ResponseEntity<ByteArrayResource> downloadPublic(
            @Parameter(description = "欲下載檔案ID，範例：1698350322036805633", schema = @Schema(type = "string"))
            @PathVariable @NotNull Long fileId){

        return driveDtoService.downloadPublic(fileId);
    }
    @GetMapping("/drive/download/{fileId}")
    @Operation(summary = "[Read] 下載單個檔案", description = "[version 1.0]", responses = {
            @ApiResponse(responseCode = "200", content = @Content)})
    public ResponseEntity<ByteArrayResource> downloadPersonal(
            @Parameter(description = "檔案ID，範例：1698350322036805633", schema = @Schema(type = "string"))
            @NotNull @PathVariable Long fileId,
            @AuthenticationPrincipal MyUserDetails myUserDetails){
        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        Long userId = myUserDetails.getUser().getId();

        return driveDtoService.downloadPersonal(userId, fileId);
    }
    // ----------------------------------Update: access level & shared link---------------------------------- //
    @PostMapping("/drive/access-control/{dataId}")
    @Operation(summary = "[Update] 變更單個資料權限", description = "[version 1.0] <br> 會根據目前狀態自動進行權限toggle，例如private -> public")
    public Result accessControl(@Parameter(description = "檔案或目錄ID，範例：1698350322036805633", schema = @Schema(type = "string"))
                                @PathVariable @NotNull Long dataId,
                                @AuthenticationPrincipal MyUserDetails myUserDetails){
        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        Long userId = myUserDetails.getUser().getId();

        DriveDto data = validateService.checkData(userId, dataId);
        driveDtoService.accessControl(data, userId);
        return Result.success();
    }

    // ----------------------------------Update: rename---------------------------------- //
    @PostMapping("/drive/rename")
    @Operation(summary = "[Update] 變更單個資料名稱", description = "[version 1.0]")
    public Result rename(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "傳入欲改名的檔案或目錄ID及變更後的名稱，並註明資料種類", content = @Content(
            examples = {@ExampleObject(value = "{\"id\": \"1710573934860890113\", \"dataName\": \"範例名稱\", \"dataType\": \"0\"}")}))
            @org.springframework.web.bind.annotation.RequestBody @Validated(DriveDto.Update.class) DriveDto dto
            , @AuthenticationPrincipal MyUserDetails myUserDetails){

        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        Long userId = myUserDetails.getUser().getId();

        driveDtoService.rename(dto, userId);
        return Result.success();
    }
    // ----------------------------------Update: relocate---------------------------------- //
    @GetMapping("/drive/folder/super")
    @Operation(summary = "[Read] 取得所有父目錄 (self included)",
            description = "[version 1.0] <br><ul><li>列出包含自己的所有父目錄</li><li>由最上層開始向下排列</li><li>不包含根目錄(ID=0)</li><li>配合relocate使用時，需傳入欲移動資料的「父目錄」ID</li></ul>")
    public Result<List<DriveDto>> getRelocateSuper(@Parameter(description = "目錄ID", content = @Content(
            schema = @Schema(type = "string", example = "1698350322036805633"))) @RequestParam @NotNull Long folderId,
                                   @AuthenticationPrincipal MyUserDetails myUserDetails){

        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        if(rootId.equals(folderId) || trashId.equals(folderId)) throw new BadRequestException(ExceptionEnum.PARAM_ERROR);
        Long userId = myUserDetails.getUser().getId();

        List<DriveDto> superFolders = driveDtoService.getSuperFolderTree(userId, folderId);
        return Result.success(superFolders);
    }

    @GetMapping("/drive/folder/sub")
    @Operation(summary = "[Read] 取得所有子目錄 (self excluded)", description = "[version 1.0] <br><ul><li>列出有相同父目錄(=同一層)的所有目錄</li><li>配合relocate使用時，需傳入欲移動資料的「父目錄」ID</li></ul>",
            responses = {
            @ApiResponse(responseCode = "200", content = @Content),
            @ApiResponse(responseCode = "400", description = "System folders not accepted", content = @Content)
//            ,@ApiResponse(responseCode = "404", description = "Empty folder", content = @Content(
//                    schema = @Schema(implementation = Result.class),
//                    examples = @ExampleObject("{\"code\": 13010, \"msg\": \"暫無資料\", \"data\": null}")))
    })
    public Result<List<DriveDto>> getRelocateSub(@Parameter(description = "目錄ID", content = @Content(
            schema = @Schema(type = "string", example = "1698350322036805633"))) @RequestParam @NotNull Long folderId,
                                                 @AuthenticationPrincipal MyUserDetails myUserDetails){

        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        if(trashId.equals(folderId)) throw new BadRequestException(ExceptionEnum.PARAM_ERROR);

        Long userId = myUserDetails.getUser().getId();
        List<DriveDto> subFolders = driveDtoService.getSubFolders(userId, folderId);
        return Result.success(subFolders);
    }
    @PostMapping("/drive/relocate")
    @Operation(summary = "[Update] 批次移動資料", description = "[version 1.0] <br><ul><li>目的地不得為系統目錄</li><li>會檢查資料是否來自同一層(父目錄)</li></ul>", responses = {
            @ApiResponse(responseCode = "200", content = @Content),
            @ApiResponse(responseCode = "400", description = "Destination ID error or Not from same parent", content = @Content)})
    public Result relocate(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "id：目的地(目錄)ID <br> list：以List傳入檔案或目錄ID，並註明資料種類", content = @Content(
            schema = @Schema(implementation = ListDriveDto.class),
            examples = {@ExampleObject(value = "{\"id\": \"1710573934860890113\",\"list\": [{\"id\": \"1698350322036805633\", \"dataType\": \"0\"}, {\"id\": \"1716111892070346754\", \"dataType\": \"1\"}]}")}))
                               @org.springframework.web.bind.annotation.RequestBody @NotNull ListDriveDto dto
            , @AuthenticationPrincipal MyUserDetails myUserDetails){

        Long userId = validateService.checkUserAndListDto(myUserDetails, dto.getList());
        validateService.checkFolder(userId, dto.getId());
        driveDtoService.relocate(dto.getId(), dto.getList(), userId);
        return Result.success();
    }
    // ----------------------------------Update: trash & recover---------------------------------- //
    @PostMapping("/drive/trash")
    @Operation(summary = "[Update] 批次移至垃圾桶", description = "[version 1.0] <br> 會檢查資料是否來自同一層(父目錄)", responses = {
            @ApiResponse(responseCode = "200", content = @Content),
            @ApiResponse(responseCode = "400", description = "Not from same parent", content = @Content)})
    public Result gotoTrash(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "以List傳入檔案或目錄ID，並註明資料種類", content = @Content(
            examples = {@ExampleObject(value = "[{\"id\": \"1698350322036805633\", \"dataType\": \"0\"}, {\"id\": \"1716111892070346754\", \"dataType\": \"1\"}]")}))
                                @org.springframework.web.bind.annotation.RequestBody @NotNull List<DriveDto> listDto,
                            @AuthenticationPrincipal MyUserDetails myUserDetails){

        Long userId = validateService.checkUserAndListDto(myUserDetails, listDto);
        driveDtoService.gotoTrash(userId, listDto);
        return Result.success();
    }
    @PostMapping("/drive/recover")
    @Operation(summary = "[Update] 批次從垃圾桶還原", description = "[version 1.0]")
    public Result recover(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "以List傳入檔案或目錄ID，並註明資料種類", content = @Content(
            examples = {@ExampleObject(value = "[{\"id\": \"1698350322036805633\", \"dataType\": \"0\"}, {\"id\": \"1716111892070346754\", \"dataType\": \"1\"}]")}))
            @org.springframework.web.bind.annotation.RequestBody @NotNull List<DriveDto> listDto,
                          @AuthenticationPrincipal MyUserDetails myUserDetails){

        Long userId = validateService.checkUserAndListDto(myUserDetails, listDto);
        driveDtoService.recover(userId, listDto);
        return Result.success();
    }
    // ----------------------------------Delete---------------------------------- //
    @PostMapping("/drive/clean")
    @Operation(summary = "[Delete] 清空垃圾桶", description = "[version 1.0]")
    public Result clean(@AuthenticationPrincipal MyUserDetails myUserDetails){
        
        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        Long userId = myUserDetails.getUser().getId();
        driveDtoService.clean(userId);
        return Result.success();
    }
    @PostMapping("/drive/conflict/trash")
    @Operation(summary = "[Delete] 處理垃圾桶衝突", description = "[version 1.0] <br> 遇到垃圾桶已有同名檔案時，以新檔案取代舊檔，並刪除舊檔",
            responses = {@ApiResponse(responseCode = "200", content = @Content),
            @ApiResponse(responseCode = "400", description = "File name error", content = @Content)})
    public Result conflictTrash(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "以List傳入檔案或目錄ID，並註明資料種類", content = @Content(
            examples = {@ExampleObject(value = "[{\"id\": \"1698350322036805633\",\"dataName\": \"範例名稱\", \"dataType\": \"0\"}, {\"id\": \"1716111892070346754\",\"dataName\": \"範例名稱\", \"dataType\": \"1\"}]")}))
                                    @org.springframework.web.bind.annotation.RequestBody @NotNull List<DriveDto> listDto,
                                @AuthenticationPrincipal MyUserDetails myUserDetails){
        if(Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        Long userId = myUserDetails.getUser().getId();
        driveDtoService.conflictTrash(userId, listDto);
        return Result.success();
    }
    @PostMapping("/drive/softDelete")
    @Operation(summary = "[Delete] 批次立即刪除", description = "[version 1.0]")
    public Result softDelete(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "以List傳入檔案或目錄ID，並註明資料種類", content = @Content(
            examples = {@ExampleObject(value = "[{\"id\": \"1698350322036805633\", \"dataType\": \"0\"}, {\"id\": \"1716111892070346754\", \"dataType\": \"1\"}]")}))
                                 @org.springframework.web.bind.annotation.RequestBody @NotNull List<DriveDto> listDto,
                             @AuthenticationPrincipal MyUserDetails myUserDetails){
        
        Long userId = validateService.checkUserAndListDto(myUserDetails, listDto);
        driveDtoService.softDelete(userId, listDto);
        return Result.success();
    }

}
