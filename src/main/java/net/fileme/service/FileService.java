package net.fileme.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.fileme.domain.pojo.File;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService extends IService<File>, DataManagerService {
    File handlePartFile(MultipartFile multipartFile);
    void upload(MultipartFile multipartFile, File file);
    void createFile(MultipartFile multipartFile, Long userId, Long folderId);
    void hardDelete(List<Long> fileIds);
}
