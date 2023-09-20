package net.fileme.service;

import net.fileme.domain.DriveDto;
import net.fileme.domain.FileFolderDto;
import net.fileme.exception.BadRequestException;
import net.fileme.utils.enums.ExceptionEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class DtoServiceImpl implements DtoService{
    @Autowired
    private FolderService folderService;
    @Autowired
    private FileService fileService;

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
}
