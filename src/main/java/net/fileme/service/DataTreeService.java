package net.fileme.service;

import net.fileme.domain.dto.DriveDto;
import net.fileme.domain.pojo.File;

import java.util.List;

public interface DataTreeService {
    List<DriveDto> getSuperFolderTree(Long userId, Long folderId);

//    List<Long> findSubFolderIds(Long userId, Long rootFolderId);
//    List<Long> findSubFileIds(Long userId, Long rootFolderId);
//    FileFolderDto findSubIds(Long userId, Long rootFolderId);
//    List<DriveDto> findTree(Long userId, Long folderId);

    List<DriveDto> getSubFolders(Long userId, Long folderId);
    List<File> findSubFiles(Long userId, Long rootFolderId);
    String findPublicFilePath(Long fileId);

    String findPersonalFilePath(Long userId, Long fileId);
//    List<Path> findRemotePaths(Long userId, List<Long> fileIds);
}
