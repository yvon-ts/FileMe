package net.fileme.service.impl;

import net.fileme.domain.Result;
import net.fileme.domain.pojo.File;

import net.fileme.exception.BizException;
import net.fileme.exception.ExceptionEnum;
import net.fileme.service.ClientFileService;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ClientFileServiceImpl implements ClientFileService {

    @Value("${file.upload.dir}")
    private String uploadServerPath;

    @Override
    public File createFile(MultipartFile clientFile, String strUserId, String strFolderId, String strAccessLevel) throws BizException{

        File file = new File();
        String tmpFullName = clientFile.getOriginalFilename();

        try {

            // 是否應檢查userId, folderId是否存在？
            Long userId = (long) Integer.parseInt(strUserId);
            Long folderId = (long) Integer.parseInt(strFolderId);
            Integer accessLevel = Integer.parseInt(strAccessLevel);
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
        }catch(IllegalArgumentException e){
            throw new BizException(ExceptionEnum.PARAM_ERROR);
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
}
