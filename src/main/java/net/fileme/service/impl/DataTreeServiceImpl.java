package net.fileme.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.fileme.domain.dto.DriveDto;
import net.fileme.domain.mapper.DriveDtoMapper;
import net.fileme.domain.mapper.FolderMapper;
import net.fileme.domain.pojo.File;
import net.fileme.enums.ExceptionEnum;
import net.fileme.exception.NotFoundException;
import net.fileme.service.DataTreeService;
import net.fileme.service.FileService;
import net.fileme.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
@PropertySource("classpath:credentials.properties")
public class DataTreeServiceImpl implements DataTreeService {

    @Value("${file.upload.dir}") // 名字可以再換
    private String remotePathPrefix;

    @Autowired
    private FolderService folderService;
    @Autowired
    private FileService fileService;
    @Autowired
    private FolderMapper folderMapper;

    @Autowired
    private DriveDtoMapper driveDtoMapper;

    @Override
    public List<DriveDto> getSuperFolderTree(Long userId, Long folderId){
        List<DriveDto> dtos = driveDtoMapper.getSuperFolderTree(userId, folderId);
        if(CollectionUtils.isEmpty(dtos)) throw new NotFoundException(ExceptionEnum.NO_SUCH_DATA);
        Collections.reverse(dtos);
        return dtos;
    }

    @Override
    public List<DriveDto> getSubFolders(Long userId, Long folderId){
        List<DriveDto> dtos = driveDtoMapper.getSubFolders(userId, folderId);
        if(CollectionUtils.isEmpty(dtos)) throw new NotFoundException(ExceptionEnum.NO_SUCH_DATA);
        return dtos;
    }
    // TODO: 重構以下
    // 考慮到根目錄=0, 目前都要傳userId
//    @Override
//    public List<Long> findSubFolderIds(Long userId, Long rootFolderId) {
//        LambdaQueryWrapper<Folder> lqw = new LambdaQueryWrapper<>();
//        lqw.select(Folder::getId)
//                .eq(Folder::getUserId, userId)
//                .eq(Folder::getParentId, rootFolderId);
//        List<String> tmpList = folderService.listObjs(lqw, Object::toString);
//        List<Long> subFolderIds = tmpList.stream().map(Long::valueOf).collect(Collectors.toList());
//        return subFolderIds;
//    }
//
//    @Override
//    public List<Long> findSubFileIds(Long userId, Long rootFolderId) {
//        LambdaQueryWrapper<File> lqw = new LambdaQueryWrapper<>();
//        lqw.select(File::getId)
//                .eq(File::getUserId, userId)
//                .eq(File::getFolderId, rootFolderId);
//        List<String> tmpList = fileService.listObjs(lqw, Object::toString);
//        List<Long> subFileIds = tmpList.stream().map(Long::valueOf).collect(Collectors.toList());
//        return subFileIds;
//    }
//
//    @Override
//    public FileFolderDto findSubIds(Long userId, Long rootFolderId) {
//        FileFolderDto dto = new FileFolderDto();
//        List<Long> subFolderIds = findSubFolderIds(userId, rootFolderId);
//        List<Long> subFileIds = findSubFileIds(userId, rootFolderId);
//        dto.setFolderIds(subFolderIds);
//        dto.setFileIds(subFileIds);
//        return dto;
//    }

//    @Override
//    public List<DriveDto> findTree(Long userId, Long folderId){
//        return driveDtoMapper.getSubTree(userId, folderId);
//    }

//    public FileFolderDto findTreeIds1(Long userId, Long rootFolderId) {
//        FileFolderDto dto = new FileFolderDto();
//        List<Long> listFolderIds = new ArrayList<>();
//        List<Long> listFileIds = new ArrayList<>();
//        List<Long> tmpList = new ArrayList<>();
//
//        tmpList.add(rootFolderId); // init
//        Iterator<Long> it = tmpList.iterator();
//
//        while(it.hasNext()){
//            Long tmpId = it.next();
//            it.remove();
//
//            // List<DriveDto>
//            // listFile直接++
//            // listFolder+進去後直接再放pending再找
//
//            List<Long> subFolderIds = findSubFolderIds(userId, tmpId);
//            listFolderIds.addAll(subFolderIds);
//
//            List<Long> subFileIds = findSubFileIds(userId, tmpId);
//            listFileIds.addAll(subFileIds);
//
//            // update loop condition
//            tmpList.addAll(subFolderIds);
//            it = tmpList.iterator();
//        }
//        dto.setFolderIds(listFolderIds);
//        dto.setFileIds(listFileIds);
//
//        return dto;
//    }



    @Override
    public List<File> findSubFiles(Long userId, Long rootFolderId) {
        LambdaQueryWrapper<File> lqw = new LambdaQueryWrapper<>();
        lqw.eq(File::getUserId, userId)
                .eq(File::getFolderId, rootFolderId);
        List<File> subFiles = fileService.list(lqw);
        return subFiles;
    }

    public String findFilePath(File file){
        StringBuilder builder = new StringBuilder();
        builder.append(remotePathPrefix).append("/").append(file.getUserId()).append("/").append(file.getId()).append(".").append(file.getExt());
        return builder.toString();
    }
    @Override
    public String findPublicFilePath(Long fileId){
        File file = fileService.getById(fileId);
        if(Objects.isNull(file)) return null;
        if(file.getAccessLevel() == 0) return null;

        return findFilePath(file);
    }
    @Override
    public String findPersonalFilePath(Long userId, Long fileId){
        File file = fileService.getById(fileId);
        if(Objects.isNull(file)) return null;
        if(!file.getUserId().equals(userId)) return null;

        return findFilePath(file);
    }
//    public List<Path> findBatchPath(Long userId, List<Long> fileIds){
//        List<String> strPaths = new ArrayList<>();
//        List<File> files = fileService.listByIds(fileIds);
//        files.forEach(file -> {
//            StringBuilder builder = new StringBuilder();
//            builder.append(remotePathPrefix).append("/").append(userId).append("/").append(file.getId()).append(".").append(file.getExt());
//            strPaths.add(builder.toString());
//        });
//        List<Path> paths = strPaths.stream().map(Paths::get).collect(Collectors.toList());
//        return paths;
//    }
}
