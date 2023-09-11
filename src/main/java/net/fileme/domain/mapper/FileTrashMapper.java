package net.fileme.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.fileme.domain.pojo.FileTrash;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FileTrashMapper extends BaseMapper<FileTrash> {
    void create(List<Long> fileIds);
    void recover(List<Long> fileIds);
}
