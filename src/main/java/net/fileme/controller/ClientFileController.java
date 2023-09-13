package net.fileme.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.fileme.domain.mapper.FileMapper;
import net.fileme.domain.mapper.FolderMapper;
import net.fileme.domain.pojo.File;
import net.fileme.domain.pojo.Folder;

import net.fileme.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@Validated
@RestController
public class ClientFileController {
    @Autowired
    private CheckExistService checkExistService;
    @Autowired
    private DataTreeService dataTreeService;
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private FolderMapper folderMapper;
    @Autowired
    private FolderService folderService;
    @Autowired
    private FileService fileService;


    @PostMapping("drive/display/folder")
    public List<Folder> displayFolder(@RequestParam Long userId, @RequestParam("currentFolderId") Long parentId){
        LambdaQueryWrapper<Folder> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Folder::getUserId, userId).eq(Folder::getParentId, parentId);
        List<Folder> folders = folderService.list(lqw);
        System.out.println(folders);
        return folders;
    }
    @PostMapping("drive/display/file")
    public List<File> displayFile(@RequestParam Long userId, @RequestParam Long folderId){
        LambdaQueryWrapper<File> lqw = new LambdaQueryWrapper<>();
        lqw.eq(File::getUserId, userId).eq(File::getFolderId, folderId);
        List<File> files = fileService.list(lqw);
        return files;
    }

}
