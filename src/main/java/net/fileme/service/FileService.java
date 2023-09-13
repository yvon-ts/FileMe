package net.fileme.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.fileme.domain.pojo.File;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService extends IService<File>, DataManagerService {
    File createFile(Long userId, String fileName, Long fileSize, Long folderId);
    void upload(MultipartFile multipartFile, File file);
    void hardDelete(List<Long> fileIds);
}
