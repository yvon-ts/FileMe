package net.fileme.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import net.fileme.domain.mapper.RemoveListMapper;
import net.fileme.domain.mapper.TrashMapper;
import net.fileme.domain.pojo.File;
import net.fileme.domain.pojo.Folder;
import net.fileme.domain.pojo.Trash;
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
    @Autowired
    private RemoveListMapper removeListMapper;

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
        trashMapper.insertFromFolders(folderIds);
        relocateFolders(trashId, folderIds);
    }

    @Override
    public void toTrashFiles(List<Long> fileIds) {
        trashMapper.insertFromFiles(fileIds);
        relocateFiles(trashId, fileIds);
    }

    /**
     *
     * @param userId
     * @param dataIds
     * @param dataType: 0 = folder, 1 = file
     */
    @Override
    public void deleteFromTrash(Long userId, List<Long> dataIds, Integer dataType) {
        LambdaQueryWrapper<Trash> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Trash::getUserId, userId).eq(Trash::getDataType, dataType).in(Trash::getDataId, dataIds);
        trashMapper.delete(lqw);
    }

    @Override
    public void recoverFolders(Long userId, List<Long> folderIds) {
        trashMapper.findOriginFolders(folderIds);
        deleteFromTrash(userId, folderIds, 0);
    }

    @Override
    public void recoverFiles(Long userId, List<Long> fileIds) {
        trashMapper.findOriginFiles(fileIds);
        deleteFromTrash(userId, fileIds, 1);
    }

    @Override
    public void toRemoveFolders(List<Long> folderIds) {
        //要先找DataTree
    }

    @Override
    public void toRemoveFiles(List<Long> fileIds) {
        removeListMapper.insertFromFiles(fileIds);
    }

    //模擬controller呼叫立即刪除files
    // (/drive/delete)
    public void foo(Long userId, List<Long> fileIds){
        toRemoveFiles(fileIds);
        fileService.removeByIds(fileIds);
        // if(currentFolder == 999) // 或是傳個flag以表示從垃圾桶刪除 // 或是有清除垃圾桶?
        deleteFromTrash(userId, fileIds, 1);
    }

    //從哪裡呼叫立即刪除?
    //user直接砍
    //user從垃圾桶砍
    //auto掃描垃圾桶砍

    // 立即刪除嗎
    @Override
    public void flushFiles(Long userId, List<Long> fileIds) {

    }
}
