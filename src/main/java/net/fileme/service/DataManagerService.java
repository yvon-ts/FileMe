package net.fileme.service;

import java.util.List;

/**
 * shared methods for folders and files
 */
public interface DataManagerService {

    void accessControl(Long id, int newAccess);
    void rename(Long dataId, String newName, Long userId);

    void relocate(Long parentId, List<Long> dataIds, Long userId);

    void gotoTrash(Long userId, List<Long> dataIds);

    void recover(Long userId, List<Long> dataIds);

    void softDelete(Long userId, List<Long> dataIds);

}
