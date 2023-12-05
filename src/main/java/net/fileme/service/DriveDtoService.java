package net.fileme.service;

import net.fileme.domain.Result;
import net.fileme.domain.dto.DriveDto;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface DriveDtoService {

    // ----------------------------------Create---------------------------------- //
    void createFile(MultipartFile part, Long userId, Long folderId);

    // ----------------------------------Read---------------------------------- //
    Result getPublicFolder(Long folderId);
    Result getPublicSub(Long folderId);
    Result getSub(Long userId, Long folderId);
    ResponseEntity previewPublic(Long fileId);
    ResponseEntity previewPersonal(Long userId, Long fileId);
    ResponseEntity<ByteArrayResource> downloadPublic(Long fileId, HttpServletResponse response);
    ResponseEntity<ByteArrayResource> downloadPersonal(Long userId, Long fileId, HttpServletResponse response);

    // ----------------------------------Update---------------------------------- //
    void accessControl(DriveDto dto, Long userId);
    void rename(DriveDto dto, Long userId);
    void relocate(Long destId, List<DriveDto> listDto, Long userId);

    List<DriveDto> getSuperFolderTree(Long userId, Long folderId);

    List<DriveDto> getSubFolders(Long userId, Long folderId);

    // ----------------------------------Delete: clean & recover---------------------------------- //
    void gotoTrash(Long userId, List<DriveDto> listDto);
    void recover(Long userId, List<DriveDto> listDto);
    void clean(Long userId);
    void softDelete(Long userId, List<DriveDto> listDto);

    // ----------------------------------specific handling---------------------------------- //
    boolean sameParentCheck(Long userId, List<DriveDto> list);
    @Transactional
    void conflictTrash(Long userId, List<DriveDto> listDto);
}
