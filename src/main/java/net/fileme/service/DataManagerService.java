package net.fileme.service;

import java.util.List;

public interface DataManagerService {

    List<Long> getTrashIds(Long userId);

    void relocate(Long parentId, List<Long> dataIds);
    // fileService.update

    void gotoTrash(Long parentId, List<Long> dataIds);
    // fileTrashMapper.insert()
    // fileService.update()

    void recover(List<Long> dataIds);

    void clearByIds(List<Long> dataIds);

    void clearAll(Long userId);

    void softDelete(List<Long> dataIds);

    // trashToRemoveList好像只有file需要??

}
