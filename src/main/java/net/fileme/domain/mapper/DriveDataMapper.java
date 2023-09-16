package net.fileme.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.fileme.domain.pojo.DriveData;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DriveDataMapper extends BaseMapper<DriveData> {
    List<DriveData> getDriveData(Long userId, Long folderId);
}
