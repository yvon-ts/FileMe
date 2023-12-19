package net.fileme.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.fileme.domain.dto.DriveDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DriveDtoMapper extends BaseMapper<DriveDto> {

    // -------------------------get All------------------------- //
    List<DriveDto> getAll(Long userId);
    List<DriveDto> getAllFolders(Long userId);
    List<DriveDto> getAllFiles(Long userId);

    // -------------------------get Sub------------------------- //
    List<DriveDto> getSub(Long userId, Long folderId);
    List<DriveDto> getSubIds(Long userId, Long folderId);
    List<DriveDto> getSubFolders(Long userId, Long folderId);
    List<DriveDto> getSubFiles(Long userId, Long folderId);
    List<DriveDto> getSubTree(Long userId, Long folderId);

    // -------------------------get Super------------------------- //
    List<DriveDto> getSuperFolderTree(Long userId, Long folderId);

    // -------------------------get One------------------------- //
    DriveDto getOneData(Long userId, Long dataId);
    DriveDto getOneFolder(Long userId, Long folderId);
    DriveDto getOneFile(Long userId, Long fileId);

    // -------------------------get Public------------------------- //
    DriveDto getPublicFolder(Long folderId);
    DriveDto getPublicFile(Long fileId);
    List<DriveDto> getPublicSub(Long folderId);

    // -------------------------specific handling------------------------- //
    List<Long> getDistinctParent(Long userId, List<Long> dataIds);
    List<DriveDto> getConflictedTrash(Long userId, List<String> folders, List<String> files);
    List<DriveDto> search(Long userId, String keyword1, String keyword2);

}
