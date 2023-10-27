package net.fileme.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.fileme.domain.pojo.FolderTrash;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FolderTrashMapper extends BaseMapper<FolderTrash> {
    int create(Long userId, List<Long> folderIds);
    int recover(Long userId, List<Long> folderIds);
}
