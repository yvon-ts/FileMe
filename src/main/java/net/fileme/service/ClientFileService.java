package net.fileme.service;

import net.fileme.domain.Result;
import net.fileme.domain.pojo.File;
import net.fileme.domain.pojo.Folder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ClientFileService {
    File createFile(MultipartFile clientFile, Long userId, Long folderId, Integer accessLevel);
    Result<String> upload(MultipartFile clientFile, File file, boolean toRemote);
    boolean saveOrUpdateFolder(Folder folder);
    boolean relocateFolders(Long userId, Long parentId, List<Long> folderIds);
    boolean relocateFiles(Long userId, Long folderId, List<Long> fileIds);
    boolean relocateBatch(Long userId, Long destinationId, Map<String, List<Long>> dataIds);
}
