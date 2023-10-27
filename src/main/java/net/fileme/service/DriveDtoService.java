package net.fileme.service;

import net.fileme.domain.Result;
import net.fileme.domain.dto.DriveDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface DriveDtoService {

    // ----------------------------------Read---------------------------------- //
    Result publicData(Long folderId);
    Result getSub(Long userId, Long folderId);
    ResponseEntity previewPublic(Long fileId);
    ResponseEntity previewPersonal(Long userId, Long fileId);

    // ----------------------------------Update---------------------------------- //
    void rename(DriveDto dto, Long userId);
    void relocate(Long destId, List<DriveDto> listDto, Long userId);

    // ----------------------------------Delete: clean & recover---------------------------------- //
    void gotoTrash(Long userId, List<DriveDto> listDto);
    void recover(Long userId, List<DriveDto> listDto);
    void clean(Long userId);
    void softDelete(Long userId, List<DriveDto> listDto);

    // ----------------------------------specific handling---------------------------------- //
    boolean sameParentCheck(Long userId, List<DriveDto> list);
}
