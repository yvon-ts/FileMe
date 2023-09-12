package net.fileme.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.fileme.domain.mapper.FileMapper;
import net.fileme.domain.mapper.FileTrashMapper;
import net.fileme.domain.mapper.RemoveListMapper;
import net.fileme.domain.pojo.File;
import net.fileme.domain.pojo.RemoveList;
import net.fileme.exception.BizException;
import net.fileme.exception.ExceptionEnum;
import net.fileme.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File>
        implements FileService {

    @Value("${file.upload.dir}") // 名字可以再換
    private String remotePathPrefix;
    @Value("${file.trash.folderId}")
    private Long trashId;
    @Autowired
    private FileTrashMapper fileTrashMapper;
    @Autowired
    private RemoveListMapper removeListMapper;

    @Override
    public List<Long> getTrashIds(Long userId) {
        LambdaQueryWrapper<File> lqw = new LambdaQueryWrapper<>();
        lqw.select(File::getId).eq(File::getUserId, userId).eq(File::getFolderId, trashId);
        List<String> tmp = listObjs(lqw, Object::toString);
        List<Long> trashIds = tmp.stream().map(Long::valueOf).collect(Collectors.toList());
        return trashIds;
    }

    @Override
    public void relocate(Long parentId, List<Long> dataIds) {
        LambdaUpdateWrapper<File> luw = new LambdaUpdateWrapper<>();
        luw.set(File::getFolderId, parentId).in(File::getId, dataIds);
        update(luw);
    }

    @Override
    public void gotoTrash(Long parentId, List<Long> dataIds) {
        fileTrashMapper.create(dataIds);
        relocate(trashId, dataIds);
    }

    @Override
    public void recover(List<Long> dataIds) {
        fileTrashMapper.recover(dataIds);
        fileTrashMapper.deleteBatchIds(dataIds);
    }

    @Override
    public void softDelete(List<Long> dataIds){
        removeListMapper.create(dataIds);
        fileTrashMapper.deleteBatchIds(dataIds);
        removeByIds(dataIds);
    }
    @Override // 可以再評估一下PK要流水號 or FileID, 以下尚未考慮location
    public void hardDelete(List<Long> fileIds) throws BizException{
        // get remote path
        LambdaQueryWrapper<RemoveList> lqw = new LambdaQueryWrapper<>();
        lqw.select(RemoveList::getFilePath).in(RemoveList::getFileId, fileIds);
        List<Object> tmp = removeListMapper.selectObjs(lqw);
        if(tmp.isEmpty()){
            throw new BizException(ExceptionEnum.DATA_NOT_EXISTS);
        }
        List<String> strPaths = tmp.stream().map(Objects::toString).collect(Collectors.toList());

        // loop to delete remote file
        strPaths.forEach(strPath -> {
            try {
                FileSystemUtils.deleteRecursively(Paths.get(remotePathPrefix + strPath));
            } catch (IOException e) {
                LambdaUpdateWrapper<RemoveList> luw = new LambdaUpdateWrapper<>();
                luw.set(RemoveList::getState, 1).in(RemoveList::getFileId, fileIds);
                removeListMapper.update(null, luw);
                throw new BizException(ExceptionEnum.DATA_DELETE_FAIL);
            }
        });
        // delete from db
        removeListMapper.deleteBatchIds(fileIds);
    }
}
