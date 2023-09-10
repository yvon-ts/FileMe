package net.fileme.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.fileme.domain.pojo.Trash;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TrashMapper extends BaseMapper<Trash> {
    void insertFromFolders(@Param("folderIds") List<Long> folderIds);
    void insertFromFiles(@Param("fileIds") List<Long> fileIds);
    void findOriginFolders(@Param("folderIds") List<Long> folderIds);
    void findOriginFiles(@Param("fileIds") List<Long> fileIds);
}
