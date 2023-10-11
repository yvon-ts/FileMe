package net.fileme.service.impl;

import net.fileme.domain.dto.EmailDto;
import net.fileme.enums.ExceptionEnum;
import net.fileme.exception.BizException;
import net.fileme.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender javaMailSender;
    @Value("${mail.sender}")
    private String sender;
    @Override
    public boolean sendSimpleMail(EmailDto dto){
        try{
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(sender);
            mailMessage.setTo(dto.getReceiver());
            mailMessage.setSubject(dto.getSubject());
            mailMessage.setText(dto.getText());
            javaMailSender.send(mailMessage);
        }catch (Exception e){
            throw new BizException(ExceptionEnum.EMAIL_ERROR);
        }
        return false;
    }
    @Override
    public String sendAttachedMail(EmailDto dto) {
//        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
//        MimeMessageHelper mimeMessageHelper;
//        try{
//            mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
//            mimeMessageHelper.setFrom(sender);
//            mimeMessageHelper.setTo(dto.getReceiver());
//            mimeMessageHelper.setText(dto.getText(), true);
//            mimeMessageHelper.setSubject(dto.getSubject());
//
//            FileSystemResource file = new FileSystemResource(new File(dto.getAttachment()));
//            mimeMessageHelper.addAttachment(file.getFilename(), file);
//
//            javaMailSender.send(mimeMessage);
//            return "Email has been sent !";
//        }
//        catch(Exception e){
//            return "Email error";
//        }
        return "";
    }
    @Override
    public void sendHtmlMail(EmailDto dto){
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper;
        try{
            mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setTo(dto.getReceiver());
            mimeMessageHelper.setSubject(dto.getSubject());
            mimeMessageHelper.setText(dto.getText(), true);

            javaMailSender.send(mimeMessage);
        }catch(Exception e){
            throw new BizException(ExceptionEnum.EMAIL_ERROR);
        }
    }
}
