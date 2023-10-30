package net.fileme.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.fileme.domain.pojo.File;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService extends IService<File>, DataManagerService {
    String findPublicFilePath(Long fileId);

    String findPersonalFilePath(Long userId, Long fileId);

    File handlePartFile(MultipartFile multipartFile);
    void upload(MultipartFile multipartFile, File file);
    void createFile(MultipartFile multipartFile, Long userId, Long folderId);

    void rename(Long dataId, String newName, Long userId);

    void hardDelete(List<Long> fileIds);
}
