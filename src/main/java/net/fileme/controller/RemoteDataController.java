package net.fileme.controller;

import net.fileme.domain.Result;
import net.fileme.domain.pojo.File;
import net.fileme.service.FileService;
import net.fileme.service.RemoteDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@RestController
public class RemoteDataController {
    @Autowired
    private FileService fileService;
    @Autowired
    private RemoteDataService remoteDataService;

    // ----------------------------------Create---------------------------------- //
    @PostMapping("/pub/drive/remote/file")
    public Result createRemoteFile(@RequestPart("file") @NotNull MultipartFile part){
        File file = fileService.handlePartFile(part);
        file.setUserId(1734590417358049281L);
        file.setFolderId(0L);
        fileService.save(file);
        remoteDataService.upload(part, file);
        return Result.success();
    }
    @GetMapping("/pub/drive/remote/download")
    public ResponseEntity<ByteArrayResource> download(@RequestParam String fileName){
        byte[] data = remoteDataService.getRemoteByteArray(fileName);
        ByteArrayResource byteArrayResource = new ByteArrayResource(data);
        return ResponseEntity.ok().contentLength(data.length)
                .header("Content-type", "application/octet-stream")
                .header("Content-disposition", "attachment; filename=\"" + fileName + "\"")
                .body(byteArrayResource);
    }
    @GetMapping("/pub/drive/remote/delete")
    public Result delete(@RequestParam String fileName){
        remoteDataService.delete(fileName);
        return Result.success();
    }
}
