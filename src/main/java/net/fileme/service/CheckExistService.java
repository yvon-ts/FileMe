package net.fileme.service;

public interface CheckExistService {
    int checkExistUser(Long userId);
    int checkExistFolder(Long folderId);
    int checkExistFile(Long fileId);
}
