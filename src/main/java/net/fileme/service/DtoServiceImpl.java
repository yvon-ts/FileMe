package net.fileme.service;

import net.fileme.domain.DriveDto;
import net.fileme.domain.FileFolderDto;
import net.fileme.exception.BadRequestException;
import net.fileme.utils.enums.ExceptionEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class DtoServiceImpl implements DtoService{
    @Autowired
    private FolderService folderService;
    @Autowired
    private FileService fileService;
    @Autowired
    private DataTreeService dataTreeService;

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
