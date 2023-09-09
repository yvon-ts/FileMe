package net.fileme.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import net.fileme.domain.Result;
import net.fileme.domain.pojo.File;

import net.fileme.domain.pojo.Folder;
import net.fileme.exception.BizException;
import net.fileme.exception.ExceptionEnum;
import net.fileme.service.*;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Service
public class ClientFileServiceImpl implements ClientFileService {

    @Value("${file.upload.dir}") // 名字可以再換
    private String remotePathPrefix;
    @Value("${file.trash.folderId}")
    private Long softDelId;

    @Autowired
    private DataTreeService dataTreeService;
    @Autowired
    private CheckExistService checkExistService;
    @Autowired
    private FolderService folderService;
    @Autowired
    private FileService fileService;

    @Override
    public File createFile(MultipartFile clientFile, Long userId, Long folderId, Integer accessLevel) throws BizException{

        File file = new File();
        String tmpFullName = clientFile.getOriginalFilename();

        try {
            // userId有FK擋，folderId不存在時視為在根目錄
            file.setUserId(userId);
            file.setFolderId(folderId);
            file.setAccessLevel(accessLevel);

            String tmpName = tmpFullName.substring(0, tmpFullName.lastIndexOf("."));
            // 檔名卡控待補充，是否要正則表達式
            if (StringUtils.isBlank(tmpName) || ".".equals(tmpName)) {
                throw new BizException(ExceptionEnum.FILE_NAME_ERROR);
            }

            String tmpExt = tmpFullName.substring(tmpFullName.lastIndexOf(".") + 1);

            if (StringUtils.isBlank(tmpExt) || tmpExt.length() <= 1) {
                throw new BizException(ExceptionEnum.FILE_NAME_ERROR);
            }

            file.setFileName(tmpName);
            file.setExt(tmpExt);

            long tmpSize = clientFile.getSize();
            // 檔案太大也要擋
            if (tmpSize <= 0) {
                throw new BizException(ExceptionEnum.FILE_SIZE_ERROR);
            }
            file.setSize(tmpSize);


        }catch(BizException bizException) {
            throw bizException;
        }catch(Exception e){
            //這邊要做log紀錄真正的異常，前端看enum就可以
            throw new BizException(ExceptionEnum.UPLOAD_DB_FAIL);
        }
        return file;
    }

    @Override //toRemote可能要改int
    public Result<String> upload(MultipartFile clientFile, File file, boolean toRemote) throws BizException{

        if(!toRemote){
            java.io.File tmpFile = new java.io.File(remotePathPrefix + "/" + file.getUserId() + "/" + file.getId() + "." + file.getExt());
            if(!tmpFile.getParentFile().exists()){
                tmpFile.getParentFile().mkdirs();
            }
            if(tmpFile.exists()){
                throw new BizException(ExceptionEnum.DUPLICATED_SVR);
            }
            try {
                clientFile.transferTo(tmpFile);
            }catch(BizException bizException){
                throw bizException;
            }catch(Exception e){
                throw new BizException(ExceptionEnum.UPLOAD_SVR_FAIL);
            }
            return Result.success();
        }
        return Result.error(ExceptionEnum.UPLOAD_SVR_FAIL);
    }

    @Override
    public boolean saveOrUpdateFolder(Folder folder){
        // check to prevent nested structure
        if(folder.getId() != null){
            if(folder.getId().equals(folder.getParentId())){
                throw new BizException(ExceptionEnum.PARAM_ERROR);
            }
        }

        Long tmpUserId = folder.getUserId();
        Long tmpParentId = folder.getParentId();

        // parentId should be either 0 or existing folderId (with same userId)
        if(tmpParentId != 0){
            int check = checkExistService.checkExistFolder(tmpUserId, tmpParentId);
            if(check != 1){
                throw new BizException(ExceptionEnum.PARAM_ERROR);
            }else{
                return folderService.saveOrUpdate(folder);
            }
        }else{
            return folderService.saveOrUpdate(folder);
        }
    }

    public void hardDelEntrance(Long userId, Long folderId){ // 先單個，在controller處理0不能進來hardDel
        if(folderId.equals(0L)){
            return; //這邊看怎麼報錯
        }
        //先找子folder
        Map<String, List<Long>> tree = dataTreeService.findTreeIds(userId, folderId);
        List<Long> subFolders = tree.get("subFolders");
        List<Long> subFiles = tree.get("subFiles");

        //先刪remote確認之後才刪db
        hardDelRemoteFiles(userId, subFiles);

        //是否要先刪file避免孤兒file
        hardDelFile(subFiles);

        //是否要確認File底下沒有@folderIds的資料才能刪除folder

        //刪除會員才要刪remote folder
    }
    @Override
    public void hardDelFolder(List<Long> folderIds) throws BizException{
        LambdaQueryWrapper<File> lqw = new LambdaQueryWrapper<>();
        lqw.in(File::getFolderId, folderIds);
        List<File> list = fileService.list(lqw);
        if(!list.isEmpty()){
            throw new BizException(ExceptionEnum.FOLDER_NOT_EMPTY);
        }else{
            folderService.removeByIds(folderIds);
        }
    }
    @Override
    public void hardDelFile(List<Long> fileIds){
        fileService.removeByIds(fileIds);
    }
    @Override
    public void hardDelRemoteFiles(Long userId, List<Long> fileIds) throws BizException{
        List<Path> paths = dataTreeService.findRemotePaths(userId, fileIds);
        System.out.println(paths);
            paths.forEach(path -> {
                try {
                    boolean success = FileSystemUtils.deleteRecursively(path);
                    // 先不處理
//                    if(!success){
//                        throw new BizException(ExceptionEnum.DATA_NOT_EXISTS);
//                    }
                } catch (IOException e) {
                    throw new BizException(ExceptionEnum.DATA_DELETE_FAIL);
                }
            });
    }
    public void hardDelRemoteFolder(){};    //刪除會員才會用到

    @Override
    public void softDelFolders(Long userId, List<Long> folderIds){
        relocateFolders(userId, softDelId, folderIds);
    }

    @Override
    public void softDelFiles(Long userId, List<Long> fileIds) {
        relocateFiles(userId, softDelId ,fileIds);
    }

    @Override
    public void softDelBatch(Long userId, Map<String, List<Long>> dataIds) {
        List<Long> folderIds = dataIds.get("folderIds");
        List<Long> fileIds = dataIds.get("fileIds");
        relocateFolders(userId, softDelId, folderIds);
        relocateFiles(userId, softDelId, fileIds);
    }

    @Override
    public void relocateFolders(Long userId, Long parentId, List<Long> folderIds) {
        folderIds.forEach(folderId -> {
            LambdaUpdateWrapper<Folder> luw = new LambdaUpdateWrapper<>();
            luw.set(Folder::getParentId, parentId).eq(Folder::getUserId, userId).eq(Folder::getId, folderId);
            // 這邊看怎樣加transaction
            folderService.update(luw);
        });
    }
    @Override
    public void relocateFiles(Long userId, Long folderId, List<Long> fileIds) {
        fileIds.forEach(fileId -> {
            LambdaUpdateWrapper<File> luw = new LambdaUpdateWrapper<>();
            luw.set(File::getFolderId, folderId).eq(File::getUserId, userId).eq(File::getId, fileId);
            // 這邊看怎樣加transaction
            fileService.update(luw);
        });
    }
    @Override
    public void relocateBatch(Long userId, Long destinationId, Map<String, List<Long>> dataIds) {
        List<Long> folderIds = dataIds.get("folderIds");
        List<Long> fileIds = dataIds.get("fileIds");
        relocateFolders(userId, destinationId, folderIds);
        relocateFiles(userId, destinationId, fileIds);
    }
}
