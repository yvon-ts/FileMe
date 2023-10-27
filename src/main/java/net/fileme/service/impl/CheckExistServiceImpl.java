package net.fileme.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.fileme.domain.mapper.FileMapper;
import net.fileme.domain.mapper.FolderMapper;
import net.fileme.domain.mapper.UserMapper;
import net.fileme.domain.pojo.File;
import net.fileme.domain.pojo.Folder;
import net.fileme.service.CheckExistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CheckExistServiceImpl implements CheckExistService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FolderMapper folderMapper;

    @Autowired
    private FileMapper fileMapper;

    @Override
    public boolean checkValidFolder(Long userId, Long folderId){
        if(folderId.equals(0L)) return true;

        int count = checkExistFolder(userId, folderId);
        if(count == 1) return true;

        return false;
    }

    @Override
    public int checkExistFolder(Long userId, Long folderId) {
        LambdaQueryWrapper<Folder> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Folder::getUserId, userId).eq(Folder::getId, folderId);
        return folderMapper.selectCount(lqw);
    }



    @Override
    public int checkExistFile(Long fileId) {
        return 0;
    }

    @Override
    public int checkSubFolder(Long userId, Long parentId) {
        LambdaQueryWrapper<Folder> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Folder::getUserId, userId).eq(Folder::getParentId, parentId);
        return folderMapper.selectCount(lqw);
    }

    @Override
    public int checkSubFile(Long userId, Long folderId) {
        LambdaQueryWrapper<File> lqw = new LambdaQueryWrapper<>();
        lqw.eq(File::getUserId, userId).eq(File::getFolderId,folderId);
        return fileMapper.selectCount(lqw);
    }

}
