package net.fileme.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.fileme.domain.pojo.RemoveList;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RemoveListMapper extends BaseMapper<RemoveList> {
    void create(List<Long> fileIds);
}
