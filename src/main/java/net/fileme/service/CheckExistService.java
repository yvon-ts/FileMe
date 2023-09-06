package net.fileme.service;

import java.util.Map;

public interface CheckExistService {
    int checkExistUser(Long userId);
    int checkExistFolder(Long userId, Long folderId);
    int checkExistFile(Long fileId);
    int checkSubFolder(Long userId, Long folderId);
    int checkSubFile(Long userId, Long folderId);
    Map<String, Integer> checkContents(Long userId, Long folderId);
}
