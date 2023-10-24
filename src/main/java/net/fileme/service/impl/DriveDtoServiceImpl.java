package net.fileme.service.impl;

import net.fileme.domain.dto.DriveDto;
import net.fileme.domain.dto.FileFolderDto;
import net.fileme.domain.Result;
import net.fileme.domain.mapper.DriveDtoMapper;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DriveDtoServiceImpl implements DriveDtoService {
    @Autowired
    private FolderService folderService;
    @Autowired
    private FileService fileService;
    @Autowired
    private DataTreeService dataTreeService;
    @Autowired
    private DriveDtoMapper driveDtoMapper;

    private Logger log = LoggerFactory.getLogger(DriveDtoServiceImpl.class);

    @Override
    public Result publicData(Long folderId){
        List<DriveDto> publicData = driveDtoMapper.getPublicData(folderId);
        if(CollectionUtils.isEmpty(publicData)) throw new BadRequestException(ExceptionEnum.NO_SUCH_DATA);

        return Result.success(publicData);
    }
    @Override
    public Result getData(Long userId, Long folderId){
        List<DriveDto> privateData = driveDtoMapper.getData(userId, folderId);
        if(CollectionUtils.isEmpty(privateData)) throw new NotFoundException(ExceptionEnum.NO_SUCH_DATA);

        return Result.success(privateData);
    }
    @Override
    public ResponseEntity previewPublic(Long fileId){
        String path = dataTreeService.findPublicFilePath(fileId);
        return preview(path);
    }
    @Override
    public ResponseEntity previewPersonal(Long userId, Long fileId){
        String path = dataTreeService.findPersonalFilePath(userId, fileId);
        return preview(path);
    }
    public ResponseEntity preview(String path){
        if(!StringUtils.hasText(path)) return ResponseEntity
                                        .status(HttpStatus.NOT_FOUND)
                                        .body(Result.error(ExceptionEnum.FILE_NOT_EXISTS));

        String ext = path.substring(path.lastIndexOf(".") + 1);

        // check mime type if allowed to preview
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
        List<Long> folderIds = getListDataId(listDto, 0);
        List<Long> fileIds = getListDataId(listDto, 1);

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
    public List<Long> getListDataId(List<DriveDto> list, int type){
        return list.stream()
                .filter(dto -> dto.getDataType() == type)
                .map(DriveDto::getId)
                .collect(Collectors.toList());
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
