package net.fileme.service;

import net.fileme.domain.EmailDto;

public interface EmailService {
    boolean sendSimpleMail(EmailDto dto);
    String sendAttachedMail(EmailDto dto);
    void sendHtmlMail(EmailDto dto);
}