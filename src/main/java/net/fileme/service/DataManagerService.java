package net.fileme.service;

import java.util.List;

public interface DataManagerService {

    void rename(Long dataId, String newName);

    List<Long> getTrashIds(Long userId);

    void relocate(Long parentId, List<Long> dataIds);

    void gotoTrash(List<Long> dataIds);

    void recover(List<Long> dataIds);

    void softDelete(List<Long> dataIds);

}
