package net.fileme.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.fileme.domain.pojo.File;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService extends IService<File>, DataManagerService {


    String findFilePath(File file);
    File findPublicFile(Long fileId);
    File findPersonalFile(Long userId, Long fileId);

    String findRemoteFileName(File file);
    File handlePartFile(MultipartFile multipartFile);
    void upload(MultipartFile multipartFile, File file);
    void rename(Long dataId, String newName, Long userId);

    void softDeleteByFileName(Long userId, String fileName);

    void hardDelete(List<Long> fileIds);
}
