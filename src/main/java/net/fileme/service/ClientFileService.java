package net.fileme.service;

import net.fileme.domain.Result;
import net.fileme.domain.pojo.File;
import net.fileme.domain.pojo.Folder;
import org.springframework.web.multipart.MultipartFile;

public interface ClientFileService {
    File createFile(MultipartFile clientFile, Long userId, Long folderId, Integer accessLevel);
    Result<String> upload(MultipartFile clientFile, File file, boolean toRemote);
    Folder createFolder(Long userId, Long parentId, String folderName);
}
