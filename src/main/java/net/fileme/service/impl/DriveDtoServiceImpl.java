package net.fileme.service.impl;

import net.fileme.domain.dto.DriveDto;
import net.fileme.domain.dto.FileFolderDto;
import net.fileme.domain.Result;
import net.fileme.exception.BadRequestException;
import net.fileme.exception.InternalErrorException;
import net.fileme.exception.NotFoundException;
import net.fileme.enums.ExceptionEnum;
import net.fileme.enums.MimeEnum;
import net.fileme.service.DataTreeService;
import net.fileme.service.DriveDtoService;
import net.fileme.service.FileService;
import net.fileme.service.FolderService;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DriveDtoServiceImpl implements DriveDtoService {
    @Autowired
    private FolderService folderService;
    @Autowired
    private FileService fileService;
    @Autowired
    private DataTreeService dataTreeService;

    @Override
    public ResponseEntity preview(Long userId, Long fileId){
        String path = dataTreeService.findFilePath(userId, fileId);
        String ext = path.substring(path.lastIndexOf(".") + 1);

        // check if allowed to preview
        if(MimeEnum.valueOf(ext.toUpperCase()).allowPreview){
            java.io.File ioFile = new java.io.File(path);
            if(!ioFile.exists()){
                throw new NotFoundException(ExceptionEnum.FILE_ERROR);
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
                throw new InternalErrorException(ExceptionEnum.FILE_ERROR);
            }
        }
        // preview not allowed
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Result.error(ExceptionEnum.PREVIEW_NOT_ALLOWED));
    }
    @Override
    public void rename(DriveDto dto){
        int dataType = dto.getDataType();
        if(dataType == 0){
            folderService.rename(dto.getId(), dto.getDataName());
        }else if(dataType == 1){
            fileService.rename(dto.getId(), dto.getDataName());
        }else{
            throw new BadRequestException(ExceptionEnum.PARAM_ERROR);
        }
    }
    @Override
    @Transactional
    public void relocate(Long destId, FileFolderDto dto) {
        List<Long> folderIds = dto.getFolderIds();
        List<Long> fileIds = dto.getFileIds();

        if(CollectionUtils.isEmpty(folderIds) && CollectionUtils.isEmpty(fileIds)){
            throw new BadRequestException(ExceptionEnum.PARAM_EMPTY);
        }
        if(!CollectionUtils.isEmpty(folderIds)){
            folderService.relocate(destId, folderIds);
        }
        if(!CollectionUtils.isEmpty(fileIds)){
            fileService.relocate(destId, fileIds);
        }
    }
    @Override
    @Transactional
    public void gotoTrash(FileFolderDto dto){
        List<Long> folderIds = dto.getFolderIds();
        List<Long> fileIds = dto.getFileIds();

        if(CollectionUtils.isEmpty(folderIds) && CollectionUtils.isEmpty(fileIds)){
            throw new BadRequestException(ExceptionEnum.PARAM_EMPTY);
        }

        if(!CollectionUtils.isEmpty(folderIds)){
            folderService.gotoTrash(folderIds);
        }
        if(!CollectionUtils.isEmpty(fileIds)){
            fileService.gotoTrash(fileIds);
        }
    }
    @Override
    @Transactional
    public void recover(FileFolderDto dto) {
        List<Long> folderIds = dto.getFolderIds();
        List<Long> fileIds = dto.getFileIds();

        if(CollectionUtils.isEmpty(folderIds) && CollectionUtils.isEmpty(fileIds)){
            throw new BadRequestException(ExceptionEnum.PARAM_EMPTY);
        }

        if(!CollectionUtils.isEmpty(folderIds)){
            folderService.recover(folderIds);
        }
        if(!CollectionUtils.isEmpty(fileIds)){
            fileService.recover(fileIds);
        }
    }

    @Override
    public void clean(Long userId) {
        List<Long> folderIds = folderService.getTrashIds(userId);
        List<Long> fileIds = fileService.getTrashIds(userId);
        FileFolderDto dto = new FileFolderDto();
        dto.setFolderIds(folderIds);
        dto.setFileIds(fileIds);

        softDelete(userId, dto);
    }

    @Override
    @Transactional
    public void softDelete(Long userId, FileFolderDto dto) {
        List<Long> folderIds = dto.getFolderIds();
        List<Long> fileIds = dto.getFileIds();

        if(CollectionUtils.isEmpty(folderIds) && CollectionUtils.isEmpty(fileIds)){
            throw new BadRequestException(ExceptionEnum.PARAM_EMPTY);
        }

        List<Long> tmpFolderIds = new ArrayList<>();
        List<Long> tmpFileIds = new ArrayList<>();

        // handle sub data
        if(!CollectionUtils.isEmpty(folderIds)){

            folderIds.forEach(folderId -> {
                FileFolderDto treeDto = dataTreeService.findTreeIds(userId, folderId);
                tmpFolderIds.addAll(treeDto.getFolderIds());
                tmpFileIds.addAll(treeDto.getFileIds());
            });

            folderIds.addAll(tmpFolderIds);
            fileIds.addAll(tmpFileIds);
            folderService.softDelete(folderIds);
        }

        if(!CollectionUtils.isEmpty(fileIds)){
            fileService.softDelete(fileIds);
        }
    }
}
