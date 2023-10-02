package net.fileme.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.fileme.domain.mapper.FolderMapper;
import net.fileme.domain.mapper.FolderTrashMapper;
import net.fileme.domain.pojo.Folder;
import net.fileme.exception.BadRequestException;
import net.fileme.exception.NotFoundException;
import net.fileme.service.CheckExistService;
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
import java.util.stream.Collectors;

@Service
@PropertySource("classpath:credentials.properties")
public class FolderServiceImpl extends ServiceImpl<FolderMapper, Folder>
        implements FolderService {
    @Value("${file.upload.dir}") // 名字可以再換
    private String remotePathPrefix;
    @Value("${file.trash.folderId}")
    private Long trashId;
    @Autowired
    private FolderTrashMapper folderTrashMapper;
    @Autowired
    private FileService fileService;
    @Autowired
    private CheckExistService checkExistService;

    private Logger log = LoggerFactory.getLogger(FolderService.class);

    @Override
    public void createFolder(Folder fd){
        Long userId = fd.getUserId();
        String name = fd.getFolderName();
        Long parentId = fd.getParentId();

        boolean isValid = checkExistService.checkValidFolder(userId, parentId);
        if(!isValid) {
            throw new BadRequestException(ExceptionEnum.PARAM_ERROR);
        }
        Folder folder = new Folder();
        folder.setUserId(userId);
        folder.setFolderName(name);
        folder.setParentId(parentId);
        save(folder);
    }
    @Override
    public void rename(Long dataId, String newName){
        LambdaUpdateWrapper<Folder> luw = new LambdaUpdateWrapper<>();
        luw.set(Folder::getFolderName, newName).eq(Folder::getId, dataId);
        boolean success = update(luw);
        if(!success){
            throw new NotFoundException(ExceptionEnum.FOLDER_NOT_EXISTS);
        }
    }

    @Override
    public List<Long> getTrashIds(Long userId) {
        LambdaQueryWrapper<Folder> lqw = new LambdaQueryWrapper<>();
        lqw.select((Folder::getId)).eq(Folder::getUserId, userId).eq(Folder::getParentId, trashId);
        List<String> tmp = listObjs(lqw, Object::toString);
        List<Long> trashIds = tmp.stream().map(Long::valueOf).collect(Collectors.toList());
        return trashIds;
    }

    @Override
    public void relocate(Long parentId, List<Long> dataIds) {
        LambdaUpdateWrapper<Folder> luw = new LambdaUpdateWrapper<>();
        luw.set(Folder::getParentId, parentId).in(Folder::getId, dataIds);
        boolean success = update(luw);
        if(!success){
            throw new NotFoundException(ExceptionEnum.UPDATE_DB_FAIL);
        }
    }

    @Override
    @Transactional
    public void gotoTrash(List<Long> dataIds) {
        folderTrashMapper.create(dataIds);
        relocate(trashId, dataIds);
    }

    @Override
    @Transactional
    public void recover(List<Long> dataIds) {
        folderTrashMapper.recover(dataIds);
        folderTrashMapper.deleteBatchIds(dataIds);
    }

    @Override
    @Transactional
    public void softDelete(List<Long> dataIds) {
        folderTrashMapper.deleteBatchIds(dataIds);
        removeByIds(dataIds);
    }
}
