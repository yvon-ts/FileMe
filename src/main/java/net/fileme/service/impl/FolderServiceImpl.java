package net.fileme.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.fileme.domain.mapper.FolderMapper;
import net.fileme.domain.mapper.FolderTrashMapper;
import net.fileme.domain.pojo.Folder;
import net.fileme.exception.InternalErrorException;
import net.fileme.exception.NotFoundException;
import net.fileme.service.FileService;
import net.fileme.service.FolderService;
import net.fileme.enums.ExceptionEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@PropertySource("classpath:credentials.properties")
public class FolderServiceImpl extends ServiceImpl<FolderMapper, Folder>
        implements FolderService {
    @Value("${file.upload.dir}") // 名字可以再換
    private String remotePathPrefix;
    @Value("${file.trash.folderId}")
    private Long trashId;
    @Value("${regex.folder.name}")
    private String regex;
    @Autowired
    private FolderTrashMapper folderTrashMapper;
    @Autowired
    private FileService fileService;

    private Logger log = LoggerFactory.getLogger(FolderService.class);

    @Override
    public void createFolder(Long userId, Long parentId, String name){
//        if(!Pattern.matches(regex, name)) throw new BadRequestException(ExceptionEnum.FOLDER_NAME_REGEX_ERROR);
        Folder folder = new Folder();
        folder.setUserId(userId);
        folder.setFolderName(name);
        folder.setParentId(parentId);
        save(folder);
    }
    @Override
    public void accessControl(Long dataId, int newAccess){
        LambdaUpdateWrapper<Folder> luw = new LambdaUpdateWrapper<>();
        luw.set(Folder::getAccessLevel, newAccess)
                .eq(Folder::getId, dataId);
        boolean success = update(luw);
        if(!success){
            throw new NotFoundException(ExceptionEnum.SET_FOLDER_ACCESS_FAIL);
        }
    }
    @Override
    public void rename(Long dataId, String newName, Long userId){
        LambdaUpdateWrapper<Folder> luw = new LambdaUpdateWrapper<>();
        luw.set(Folder::getFolderName, newName)
                .eq(Folder::getId, dataId)
                .eq(Folder::getUserId, userId);
        boolean success = update(luw);
        if(!success){
            throw new NotFoundException(ExceptionEnum.FOLDER_RENAME_FAIL);
        }
    }

    @Override
    public void relocate(Long parentId, List<Long> dataIds, Long userId) {
        LambdaUpdateWrapper<Folder> luw = new LambdaUpdateWrapper<>();
        luw.set(Folder::getParentId, parentId)
                .in(Folder::getId, dataIds)
                .eq(Folder::getUserId, userId);
        boolean success = update(luw);
        if(!success){
            throw new NotFoundException(ExceptionEnum.FOLDER_RELOCATE_FAIL);
        }
    }

    @Override
    @Transactional
    public void gotoTrash(Long userId, List<Long> dataIds) {
        int successCreate = folderTrashMapper.create(userId, dataIds);
        if(successCreate == 0) throw new InternalErrorException(ExceptionEnum.CREATE_FOLDER_TRASH_FAIL);
        relocate(trashId, dataIds, userId);
    }

    @Override
    @Transactional
    public void recover(Long userId, List<Long> dataIds) {
        int successRecover = folderTrashMapper.recover(userId, dataIds);
        if(successRecover == 0) throw new InternalErrorException(ExceptionEnum.FOLDER_RECOVER_FAIL);
        int successDelete = folderTrashMapper.deleteBatchIds(dataIds);
        if(successDelete == 0) throw new InternalErrorException(ExceptionEnum.DELETE_FOLDER_TRASH_FAIL);
    }

    @Override
    @Transactional
    public void softDelete(Long userId, List<Long> dataIds) {
        folderTrashMapper.deleteBatchIds(dataIds);
        LambdaUpdateWrapper<Folder> luw = new LambdaUpdateWrapper<>();
        luw.eq(Folder::getUserId, userId)
            .in(Folder::getId, dataIds);
        boolean success = remove(luw);
        if(!success) throw new InternalErrorException(ExceptionEnum.DELETE_FOLDER_FAIL);
    }
}
