package net.fileme.service;

import net.fileme.domain.pojo.File;
import net.fileme.domain.pojo.Folder;


import java.util.List;
import java.util.Map;

public interface DataTreeService {
    List<Folder> findSuperFolders(Long folderId);
    List<Long> findSubFolderIds(Long userId, Long rootFolderId);
    List<Long> findSubFileIds(Long userId, Long rootFolderId);
    Map<String, List<Long>> findSubIds(Long userId, Long rootFolderId);
    Map<String, List<Long>> findTreeIds(Long userId, Long rootFolderId);
    List<Folder> findSubFolders(Long userId, Long rootFolderId);
    List<File> findSubFiles(Long userId, Long rootFolderId);
    String findFilePath(Long userId, Long fileId);
//    List<Path> findRemotePaths(Long userId, List<Long> fileIds);
}
