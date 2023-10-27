package net.fileme.service;

import net.fileme.domain.MyUserDetails;
import net.fileme.domain.dto.DriveDto;

import java.util.List;

public interface ValidateService {
    void checkFolder(Long userId, Long folderId);
    Long checkUserAndListDto(MyUserDetails myUserDetails, List<DriveDto> listDto);
}
