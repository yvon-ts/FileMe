package net.fileme.service.impl;

import net.fileme.domain.EmailDetails;
import net.fileme.enums.ExceptionEnum;
import net.fileme.exception.BizException;
import net.fileme.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

@Service
@PropertySource("classpath:credentials.properties")
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender javaMailSender;
    @Value("${mail.sender}")
    private String sender;
    @Override
    public boolean sendSimpleMail(EmailDetails details){
        try{
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(sender);
            mailMessage.setTo(details.getReceiver());
            mailMessage.setSubject(details.getSubject());
            mailMessage.setText(details.getText());
            javaMailSender.send(mailMessage);
        }catch (Exception e){
            throw new BizException(ExceptionEnum.EMAIL_ERROR);
        }
        return false;
    }
    @Override
    public String sendAttachedMail(EmailDetails details) {
//        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
//        MimeMessageHelper mimeMessageHelper;
//        try{
//            mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
//            mimeMessageHelper.setFrom(sender);
//            mimeMessageHelper.setTo(details.getReceiver());
//            mimeMessageHelper.setText(details.getText(), true);
//            mimeMessageHelper.setSubject(details.getSubject());
//
//            FileSystemResource file = new FileSystemResource(new File(details.getAttachment()));
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
    public void sendHtmlMail(EmailDetails details){
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper;
        try{
            mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setTo(details.getReceiver());
            mimeMessageHelper.setSubject(details.getSubject());
            mimeMessageHelper.setText(details.getText(), true);

            javaMailSender.send(mimeMessage);
        }catch(Exception e){

        }
    }
}
