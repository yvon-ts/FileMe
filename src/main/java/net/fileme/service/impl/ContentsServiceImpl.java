package net.fileme.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.fileme.domain.mapper.FileMapper;
import net.fileme.domain.mapper.FolderMapper;
import net.fileme.domain.pojo.File;
import net.fileme.domain.pojo.Folder;
import net.fileme.service.ContentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contents = Folders + Files
 */
@Service
public class ContentsServiceImpl implements ContentsService {

    @Autowired
    private FolderMapper folderMapper;
    @Autowired
    private FileMapper fileMapper;

    @Override
    public Map<String, List<Object>> getContents(Long userId, Long folderId) {

        Map<String, List<Object>> map = new HashMap<>();

        LambdaQueryWrapper<Folder> wrapperFolder = new LambdaQueryWrapper<>();
        wrapperFolder.eq(Folder::getUserId, userId);
        List<Folder> folders = folderMapper.selectList(wrapperFolder);

        map.put("folders", new ArrayList<Object>(folders));

        LambdaQueryWrapper<File> wrapperFile = new LambdaQueryWrapper<>();
        wrapperFile.eq(File::getUserId, userId)
                .eq(File::getFolderId, folderId); // existing file
        List<File> files = fileMapper.selectList(wrapperFile);

        map.put("files", new ArrayList<Object>(files));

        return map;
    }
}
