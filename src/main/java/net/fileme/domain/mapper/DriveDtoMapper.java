package net.fileme.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.fileme.domain.dto.DriveDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DriveDtoMapper extends BaseMapper<DriveDto> {
    List<DriveDto> getAll(Long userId);
    List<DriveDto> getAllFolders(Long userId);
    List<DriveDto> getAllFiles(Long userId);

    // ----------- 要改 ----------- //
    List<DriveDto> getPublicData(Long folderId);
    List<DriveDto> getData(Long userId, Long folderId);

    // ----------- 要改 ----------- //
    DriveDto getFolderDto(Long userId, Long folderId); // validate
    List<DriveDto> findSuperFolderDtos(Long userId, Long folderId);
    List<DriveDto> findSubFolderDtos(Long userId, Long folderId);
}
