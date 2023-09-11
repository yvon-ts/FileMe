package net.fileme.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.fileme.domain.mapper.FolderMapper;
import net.fileme.domain.pojo.Folder;
import net.fileme.service.FolderService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FolderServiceImpl extends ServiceImpl<FolderMapper, Folder>
        implements FolderService {
    @Override
    public List<Long> getTrashIds(Long userId) {
        return null;
    }

    @Override
    public void relocate(Long parentId, List<Long> dataIds) {

    }

    @Override
    public void gotoTrash(Long parentId, List<Long> dataIds) {

    }

    @Override
    public void recover(List<Long> dataIds) {

    }

    @Override
    public void clearByIds(List<Long> dataIds) {

    }

    @Override
    public void clearAll(Long userId) {

    }

    @Override
    public void softDelete(List<Long> dataIds) {

    }
}
