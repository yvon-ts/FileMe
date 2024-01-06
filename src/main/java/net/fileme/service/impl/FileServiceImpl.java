package net.fileme.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.fileme.domain.mapper.FileMapper;
import net.fileme.domain.mapper.FileTrashMapper;
import net.fileme.domain.mapper.RemoveListMapper;
import net.fileme.domain.pojo.File;
import net.fileme.domain.pojo.RemoveList;
import net.fileme.exception.*;
import net.fileme.enums.ExceptionEnum;
import net.fileme.service.FileService;
import net.fileme.enums.MimeEnum;
import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@PropertySource("classpath:credentials.properties")
public class FileServiceImpl extends ServiceImpl<FileMapper, File>
        implements FileService {

    @Value("${file.upload.dir}") // 名字可以再換
    private String remotePathPrefix;
    @Value("${file.trash.folderId}")
    private Long trashId;
    @Value("${regex.file.name}")
    private String regex;
    @Autowired
    private FileTrashMapper fileTrashMapper;
    @Autowired
    private RemoveListMapper removeListMapper;
    @Autowired
    private ApplicationContext applicationContext;

    public void checkFileName(String dataName){
        if(StringUtils.isBlank(dataName)) throw new BadRequestException(ExceptionEnum.PARAM_ERROR);
        if(!Pattern.matches(regex, dataName)) throw new BadRequestException(ExceptionEnum.FILE_NAME_REGEX_ERROR);
    }
    @Override
    public String findFilePath(File file){
        StringBuilder builder = new StringBuilder();
        builder.append(remotePathPrefix).append("/").append(file.getUserId()).append("/").append(file.getId()).append(".").append(file.getExt());
        return builder.toString();
    }

    @Override
    public String findRemoteFileName(File file){
        StringBuilder builder = new StringBuilder();
        builder.append(file.getUserId()).append("/").append(file.getId()).append(".").append(file.getExt());
        return builder.toString();
    }

    @Override
    public File findPersonalFile(Long userId, Long fileId){
        LambdaQueryWrapper<File> lqw = new LambdaQueryWrapper<>();
        lqw.eq(File::getId, fileId).eq(File::getUserId, userId);
        File file = getOne(lqw);
        if(Objects.isNull(file)) throw new NotFoundException(ExceptionEnum.NO_SUCH_DATA);
        return file;
    }
    @Override
    public File findPublicFile(Long fileId){
        LambdaQueryWrapper<File> lqw = new LambdaQueryWrapper<>();
        lqw.eq(File::getId, fileId).eq(File::getAccessLevel, 1).ne(File::getFolderId, trashId);
        File file = getOne(lqw);
        if(Objects.isNull(file)) throw new NotFoundException(ExceptionEnum.NO_SUCH_DATA);
        return file;
    }

    @Override
    public File handlePartFile(MultipartFile multipartFile){
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
                throw new BadRequestException(ExceptionEnum.FILE_TYPE_NOT_ALLOWED);
            }
            file.setExt(ext);

            // handle file name
            String tmpFullName = multipartFile.getOriginalFilename();
            String fileName = tmpFullName.substring(0, tmpFullName.lastIndexOf("."));

            checkFileName(fileName);

            file.setFileName(fileName);

            // handle file size
            file.setSize(multipartFile.getSize());

        }catch(MimeTypeException e){
            throw new BadRequestException(ExceptionEnum.FILE_TYPE_ERROR);
        }catch(IOException e){
            throw new InternalErrorException(ExceptionEnum.FILE_IO_ERROR);
        }
        return file;
    }

    @Override
    public void uploadLocal(MultipartFile part, File file){
        StringBuilder builder = new StringBuilder();
        builder.append(remotePathPrefix).append("/").append(file.getUserId()).append("/").append(file.getId()).append(".").append(file.getExt());
        String path = builder.toString();

        java.io.File tmpFile = new java.io.File(path);

        if(!tmpFile.getParentFile().exists()){
            tmpFile.getParentFile().mkdirs();
        }
        if(tmpFile.exists()){
            throw new InternalErrorException(ExceptionEnum.FILE_EXISTS_ON_SERVER);
        }
        try {
            part.transferTo(tmpFile);
        }catch(Exception e){
            throw new InternalErrorException(ExceptionEnum.UPLOAD_SVR_FAIL);
        }
    }

    @Override
    public void accessControl(Long dataId, int newAccess){
        LambdaUpdateWrapper<File> luw = new LambdaUpdateWrapper<>();
        luw.set(File::getAccessLevel, newAccess)
                .eq(File::getId, dataId);
        boolean success = update(luw);
        if(!success){
            throw new NotFoundException(ExceptionEnum.SET_FILE_ACCESS_FAIL);
        }
    }
    @Override
    public void rename(Long dataId, String newName, Long userId){
        LambdaUpdateWrapper<File> luw = new LambdaUpdateWrapper<>();
        luw.set(File::getFileName, newName)
                .eq(File::getId, dataId)
                .eq(File::getUserId, userId);
        boolean success = update(luw);
        if(!success){
            throw new NotFoundException(ExceptionEnum.FILE_RENAME_FAIL);
        }
    }

    @Override
    public void relocate(Long parentId, List<Long> dataIds, Long userId) {
        LambdaUpdateWrapper<File> luw = new LambdaUpdateWrapper<>();
        luw.set(File::getFolderId, parentId)
                .in(File::getId, dataIds)
                .eq(File::getUserId, userId);
        boolean success = update(luw);
        if(!success){
            throw new NotFoundException(ExceptionEnum.FILE_RELOCATE_FAIL);
        }
    }
    @Override
    @Transactional
    public void gotoTrash(Long userId, List<Long> dataIds) {
//        try{
//
//        }catch(DuplicateKeyException ex) {
//
//        }
        int successCreate = fileTrashMapper.create(userId, dataIds);
        if(successCreate == 0) throw new InternalErrorException(ExceptionEnum.CREATE_FILE_TRASH_FAIL);
        relocate(trashId, dataIds, userId);
    }

    @Override
    @Transactional
    public void recover(Long userId, List<Long> dataIds) {

        dataIds.forEach(dataId -> {
            int count = fileTrashMapper.conflictCheckBeforeRecover(userId, dataId);
            if(count > 0) throw new ConflictException(ExceptionEnum.RECOVERY_LOGIC_CONFLICT);
        });

        int successRecover = fileTrashMapper.recover(userId, dataIds);
        if(successRecover == 0) throw new InternalErrorException(ExceptionEnum.FILE_RECOVER_FAIL);
        int successDelete = fileTrashMapper.deleteBatchIds(dataIds);
        if(successDelete == 0) throw new InternalErrorException(ExceptionEnum.DELETE_FILE_TRASH_FAIL);
    }

    @Override
    @Transactional
    public void softDelete(Long userId, List<Long> fileIds){
        int successCreate = removeListMapper.create(fileIds);
        if(successCreate == 0) throw new InternalErrorException(ExceptionEnum.CREATE_REMOVE_LIST_FAIL);
        fileTrashMapper.deleteBatchIds(fileIds);
        LambdaUpdateWrapper<File> luw = new LambdaUpdateWrapper<>();
        luw.eq(File::getUserId, userId)
                .in(File::getId, fileIds);
        boolean success = remove(luw);
        if(!success) throw new InternalErrorException(ExceptionEnum.DELETE_REMOVE_LIST_FAIL);
    }
    @Override
    public void softDeleteByFileName(Long userId, String fileName){
        int dotIndex = fileName.indexOf(".");
        if(dotIndex == -1) throw new BadRequestException(ExceptionEnum.PARAM_ERROR);

        String prefix = fileName.substring(0, dotIndex);
        String suffix = fileName.substring(dotIndex + 1);

        LambdaQueryWrapper<File> lqw = new LambdaQueryWrapper<>();
        lqw.select(File::getId)
                .eq(File::getUserId, userId)
                .eq(File::getFolderId, trashId)
                .eq(File::getFileName, prefix)
                .eq(File::getExt, suffix);

        List<Long> fileIds = list(lqw)
                .stream()
                .map(file -> {
                    return file.getId();
                })
                .collect(Collectors.toList());

        softDelete(userId, fileIds);
    }

    @Override // 可以再評估一下PK要流水號 or FileID, 以下尚未考慮location
    @Transactional // 尚未測試
    public void hardDelete(List<Long> fileIds){
        // get remote path
        LambdaQueryWrapper<RemoveList> lqw = new LambdaQueryWrapper<>();
        lqw.select(RemoveList::getFilePath).in(RemoveList::getFileId, fileIds);
        List<Object> tmp = removeListMapper.selectObjs(lqw);
        if(tmp.isEmpty()){
            throw new BizException(ExceptionEnum.NO_SUCH_DATA);
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
                throw new BizException(ExceptionEnum.REMOVE_REMOVE_LIST_FAIL);
            }
        });
        // delete from db
        removeListMapper.deleteBatchIds(fileIds);
    }
}
