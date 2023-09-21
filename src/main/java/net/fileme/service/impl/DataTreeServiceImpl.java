package net.fileme.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.fileme.domain.FileFolderDto;
import net.fileme.domain.mapper.FolderMapper;
import net.fileme.domain.pojo.File;
import net.fileme.domain.pojo.Folder;
import net.fileme.service.DataTreeService;
import net.fileme.service.FileService;
import net.fileme.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataTreeServiceImpl implements DataTreeService {

    @Value("${file.upload.dir}") // 名字可以再換
    private String remotePathPrefix;

    @Autowired
    private FolderService folderService;
    @Autowired
    private FileService fileService;
    @Autowired
    private FolderMapper folderMapper;

    @Override
    public List<Folder> findSuperFolders(Long folderId){
        List<Folder> superFolders = folderMapper.findSuperFolders(folderId);
        Collections.reverse(superFolders);
        return superFolders;
    }

    // 考慮到根目錄=0, 目前都要傳userId
    @Override
    public List<Long> findSubFolderIds(Long userId, Long rootFolderId) {
        LambdaQueryWrapper<Folder> lqw = new LambdaQueryWrapper<>();
        lqw.select(Folder::getId)
                .eq(Folder::getUserId, userId)
                .eq(Folder::getParentId, rootFolderId);
        List<String> tmpList = folderService.listObjs(lqw, Object::toString);
        List<Long> subFolderIds = tmpList.stream().map(Long::valueOf).collect(Collectors.toList());
        return subFolderIds;
    }

    @Override
    public List<Long> findSubFileIds(Long userId, Long rootFolderId) {
        LambdaQueryWrapper<File> lqw = new LambdaQueryWrapper<>();
        lqw.select(File::getId)
                .eq(File::getUserId, userId)
                .eq(File::getFolderId, rootFolderId);
        List<String> tmpList = fileService.listObjs(lqw, Object::toString);
        List<Long> subFileIds = tmpList.stream().map(Long::valueOf).collect(Collectors.toList());
        return subFileIds;
    }

    @Override
    public FileFolderDto findSubIds(Long userId, Long rootFolderId) {
        FileFolderDto dto = new FileFolderDto();
        List<Long> subFolderIds = findSubFolderIds(userId, rootFolderId);
        List<Long> subFileIds = findSubFileIds(userId, rootFolderId);
        dto.setFolderIds(subFolderIds);
        dto.setFileIds(subFileIds);
        return dto;
    }

    @Override
    public FileFolderDto findTreeIds(Long userId, Long rootFolderId) {
        FileFolderDto dto = new FileFolderDto();
        List<Long> listFolderIds = new ArrayList<>();
        List<Long> listFileIds = new ArrayList<>();
        List<Long> tmpList = new ArrayList<>();

        tmpList.add(rootFolderId); // init
        Iterator<Long> it = tmpList.iterator();

        while(it.hasNext()){
            Long tmpId = it.next();
            it.remove();

            List<Long> subFolderIds = findSubFolderIds(userId, tmpId);
            listFolderIds.addAll(subFolderIds);

            List<Long> subFileIds = findSubFileIds(userId, tmpId);
            listFileIds.addAll(subFileIds);

            // update loop condition
            tmpList.addAll(subFolderIds);
            it = tmpList.iterator();
        }
        dto.setFolderIds(listFolderIds);
        dto.setFileIds(listFileIds);

        return dto;
    }

    @Override
    public List<Folder> findSubFolders(Long userId, Long rootFolderId) {
        LambdaQueryWrapper<Folder> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Folder::getUserId, userId)
                .eq(Folder::getParentId, rootFolderId);
        List<Folder> subFolders = folderService.list(lqw);
        return subFolders;
    }

    @Override
    public List<File> findSubFiles(Long userId, Long rootFolderId) {
        LambdaQueryWrapper<File> lqw = new LambdaQueryWrapper<>();
        lqw.eq(File::getUserId, userId)
                .eq(File::getFolderId, rootFolderId);
        List<File> subFiles = fileService.list(lqw);
        return subFiles;
    }
    @Override
    public String findFilePath(Long userId, Long fileId){
        File file = fileService.getById(fileId);
        StringBuilder builder = new StringBuilder();
        builder.append(remotePathPrefix).append("/").append(userId).append("/").append(file.getId()).append(".").append(file.getExt());
        return builder.toString();
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
