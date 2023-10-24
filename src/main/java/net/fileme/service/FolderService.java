package net.fileme.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.fileme.domain.pojo.Folder;

public interface FolderService extends IService<Folder>, DataManagerService {
    void createFolder(Long userId, Long parentId, String name);
}
