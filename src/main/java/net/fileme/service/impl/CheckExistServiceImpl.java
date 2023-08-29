package net.fileme.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.fileme.domain.mapper.FileMapper;
import net.fileme.domain.mapper.FolderMapper;
import net.fileme.domain.mapper.UserMapper;
import net.fileme.domain.pojo.Folder;
import net.fileme.domain.pojo.User;
import net.fileme.exception.BizException;
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
    public int checkExistUser(Long userId) throws BizException{
        LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
        lqw.eq(User::getId, userId);
        return userMapper.selectCount(lqw);
    }

    @Override
    public int checkExistFolder(Long folderId) {
        LambdaQueryWrapper<Folder> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Folder::getId, folderId);
        return folderMapper.selectCount(lqw);
    }

    @Override
    public int checkExistFile(Long fileId) {
        return 0;
    }
}
