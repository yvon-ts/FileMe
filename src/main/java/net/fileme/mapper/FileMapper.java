package net.fileme.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.fileme.pojo.File;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileMapper extends BaseMapper<File> {
}
