package net.fileme.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.fileme.domain.dto.DriveDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DriveDtoMapper extends BaseMapper<DriveDto> {
    List<DriveDto> getDriveDto(Long userId, Long folderId);
}
