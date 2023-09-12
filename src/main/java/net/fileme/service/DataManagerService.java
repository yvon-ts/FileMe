package net.fileme.service;

import java.util.List;

public interface DataManagerService {

    List<Long> getTrashIds(Long userId);

    void relocate(Long parentId, List<Long> dataIds);

    void gotoTrash(Long parentId, List<Long> dataIds);

    void recover(List<Long> dataIds);

    void softDelete(List<Long> dataIds);

    // trashToRemoveList好像只有file需要??

}
