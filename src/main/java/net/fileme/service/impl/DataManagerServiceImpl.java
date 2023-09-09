package net.fileme.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import net.fileme.domain.mapper.TrashMapper;
import net.fileme.domain.pojo.File;
import net.fileme.domain.pojo.Folder;
import net.fileme.service.DataManagerService;
import net.fileme.service.FileService;
import net.fileme.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataManagerServiceImpl implements DataManagerService {
    @Value("${file.trash.folderId}")
    private Long trashId;

    @Autowired
    private FolderService folderService;
    @Autowired
    private FileService fileService;
    @Autowired
    private TrashMapper trashMapper;

    @Override
    public void relocateFolders(Long parentId, List<Long> folderIds) {
        LambdaUpdateWrapper<Folder> luw = new LambdaUpdateWrapper<>();
        luw.set(Folder::getParentId, parentId).in(Folder::getId, folderIds);
        folderService.update(luw);
    }

    @Override
    public void relocateFiles(Long folderId, List<Long> fileIds) {
        LambdaUpdateWrapper<File> luw = new LambdaUpdateWrapper<>();
        luw.set(File::getFolderId, folderId).in(File::getId, fileIds);
        fileService.update(luw);
    }

    @Override
    public void toTrashFolders(List<Long> folderIds) {
        //寫xml: insert into trash(...) select(...)from folder where folder_id = xx
        relocateFolders(trashId, folderIds);
    }

    @Override
    public void toTrashFiles(List<Long> fileIds) {
        //寫xml
        relocateFiles(trashId, fileIds);
    }

    @Override
    public void recoverFolders(Long userId, List<Long> folderIds) {
        //xml
        //而且要把資料從trash刪掉
        //要用querymapper 還是 updatemapper????
    }

    @Override
    public void recoverFiles(Long userId, List<Long> fileIds) {
        //xml
        //而且要把資料從trash刪掉
    }

    @Override
    public void flushFolders(Long userId, List<Long> folderIds) {

    }

    @Override
    public void flushFiles(Long userId, List<Long> fileIds) {

    }
}
