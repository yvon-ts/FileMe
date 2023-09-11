package net.fileme.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.fileme.domain.pojo.Folder;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FolderTrashMapper extends BaseMapper<Folder> {
    void create(List<Long> folderIds);
}
