package net.fileme.service;

import net.fileme.domain.dto.DriveDto;
import net.fileme.domain.dto.FileFolderDto;
import org.springframework.http.ResponseEntity;

public interface DriveDtoService {
    ResponseEntity preview(Long userId, Long fileId);
    void rename(DriveDto dto);
    void relocate(Long destId, FileFolderDto dto);
    void gotoTrash(FileFolderDto dto);
    void recover(FileFolderDto dto);
    void clean(Long userId);
    void softDelete(Long userId, FileFolderDto dto);
}
