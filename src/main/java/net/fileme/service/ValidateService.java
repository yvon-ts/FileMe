package net.fileme.service;

import net.fileme.domain.MyUserDetails;
import net.fileme.domain.dto.DriveDto;

import java.util.List;

public interface ValidateService {

    void regexEmail(String email);

    void regexPwd(String pwd);

    void checkUserName(String username);

    void checkEmail(String email);

    DriveDto checkData(Long userId, Long dataId);
    DriveDto checkFolder(Long userId, Long folderId);
    DriveDto checkFile(Long userId, Long fileId);

    DriveDto checkPublicFolder(Long folderId);

    Long checkUserAndListDto(MyUserDetails myUserDetails, List<DriveDto> listDto);

    void checkUserByEmail(String email);
}
