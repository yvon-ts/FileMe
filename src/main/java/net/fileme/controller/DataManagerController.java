package net.fileme.controller;

import net.fileme.domain.Result;
import net.fileme.domain.pojo.Folder;
import net.fileme.exception.BizException;
import net.fileme.exception.ExceptionEnum;
import net.fileme.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class DataManagerController {

    @Autowired
    private DataTreeService dataTreeService;
    @Autowired
    private FolderService folderService;
    @Autowired
    private FileService fileService;

    @GetMapping("/drive/relocate/super")
    public Result getRelocateSuper(@RequestParam Long folderId){
        List<Folder> superFolders = dataTreeService.findSuperFolders(folderId);
        return Result.success(superFolders);
    }

    @GetMapping("/drive/relocate/sub")
    public Result getRelocateSub(@RequestParam Long userId, @RequestParam Long folderId){
        List<Folder> subFolders = dataTreeService.findSubFolders(userId, folderId);
        return Result.success(subFolders);
    }

    @PostMapping("/drive/relocate")
    public void relocate(@RequestParam Long folderId, @RequestBody Map<String, List<Long>> map){
        List<Long> folderIds = map.get("folders");
        List<Long> fileIds = map.get("files");

        if(folderIds.isEmpty() && fileIds.isEmpty()){
            throw new BizException(ExceptionEnum.PARAM_ERROR);
        }

        folderService.relocate(folderId, folderIds);
        fileService.relocate(folderId, fileIds);
    }

    @PostMapping("/drive/clean") // 清空垃圾桶
    public void clean(Long userId){
        List<Long> folders = folderService.getTrashIds(userId);
        List<Long> files = fileService.getTrashIds(userId);
        Map<String, List<Long>> map = new HashMap<>();
        map.put("folders", folders);
        map.put("files", files);
        softDelete(userId, map);
    }

    /**
     * 不管從垃圾桶或主表點選刪除都一樣，trashDel不會報錯
     * 甚至系統排程掃描包好id也可直接呼叫
     * @param userId
     * @param map
     */
    @PostMapping("/drive/softDelete")
    public void softDelete(@RequestParam Long userId, @RequestBody Map<String, List<Long>> map){
        List<Long> foldersToDelete = map.get("folders");
        List<Long> filesToDelete = map.get("files");

        if(foldersToDelete.isEmpty() && filesToDelete.isEmpty()){
            throw new BizException(ExceptionEnum.PARAM_ERROR);
        }

        List<Long> tmpFolders = new ArrayList<>();
        List<Long> tmpFiles = new ArrayList<>();

        // handle sub data
        if(!foldersToDelete.isEmpty()){

            foldersToDelete.forEach(folder -> {
                Map<String, List<Long>> tmp = dataTreeService.findTreeIds(userId, folder);
                tmpFolders.addAll(tmp.get("subFolders"));
                tmpFiles.addAll(tmp.get("subFiles"));
            });

            foldersToDelete.addAll(tmpFolders);
            filesToDelete.addAll(tmpFiles);
            folderService.softDelete(foldersToDelete);
        }

        if(!filesToDelete.isEmpty()){
            fileService.softDelete(filesToDelete);
        }
    }

}
