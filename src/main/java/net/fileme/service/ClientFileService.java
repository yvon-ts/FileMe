package net.fileme.service;

import net.fileme.domain.Result;
import net.fileme.domain.pojo.File;
import org.springframework.web.multipart.MultipartFile;

public interface ClientFileService {
    File createFile(MultipartFile clientFile, String userId, String folderId, String accessLevel);
    Result<String> upload(MultipartFile clientFile, File file, boolean toRemote);
}
