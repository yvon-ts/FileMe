package net.fileme.service.impl;

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
    public File createFile(MultipartFile clientFile) throws BizException{

        File file = new File();
        String tmpFullName = clientFile.getOriginalFilename();

        try {
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

            if (tmpSize <= 0) {
                throw new BizException(ExceptionEnum.FILE_SIZE_ERROR);
            }
            file.setSize(tmpSize);

        }catch(BizException bizException){
            throw bizException;
        }catch(Exception e){
            //這邊要做log紀錄真正的異常，前端看enum就可以
            throw new BizException(ExceptionEnum.UPLOAD_DB_FAIL);
        }
        return file;
    }

    @Override
    public String upload(MultipartFile clientFile
            , Long userId
            , Long fileId
            , boolean toRemote){

        if(!toRemote){
            String clientFileName = clientFile.getOriginalFilename();
            String ext = clientFileName.substring(clientFileName.lastIndexOf("."));
            java.io.File tmpFile = new java.io.File(uploadServerPath + "/" + userId + "/" + fileId + ext);
            if(!tmpFile.getParentFile().exists()){
                tmpFile.getParentFile().mkdirs();
            }
            if(tmpFile.exists()){
                //應該直接exception丟出來
                return "檔案已存在server"; // 之後要封裝 - errcode: server已有資料
            }
            try{
                clientFile.transferTo(tmpFile);
            }catch(Exception e){
                return "檔案寫入異常"; // 之後要封裝 - errcode: 檔案寫入異常
            }
            return "上傳成功";
        }
        return null;
    }
}
