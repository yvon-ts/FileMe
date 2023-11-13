package net.fileme.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import net.fileme.domain.MyUserDetails;
import net.fileme.domain.dto.DriveDto;
import net.fileme.domain.mapper.DriveDtoMapper;
import net.fileme.domain.mapper.UserMapper;
import net.fileme.domain.pojo.User;
import net.fileme.enums.ExceptionEnum;
import net.fileme.exception.BadRequestException;
import net.fileme.exception.ConflictException;
import net.fileme.exception.NotFoundException;
import net.fileme.exception.UnauthorizedException;
import net.fileme.service.DriveDtoService;
import net.fileme.service.ValidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
public class ValidateServiceImpl implements ValidateService {

    @Autowired
    private DriveDtoMapper driveDtoMapper;
    @Autowired
    private DriveDtoService driveDtoService;
    @Autowired
    private UserMapper userMapper;

    @Value("${file.root.folderId}")
    private Long rootId;
    @Value("${file.trash.folderId}")
    private Long trashId;
    @Value("${regex.email}")
    private String regexEmail;
    @Value("${regex.pwd}")
    private String regexPwd;
    @Value("${regex.username.forbidden}")
    private String regexUsernameForbidden;


    /**
     * can remove when @NotBlank @Email validation works on controller
     */
    @Override
    public void regexEmail(String email){
        if(!Pattern.matches(regexEmail, email)) throw new BadRequestException(ExceptionEnum.USER_EMAIL_REGEX_ERROR);
    }

    /**
     * can remove when @NotBlank @Pattern validation works on controller
     */
    @Override
    public void regexPwd(String pwd){
        if(!Pattern.matches(regexPwd, pwd)) throw new BadRequestException(ExceptionEnum.PWD_REGEX_ERROR);
    }

    public void regexUsername(String username){
        if(!Pattern.matches(regexUsernameForbidden, username)) throw new BadRequestException(ExceptionEnum.USERNAME_REGEX_ERROR);
    }

    @Override
    public void checkUserName(String username){
        if(StringUtils.isBlank(username)) throw new BadRequestException(ExceptionEnum.PARAM_ERROR);
        regexUsername(username);
        LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
        lqw.eq(User::getUsername, username).ne(User::getState, 99); // invalid user
        Integer count = userMapper.selectCount(lqw);
        if(count != 0) throw new ConflictException(ExceptionEnum.EXISTING_USERNAME);
    }
    @Override
    public void checkEmail(String email){
        if(StringUtils.isBlank(email)) throw new BadRequestException(ExceptionEnum.PARAM_ERROR);
        regexEmail(email);
        LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
        lqw.eq(User::getEmail, email).ne(User::getState, 99); // invalid user
        Integer count = userMapper.selectCount(lqw);
        if(count != 0) throw new ConflictException(ExceptionEnum.EXISTING_EMAIL);
    }

    @Override
    public DriveDto checkData(Long userId, Long dataId){
        if(rootId.equals(dataId) || trashId.equals(dataId)) throw new BadRequestException(ExceptionEnum.PARAM_ERROR);
        DriveDto data = driveDtoMapper.getOneData(userId, dataId);
        if(Objects.isNull(data)) throw new NotFoundException(ExceptionEnum.NO_SUCH_DATA);
        return data;
    }
    
    @Override
    public DriveDto checkFolder(Long userId, Long folderId){
        if(trashId.equals(folderId)) throw new BadRequestException(ExceptionEnum.PARAM_ERROR);
        if(!rootId.equals(folderId)){
            DriveDto folder = driveDtoMapper.getOneFolder(userId, folderId);
            if(Objects.isNull(folder)) throw new NotFoundException(ExceptionEnum.NO_SUCH_DATA);
            return folder;
        }
        return null; // do nothing when folder = root
    }
    @Override
    public DriveDto checkFile(Long userId, Long fileId){
        DriveDto file = driveDtoMapper.getOneFile(userId, fileId);
        if(Objects.isNull(file)) throw new NotFoundException(ExceptionEnum.NO_SUCH_DATA);
        return file;
    }
    @Override
    public DriveDto checkPublicFolder(Long folderId){
        if(rootId.equals(folderId) || trashId.equals(folderId)) throw new BadRequestException(ExceptionEnum.PARAM_ERROR);
        DriveDto folder = driveDtoMapper.getPublicFolder(folderId);
        if(Objects.isNull(folder)) throw new NotFoundException(ExceptionEnum.NO_SUCH_DATA);
        return folder;
    }
    @Override
    public Long checkUserAndListDto(MyUserDetails myUserDetails, List<DriveDto> listDto) {
        if (Objects.isNull(myUserDetails)) throw new UnauthorizedException(ExceptionEnum.GUEST_NOT_ALLOWED);
        Long userId = myUserDetails.getUser().getId();

        // remove system folders
        listDto.removeIf(dto -> rootId.equals(dto.getId()) || trashId.equals(dto.getId()));
        if(CollectionUtils.isEmpty(listDto)) throw new BadRequestException(ExceptionEnum.PARAM_ERROR);
        
        boolean parentCheck = driveDtoService.sameParentCheck(userId, listDto);
        if (!parentCheck) throw new BadRequestException(ExceptionEnum.NOT_SAME_PARENT);

        return userId;
    }
}
