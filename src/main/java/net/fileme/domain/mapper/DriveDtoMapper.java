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
    List<DriveDto> getPublicSub(Long folderId);
    List<DriveDto> getSubFolders(Long userId, Long folderId);
    List<DriveDto> getSubTree(Long userId, Long folderId);

    // -------------------------get Super------------------------- //
    List<DriveDto> getSuperFolderTree(Long userId, Long folderId);

    // -------------------------specific handling------------------------- //
    DriveDto getOneFolder(Long userId, Long folderId); // validate
    List<Long> getDistinctParent(Long userId, List<Long> dataIds);


}
