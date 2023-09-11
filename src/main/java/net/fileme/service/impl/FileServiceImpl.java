package net.fileme.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.fileme.domain.mapper.FileMapper;
import net.fileme.domain.mapper.FileTrashMapper;
import net.fileme.domain.mapper.RemoveListMapper;
import net.fileme.domain.pojo.File;
import net.fileme.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File>
        implements FileService {

    @Value("${file.trash.folderId}")
    private Long trashId;
    @Autowired
    private FileTrashMapper fileTrashMapper;
    @Autowired
    private RemoveListMapper removeListMapper;

    @Override
    public List<Long> getTrashIds(Long userId) {
        LambdaQueryWrapper<File> lqw = new LambdaQueryWrapper<>();
        lqw.select(File::getId).eq(File::getUserId, userId).eq(File::getFolderId, trashId);
        List<String> tmp = listObjs(lqw, Object::toString);
        List<Long> trashIds = tmp.stream().map(Long::valueOf).collect(Collectors.toList());
        return trashIds;
    }

    @Override
    public void relocate(Long parentId, List<Long> dataIds) {
        LambdaUpdateWrapper<File> luw = new LambdaUpdateWrapper<>();
        luw.set(File::getFolderId, parentId).in(File::getId, dataIds);
        update(luw);
    }

    @Override
    public void gotoTrash(Long parentId, List<Long> dataIds) {
        fileTrashMapper.create(dataIds);
        relocate(trashId, dataIds);
    }

    @Override
    public void recover(List<Long> dataIds) {
        fileTrashMapper.recover(dataIds);
        fileTrashMapper.deleteBatchIds(dataIds);
    }

    @Override // 從垃圾桶指定刪除
    public void clearByIds(List<Long> dataIds) {
        removeListMapper.create(dataIds);
        fileTrashMapper.deleteBatchIds(dataIds);
        removeByIds(dataIds);
    }

    @Override // 清空垃圾桶
    public void clearAll(Long userId) {
        List<Long> trashIds = getTrashIds(userId);
        clearByIds(trashIds);
    }

    @Override // 不經過垃圾桶直接刪除
    public void softDelete(List<Long> dataIds){
        removeListMapper.create(dataIds);
        removeByIds(dataIds);
    }
}
