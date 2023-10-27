package net.fileme.service.impl;

import net.fileme.domain.MyUserDetails;
import net.fileme.domain.dto.DriveDto;
import net.fileme.domain.mapper.DriveDtoMapper;
import net.fileme.enums.ExceptionEnum;
import net.fileme.exception.BadRequestException;
import net.fileme.exception.UnauthorizedException;
import net.fileme.service.DriveDtoService;
import net.fileme.service.ValidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Service
public class ValidateServiceImpl implements ValidateService {

    @Autowired
    private DriveDtoMapper driveDtoMapper;
    @Autowired
    private DriveDtoService driveDtoService;

    @Value("${file.root.folderId}")
    private Long rootId;

    @Value("${file.trash.folderId}")
    private Long trashId;

    @Override
    public void checkFolder(Long userId, Long folderId){
        if(!rootId.equals(folderId) && !trashId.equals(folderId)){
            DriveDto driveDto = driveDtoMapper.getOneFolder(userId, folderId);
            if(Objects.isNull(driveDto)) throw new BadRequestException(ExceptionEnum.PARAM_ERROR);
        }
    }
    
    @Override
    public Long checkUserAndListDto(MyUserDetails myUserDetails, List<DriveDto> listDto) {
        if (Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
//        if (CollectionUtils.isEmpty(listDto)) throw new BadRequestException(ExceptionEnum.PARAM_EMPTY);
        Long userId = myUserDetails.getUser().getId();

        // remove system folders
        listDto.removeIf(dto -> rootId.equals(dto.getId()) || trashId.equals(dto.getId()));
        if(CollectionUtils.isEmpty(listDto)) throw new BadRequestException(ExceptionEnum.PARAM_ERROR);
        
        boolean parentCheck = driveDtoService.sameParentCheck(userId, listDto);
        if (!parentCheck) throw new BadRequestException(ExceptionEnum.NOT_SAME_PARENT);

        return userId;
    }
}
