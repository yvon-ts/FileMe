package net.fileme.service;

public interface CheckExistService {
    int checkExistUser(Long userId);
    int checkExistFolder(Long userId, Long folderId);
    int checkExistFile(Long fileId);
}
