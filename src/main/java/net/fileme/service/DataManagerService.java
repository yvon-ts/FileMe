package net.fileme.service;

import java.util.List;


public interface DataManagerService {
    void relocateFolders(Long parentId, List<Long> folderIds);
    void relocateFiles(Long folderId, List<Long> fileIds);
    void toTrashFolders(List<Long> folderIds);
    void toTrashFiles(List<Long> fileIds);
    void deleteFromTrash(Long userId, List<Long> dataIds, Integer dataType);
    void recoverFolders(Long userId, List<Long> folderIds);
    void recoverFiles(Long userId, List<Long> fileIds);
    // --------------------------------------------------
    void toRemoveFolders(List<Long> folderIds);
    void toRemoveFiles(List<Long> fileIds);
    void flushFiles(Long userId, List<Long> fileIds);

    // 立即刪除可能要放在別的service?
}
