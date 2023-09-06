package net.fileme.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.fileme.domain.mapper.FileMapper;
import net.fileme.domain.mapper.FolderMapper;
import net.fileme.domain.mapper.UserMapper;
import net.fileme.domain.pojo.File;
import net.fileme.domain.pojo.Folder;
import net.fileme.domain.pojo.User;
import net.fileme.exception.BizException;
import net.fileme.service.CheckExistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CheckExistServiceImpl implements CheckExistService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FolderMapper folderMapper;

    @Autowired
    private FileMapper fileMapper;

    @Override
    public int checkExistUser(Long userId) throws BizException{
        LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
        lqw.eq(User::getId, userId);
        return userMapper.selectCount(lqw);
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
    @Override
    public Map<String, Integer> checkContents(Long userId, Long folderId){
        Map<String, Integer> contents = new HashMap<>();
        int countFolder = checkSubFolder(userId, folderId);
        int countFile = checkSubFile(userId, folderId);
        if(countFolder != 0){
            contents.put("subFolder", countFolder);
        }
        if(countFile != 0){
            contents.put("subFile", countFile);
        }
        System.out.println("*********************");
        System.out.println(contents);
        return contents;
    }
}
