package net.fileme.service.impl;

import net.fileme.domain.dto.DriveDto;
import net.fileme.domain.mapper.DriveDtoMapper;
import net.fileme.service.ValidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ValidateServiceImpl implements ValidateService {

    @Autowired
    private DriveDtoMapper driveDtoMapper;

    @Value("${file.root.folderId}")
    private Long rootId;

    @Value("${file.trash.folderId}")
    private Long trashId;

    @Override
    public boolean validateFolder(Long userId, Long folderId){
        if(!rootId.equals(folderId) && !trashId.equals(folderId)){
            DriveDto folderDto = driveDtoMapper.getFolderDto(userId, folderId);
            return !Objects.isNull(folderDto);
        }
        return true;
    }
}
