package net.fileme.service;

import net.fileme.domain.dto.EmailDto;

public interface EmailService {
    boolean sendSimpleMail(EmailDto dto);
    String sendAttachedMail(EmailDto dto);
    void sendHtmlMail(EmailDto dto);
}