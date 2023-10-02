package net.fileme.service;

import net.fileme.domain.EmailDetails;

public interface EmailService {
    boolean sendSimpleMail(EmailDetails details);
    String sendAttachedMail(EmailDetails details);
    void sendHtmlMail(EmailDetails details);
}