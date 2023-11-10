package net.fileme.service.impl;

import net.fileme.domain.dto.DriveDto;
import net.fileme.domain.Result;
import net.fileme.domain.mapper.DriveDtoMapper;
import net.fileme.domain.pojo.File;
import net.fileme.exception.BadRequestException;
import net.fileme.exception.InternalErrorException;
import net.fileme.exception.NotFoundException;
import net.fileme.enums.ExceptionEnum;
import net.fileme.enums.MimeEnum;
import net.fileme.service.DriveDtoService;
import net.fileme.service.FileService;
import net.fileme.service.FolderService;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    @Override
    public boolean sameParentCheck(Long userId, List<DriveDto> list){
        List<Long> dataIds = list.stream()
                .map(DriveDto::getId)
                .collect(Collectors.toList());

        List<Long> count = driveDtoMapper.getDistinctParent(userId, dataIds);
        if(count.size() != 1) return false;

        return true;
    }

    // ----------------------------------Read---------------------------------- //

    @Override
    public Result getPublicSub(Long folderId){
        List<DriveDto> publicData = driveDtoMapper.getPublicSub(folderId);
        if(CollectionUtils.isEmpty(publicData)) throw new NotFoundException(ExceptionEnum.NO_SUCH_DATA);

        return Result.success(publicData);
    }
    @Override
    public Result getSub(Long userId, Long folderId){
        List<DriveDto> privateData = driveDtoMapper.getSub(userId, folderId);
        if(CollectionUtils.isEmpty(privateData)) throw new NotFoundException(ExceptionEnum.NO_SUCH_DATA);

        return Result.success(privateData);
    }
    @Override
    public ResponseEntity previewPublic(Long fileId){
        File file = fileService.findPublicFile(fileId);
        String path = fileService.findFilePath(file);
        return preview(path);
    }
    @Override
    public ResponseEntity previewPersonal(Long userId, Long fileId){
        File file = fileService.findPersonalFile(userId, fileId);
        String path = fileService.findFilePath(file);
        return preview(path);
    }
    public ResponseEntity preview(String path){
        if(!StringUtils.hasText(path)) return ResponseEntity
                                        .status(HttpStatus.NOT_FOUND)
                                        .body(Result.error(ExceptionEnum.NO_SUCH_DATA));

        String ext = path.substring(path.lastIndexOf(".") + 1);

        // check mime type if allowed to preview
        if(MimeEnum.valueOf(ext.toUpperCase()).allowPreview){
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
        // preview not allowed
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Result.error(ExceptionEnum.PREVIEW_NOT_ALLOWED));
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
        try{
            byte[] bytes = FileCopyUtils.copyToByteArray(file);
            resource = new ByteArrayResource(bytes);
        }catch (IOException ex){
            throw new InternalErrorException(ExceptionEnum.FILE_IO_ERROR);
        }
        return ResponseEntity.ok()
                .header("Content-Disposition","attachment;filename=" + new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1))
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
        if(CollectionUtils.isEmpty(list)) throw new NotFoundException(ExceptionEnum.NO_SUCH_DATA);
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
}
