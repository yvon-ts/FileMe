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
    // ----------------------------------------------------------------------------
    void softDelFolders(Long userId, List<Long> folderIds);
    void softDelFiles(Long userId, List<Long> fileIds);
    void softDelBatch(Long userId, Map<String, List<Long>> dataIds);
    void hardDelFolder(List<Long> folderIds);
    void hardDelFile(List<Long> fileIds);
//    boolean hardDelFiles(Long userId, List<Long> fileIds);
    void hardDelRemoteFiles(Long userId, List<Long> fileIds);
    void relocateFolders(Long userId, Long parentId, List<Long> folderIds);
    void relocateFiles(Long userId, Long folderId, List<Long> fileIds);
    void relocateBatch(Long userId, Long destinationId, Map<String, List<Long>> dataIds);
}
