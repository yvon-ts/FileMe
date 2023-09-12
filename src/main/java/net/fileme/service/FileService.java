package net.fileme.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.fileme.domain.pojo.File;

import java.util.List;

public interface FileService extends IService<File>, DataManagerService {
    void hardDelete(List<Long> fileIds);
}
