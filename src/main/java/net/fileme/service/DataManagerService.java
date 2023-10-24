package net.fileme.service;

import java.util.List;

public interface DataManagerService {

    void rename(Long dataId, String newName, Long userId);

    List<Long> getTrashIds(Long userId);

    void relocate(Long parentId, List<Long> dataIds, Long userId);

    void gotoTrash(List<Long> dataIds);

    void recover(List<Long> dataIds);

    void softDelete(List<Long> dataIds);

}
