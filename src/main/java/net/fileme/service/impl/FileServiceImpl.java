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
import net.fileme.exception.NotFoundException;
import net.fileme.utils.enums.ExceptionEnum;
import net.fileme.service.FileService;
import net.fileme.utils.enums.MimeEnum;
import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

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
    public File handlePartFile(MultipartFile multipartFile) throws BizException {
        File file = new File();

        try {
            // handle mime type
            Tika tika = new Tika();
            byte[] bytes = multipartFile.getBytes();
            String mime = tika.detect(TikaInputStream.get(bytes));

            MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
            String tmpExt = allTypes.forName(mime).getExtension();

            String ext = tmpExt.substring(tmpExt.lastIndexOf(".") + 1);
            boolean isValidMime = ObjectUtils.containsConstant(MimeEnum.values(), ext);

            if(!isValidMime){
                throw new BizException(ExceptionEnum.FILE_TYPE_ERROR);
            }else{
                file.setExt(ext);
            }

            // handle file name
            String tmpFullName = multipartFile.getOriginalFilename();
            String fileName = tmpFullName.substring(0, tmpFullName.lastIndexOf("."));
            // 檔名卡控待補充，是否要正則表達式
            if (StringUtils.isBlank(fileName) || ".".equals(fileName)) {
                throw new BizException(ExceptionEnum.FILE_NAME_ERROR);
            }
            file.setFileName(fileName);

            // handle file size
            file.setSize(multipartFile.getSize());

        }catch(MimeTypeException e){
            throw new BizException(ExceptionEnum.FILE_TYPE_ERROR);
        }catch(IOException e){
            throw new BizException(ExceptionEnum.FILE_TYPE_ERROR);
        }
        return file;
    }

    @Override // 未來可能overloading增加toRemote的flag
    public void upload(MultipartFile part, File file) throws BizException{
        StringBuilder builder = new StringBuilder();
        builder.append(remotePathPrefix).append("/").append(file.getUserId()).append("/").append(file.getId()).append(".").append(file.getExt());
        String path = builder.toString();

        java.io.File tmpFile = new java.io.File(path);

        if(!tmpFile.getParentFile().exists()){
            tmpFile.getParentFile().mkdirs();
        }
        if(tmpFile.exists()){
            throw new BizException(ExceptionEnum.DUPLICATED_SVR);
        }
        try {
            part.transferTo(tmpFile);
        }catch(Exception e){
            throw new BizException(ExceptionEnum.UPLOAD_SVR_FAIL);
        }
    }

    @Override
    public void rename(Long dataId, String newName){
        LambdaUpdateWrapper<File> luw = new LambdaUpdateWrapper<>();
        luw.set(File::getFileName, newName).eq(File::getId, dataId);
        boolean success = update(luw);
        if(!success){
            throw new NotFoundException(ExceptionEnum.FILE_NOT_EXISTS);
        }
    }

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
        boolean success = update(luw);
        if(!success){
            throw new NotFoundException(ExceptionEnum.UPDATE_DB_FAIL);
        }
    }

    @Override
    public void gotoTrash(List<Long> dataIds) {
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
