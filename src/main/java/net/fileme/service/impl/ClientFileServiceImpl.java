package net.fileme.service.impl;

import net.fileme.domain.Result;
import net.fileme.domain.pojo.File;

import net.fileme.domain.pojo.Folder;
import net.fileme.exception.BizException;
import net.fileme.exception.ExceptionEnum;
import net.fileme.service.CheckExistService;
import net.fileme.service.ClientFileService;
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
            return Result.success("upload completed.");
        }
        return Result.error(ExceptionEnum.UPLOAD_SVR_FAIL);
    }

    @Override
    public Folder createFolder(Long userId, Long parentId, String folderName){
        // parentId = 0 means at root folder
        if(parentId != 0){
            int check = checkExistService.checkExistFolder(parentId);
            if(check != 1){
                throw new BizException(ExceptionEnum.PARAM_ERROR);
            }
        }

        //需要過濾folderName?
        if(".".equals(folderName)){
            throw new BizException(ExceptionEnum.FOLDER_NAME_ERROR);
        }

        Folder folder = new Folder();
        folder.setUserId(userId);
        folder.setFolderName(folderName);
        folder.setParentId(parentId);
        return folder;
    }
}
