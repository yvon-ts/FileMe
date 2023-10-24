package net.fileme.service;

import net.fileme.domain.Result;
import net.fileme.domain.dto.DriveDto;
import net.fileme.domain.dto.FileFolderDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface DriveDtoService {
    Result publicData(Long folderId);

    Result getData(Long userId, Long folderId);

    ResponseEntity previewPublic(Long fileId);
    ResponseEntity previewPersonal(Long userId, Long fileId);
    void rename(DriveDto dto, Long userId);
    void relocate(Long destId, List<DriveDto> listDto, Long userId);
    void gotoTrash(FileFolderDto dto);
    void recover(FileFolderDto dto);
    void clean(Long userId);
    void softDelete(Long userId, FileFolderDto dto);
}
