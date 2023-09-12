package net.fileme.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.fileme.domain.mapper.FolderMapper;
import net.fileme.domain.mapper.FolderTrashMapper;
import net.fileme.domain.pojo.Folder;
import net.fileme.service.DataTreeService;
import net.fileme.service.FileService;
import net.fileme.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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
    private DataTreeService dataTreeService;
    @Override
    public List<Long> getTrashIds(Long userId) {
        return null;
    }

    @Override
    public void relocate(Long parentId, List<Long> dataIds) {
        LambdaUpdateWrapper<Folder> luw = new LambdaUpdateWrapper<>();
        luw.set(Folder::getParentId, parentId).in(Folder::getId, dataIds);
        update(luw);
    }

    @Override
    public void gotoTrash(Long parentId, List<Long> dataIds) {
        folderTrashMapper.create(dataIds);
        relocate(trashId, dataIds);
    }

    @Override
    public void recover(List<Long> dataIds) {
        folderTrashMapper.recover(dataIds);
        folderTrashMapper.deleteBatchIds(dataIds);
    }

    @Override
    public void softDelete(List<Long> dataIds) {
        folderTrashMapper.deleteBatchIds(dataIds);
        removeByIds(dataIds);
    }
}
