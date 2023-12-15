package net.fileme.service.impl;

import net.fileme.domain.dto.DriveDto;
import net.fileme.domain.Result;
import net.fileme.domain.dto.ListDriveDto;
import net.fileme.domain.mapper.DriveDtoMapper;
import net.fileme.domain.pojo.File;
import net.fileme.exception.BadRequestException;
import net.fileme.exception.InternalErrorException;
import net.fileme.exception.NotFoundException;
import net.fileme.enums.ExceptionEnum;
import net.fileme.enums.MimeEnum;
import net.fileme.handler.GlobalExceptionHandler;
import net.fileme.service.DriveDtoService;
import net.fileme.service.FileService;
import net.fileme.service.FolderService;
import net.fileme.service.RemoteDataService;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DriveDtoServiceImpl implements DriveDtoService {
    @Autowired
    private FolderService folderService;
    @Autowired
    private FileService fileService;
    @Autowired
    private DriveDtoMapper driveDtoMapper;
    @Autowired
    private RemoteDataService remoteDataService;

    @Value("${file.root.folderId}")
    private Long rootId;
    @Value("${file.trash.folderId}")
    private Long trashId;

    private Logger log = LoggerFactory.getLogger(DriveDtoServiceImpl.class);

    public List<Long> filterByType(List<DriveDto> list, int type){
        return list.stream()
                .filter(dto -> dto.getDataType() == type)
                .map(DriveDto::getId)
                .collect(Collectors.toList());
    }
    public Set<Long> filterToSetByType(List<DriveDto> list, int type){
        return list.stream()
                .filter(dto -> dto.getDataType() == type)
                .map(DriveDto::getId)
                .collect(Collectors.toSet());
    }
    public List<String> filterNameByType(List<DriveDto> list, int type){
        return list.stream()
                .filter(dto -> dto.getDataType() == type)
                .map(DriveDto::getDataName)
                .collect(Collectors.toList());
    }
    @Override
    public boolean sameParentCheck(Long userId, List<DriveDto> list){
        List<Long> dataIds = list.stream()
                .map(DriveDto::getId)
                .collect(Collectors.toList());

        List<Long> count = driveDtoMapper.getDistinctParent(userId, dataIds);
        if(count.size() != 1) return false;

        return true;
    }

    // ----------------------------------Create---------------------------------- //
    @Override
    @Transactional
    public void createFile(MultipartFile part, Long userId, Long folderId, Integer location){
        File file = fileService.handlePartFile(part);
        file.setUserId(userId);
        file.setFolderId(folderId);
        file.setLocation(location);
        try{
            fileService.save(file);
            if(location == 0) fileService.upload(part, file);
            if(location == 1) remoteDataService.upload(part, file);
        }catch(DuplicateKeyException ex){
            GlobalExceptionHandler.logError(ex, ExceptionEnum.UNIQUE_CONFLICT);
            log.info("Start handling conflicted file...");
            handleConflictedFile(part, file, userId, folderId, location);
        }
    }
    public void retryCreateFile(MultipartFile part, File file, Long userId, Long folderId, Integer location){
        try{
            fileService.save(file);
            if(location == 0) fileService.upload(part, file);
            if(location == 1) remoteDataService.upload(part, file);
            log.info("Successful handle of conflicted file...");
        }catch(DuplicateKeyException ex){
            throw new InternalErrorException(ExceptionEnum.HANDLE_DUPLICATED_NEW_FILE_FAIL);
        }
    }
    public void handleConflictedFile(MultipartFile part, File file, Long userId, Long folderId, Integer location){
        List<String> fileNames = driveDtoMapper.getSubFiles(userId, folderId)
                .stream()
                .map(data -> {
                    return data.getDataName();
                })
                .collect(Collectors.toList());

        String newName = generateNewFileName(fileNames, file);
        file.setFileName(newName);

        retryCreateFile(part, file, userId, folderId, location);
    }
    public String generateNewFileName(List<String> fileNames, File file){
        System.out.println(fileNames);
        String ext = "." + file.getExt();
        String tmpName = file.getFileName() + "-複製";
        while(fileNames.contains(tmpName + ext)){
            tmpName += "-複製";
        }
        return tmpName;
    }
    // ----------------------------------Read---------------------------------- //
    @Override
    public Result getPublicFolder(Long folderId){
        DriveDto folder = driveDtoMapper.getPublicFolder(folderId);
        if(ObjectUtils.isEmpty(folder)) throw new NotFoundException(ExceptionEnum.NO_SUCH_DATA);
        List<DriveDto> publicData = driveDtoMapper.getPublicSub(folderId);
//        if(CollectionUtils.isEmpty(publicData)) throw new NotFoundException(ExceptionEnum.EMPTY_FOLDER);
        ListDriveDto listDriveDto = new ListDriveDto();
        listDriveDto.setId(folder.getId());
        listDriveDto.setName(folder.getDataName());
        listDriveDto.setList(publicData);

        return Result.success(listDriveDto);
    }
    @Override
    public Result getPublicSub(Long folderId){
        List<DriveDto> publicData = driveDtoMapper.getPublicSub(folderId);
//        if(CollectionUtils.isEmpty(publicData)) throw new NotFoundException(ExceptionEnum.EMPTY_FOLDER);

        return Result.success(publicData);
    }
    @Override
    public Result getSub(Long userId, Long folderId){
        List<DriveDto> privateData = driveDtoMapper.getSub(userId, folderId);
//        if(CollectionUtils.isEmpty(privateData)) throw new NotFoundException(ExceptionEnum.EMPTY_FOLDER);

        return Result.success(privateData);
    }

    @Override
    public ResponseEntity previewPublic(Long fileId){
        File file = fileService.findPublicFile(fileId);
        return preview(file);
    }
    @Override
    public ResponseEntity previewPersonal(Long userId, Long fileId){
        File file = fileService.findPersonalFile(userId, fileId);
        return preview(file);
    }

    public ResponseEntity preview(File file){
        // preview not allowed
        if(!isPreviewAllowed(file))
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Result.error(ExceptionEnum.PREVIEW_NOT_ALLOWED));

        Integer location = file.getLocation();

        if(location == 0){
            String path = fileService.findFilePath(file);
            return previewLocal(path);
        }
        if(location == 1) return previewRemote(file);

        return null;
    }
    public boolean isPreviewAllowed(File file){
        String ext = file.getExt().toUpperCase();
        return MimeEnum.valueOf(ext).allowPreview;
    }
    public ResponseEntity previewRemote(File file){
        String remoteFileName = fileService.findRemoteFileName(file);

        if(!StringUtils.hasText(remoteFileName)) return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(ExceptionEnum.FILE_NAME_ERROR));

        String mimeType = MimeEnum.getMimeType(file.getExt()); // workaround
        Long size = file.getSize();
        byte[] bytes = remoteDataService.getRemoteByteArray(remoteFileName);

        return ResponseEntity
                    .ok()
                    .contentType(MediaType.valueOf(mimeType))
                    .contentLength(size)
                    .body(bytes);
    }
    public ResponseEntity previewLocal(String path){
        if(!StringUtils.hasText(path)) return ResponseEntity
                                        .status(HttpStatus.NOT_FOUND)
                                        .body(Result.error(ExceptionEnum.NO_SUCH_DATA));

        String ext = path.substring(path.lastIndexOf(".") + 1);

        java.io.File ioFile = new java.io.File(path);
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
            throw new InternalErrorException(ExceptionEnum.FILE_IO_ERROR);
        }
    }

    @Override
    public ResponseEntity<ByteArrayResource> downloadPublic(Long fileId, HttpServletResponse response){
        File file = fileService.findPublicFile(fileId);
        String fileName = file.getFullName();
        if(!StringUtils.hasText(fileName)) throw new InternalErrorException(ExceptionEnum.FILE_NAME_ERROR);

        String path = fileService.findFilePath(file);
        return download(fileName, path, response);
    }
    @Override
    public ResponseEntity<ByteArrayResource> downloadPersonal(Long userId, Long fileId, HttpServletResponse response){
        File file = fileService.findPersonalFile(userId, fileId);
        String fileName = file.getFullName();
        if(!StringUtils.hasText(fileName)) throw new InternalErrorException(ExceptionEnum.FILE_NAME_ERROR);

        String path = fileService.findFilePath(file);
        return download(fileName, path, response);
    }
    public ResponseEntity<ByteArrayResource> download(String fileName, String path, HttpServletResponse response){
        if(!StringUtils.hasText(path)) throw new NotFoundException(ExceptionEnum.NO_SUCH_DATA);
        java.io.File file = new java.io.File(path);
        ByteArrayResource resource = null;
        HttpHeaders headers = new HttpHeaders();
        String encodedFileName = null;

        try {
            // file name encoding
            encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
            headers.add("Content-Disposition", "attachment;filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);

            // for front-end axios to get filename
            headers.add("Access-Control-Expose-Headers","Content-Disposition");

            byte[] bytes = FileCopyUtils.copyToByteArray(file);
            resource = new ByteArrayResource(bytes);

        } catch (UnsupportedEncodingException e) {
            throw new InternalErrorException(ExceptionEnum.FILE_NAME_ERROR);
        }catch (IOException ex){
            throw new InternalErrorException(ExceptionEnum.FILE_IO_ERROR);
        }
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    // ----------------------------------Update---------------------------------- //
    public int doXOR(int input){
        return input ^ 1;
    }
    @Override
    public void accessControl(DriveDto dto, Long userId){
        int dataType = dto.getDataType();
        if(dataType == 0){
            int XORAccessLevel = doXOR(dto.getAccessLevel());
            folderService.accessControl(dto.getId(), XORAccessLevel);

        }else if(dataType == 1){
            int XORAccessLevel = doXOR(dto.getAccessLevel());
            fileService.accessControl(dto.getId(), XORAccessLevel);

        }else{
            throw new BadRequestException(ExceptionEnum.PARAM_ERROR);
        }

    }
    @Override
    public void rename(DriveDto dto, Long userId){
        int dataType = dto.getDataType();
        if(dataType == 0){
            folderService.rename(dto.getId(), dto.getDataName(), userId);
        }else if(dataType == 1){
            fileService.rename(dto.getId(), dto.getDataName(), userId);
        }else{
            throw new BadRequestException(ExceptionEnum.PARAM_ERROR);
        }
    }

    @Override
    @Transactional
    public void relocate(Long destId, List<DriveDto> listDto, Long userId){
        List<Long> folderIds = filterByType(listDto, 0);
        List<Long> fileIds = filterByType(listDto, 1);

        if(folderIds.contains(destId)){
            log.warn(ExceptionEnum.NESTED_FOLDER.toStringDetails());
            log.info("Remove destination folder id from relocate pending list.");
            folderIds.remove(destId);
        }

        if(!CollectionUtils.isEmpty(folderIds)){
            folderService.relocate(destId, folderIds, userId);
        }
        if(!CollectionUtils.isEmpty(fileIds)){
            fileService.relocate(destId, fileIds, userId);
        }

    }

    @Override
    public List<DriveDto> getSuperFolderTree(Long userId, Long folderId){
        List<DriveDto> list = driveDtoMapper.getSuperFolderTree(userId, folderId);
        if(CollectionUtils.isEmpty(list)) throw new NotFoundException(ExceptionEnum.NO_SUCH_DATA);
        Collections.reverse(list);
        return list;
    }
    @Override
    public List<DriveDto> getSubFolders(Long userId, Long folderId){
        List<DriveDto> list = driveDtoMapper.getSubFolders(userId, folderId);
//        if(CollectionUtils.isEmpty(list)) throw new NotFoundException(ExceptionEnum.NO_SUCH_DATA);
        return list;
    }
    // ----------------------------------Delete: clean & recover---------------------------------- //
    @Override
    @Transactional
    public void gotoTrash(Long userId, List<DriveDto> listDto){
        List<Long> folderIds = filterByType(listDto, 0);
        List<Long> fileIds = filterByType(listDto, 1);

        if(!CollectionUtils.isEmpty(folderIds)){
            folderService.gotoTrash(userId, folderIds);
        }
        if(!CollectionUtils.isEmpty(fileIds)){
            fileService.gotoTrash(userId, fileIds);
        }
    }
    @Override
    @Transactional
    public void recover(Long userId, List<DriveDto> listDto) {
        List<Long> folderIds = filterByType(listDto, 0);
        List<Long> fileIds = filterByType(listDto, 1);

        if(!CollectionUtils.isEmpty(folderIds)){
            folderService.recover(userId, folderIds);
        }
        if(!CollectionUtils.isEmpty(fileIds)){
            fileService.recover(userId, fileIds);
        }
    }

    @Override
    public void clean(Long userId) {
        List<DriveDto> trash = driveDtoMapper.getSubIds(userId, trashId);
        if(CollectionUtils.isEmpty(trash)) throw new NotFoundException(ExceptionEnum.NO_SUCH_DATA);

        softDelete(userId, trash);
    }

    @Override
    @Transactional
    public void softDelete(Long userId, List<DriveDto> listDto) {

        // add to pending lists
        // use Set to avoid deleting repeatedly
        Set<Long> folderIds = filterToSetByType(listDto, 0);
        Set<Long> fileIds = filterToSetByType(listDto, 1);

        // init tmp lists
        List<Long> tmpFolderIds = new ArrayList<>();
        List<Long> tmpFileIds = new ArrayList<>();

        // find sub folders and files
        if(!CollectionUtils.isEmpty(folderIds)){

            folderIds.forEach(folderId -> {
                List<DriveDto> subDataList = driveDtoMapper.getSubTree(userId, folderId);
                tmpFolderIds.addAll(filterByType(subDataList, 0));
                tmpFileIds.addAll(filterByType(subDataList, 1));
            });

            folderIds.addAll(tmpFolderIds);
            fileIds.addAll(tmpFileIds);

            folderService.softDelete(userId, new ArrayList<>(folderIds)); // workaround casting
        }

        if(!CollectionUtils.isEmpty(fileIds)){
            fileService.softDelete(userId, new ArrayList<>(fileIds)); // workaround casting
        }
    }

    @Override
    @Transactional
    public void conflictTrash(Long userId, List<DriveDto> listDto) {
        // find conflicted data in trashcan
        List<String> folders = filterNameByType(listDto, 0);
        List<String> files = filterNameByType(listDto, 1);
        List<DriveDto> conflictedData = driveDtoMapper.getConflictedTrash(userId, folders, files);

        // handle conflicts
        softDelete(userId, conflictedData);

        // move new data into trashcan
        gotoTrash(userId, listDto);
    }
}
