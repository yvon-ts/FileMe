package net.fileme.service.impl;

import net.fileme.domain.Result;
import net.fileme.domain.pojo.File;

import net.fileme.domain.pojo.Folder;
import net.fileme.exception.BizException;
import net.fileme.exception.ExceptionEnum;
import net.fileme.service.CheckExistService;
import net.fileme.service.ClientFileService;
import net.fileme.service.FolderService;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ClientFileServiceImpl implements ClientFileService {

    @Value("${file.upload.dir}")
    private String uploadServerPath;

    @Autowired
    private CheckExistService checkExistService;
    @Autowired
    private FolderService folderService;
//
//    @Autowired
//    private FolderMapper folderMapper;

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

    @Override
    public Result<String> upload(MultipartFile clientFile, File file, boolean toRemote) throws BizException{

        if(!toRemote){
            java.io.File tmpFile = new java.io.File(uploadServerPath + "/" + file.getUserId() + "/" + file.getId() + "." + file.getExt());
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
}
