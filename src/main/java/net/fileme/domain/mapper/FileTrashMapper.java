package net.fileme.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.fileme.domain.pojo.FileTrash;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FileTrashMapper extends BaseMapper<FileTrash> {
    int create(Long userId, List<Long> fileIds);
    int recover(Long userId, List<Long> fileIds);
    int conflictCheckBeforeRecover(Long userId, Long fileId);
}
