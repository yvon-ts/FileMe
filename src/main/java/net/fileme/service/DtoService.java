package net.fileme.service;

import net.fileme.domain.DriveDto;
import net.fileme.domain.FileFolderDto;

public interface DtoService {
    void rename(DriveDto dto);
    void relocate(Long destId, FileFolderDto dto);
    void gotoTrash(FileFolderDto dto);
    void recover(FileFolderDto dto);

    void softDelete(Long userId, FileFolderDto dto);
}
