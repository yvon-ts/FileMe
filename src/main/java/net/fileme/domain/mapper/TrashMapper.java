package net.fileme.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.fileme.domain.pojo.Trash;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TrashMapper extends BaseMapper<Trash> {
}
