package net.fileme.service;

import net.fileme.domain.DriveDto;
import net.fileme.domain.FileFolderDto;
import org.springframework.http.ResponseEntity;

public interface DtoService {
    ResponseEntity preview(Long userId, Long fileId);
    void rename(DriveDto dto);
    void relocate(Long destId, FileFolderDto dto);
    void gotoTrash(FileFolderDto dto);
    void recover(FileFolderDto dto);
    void clean(Long userId);
    void softDelete(Long userId, FileFolderDto dto);
}
