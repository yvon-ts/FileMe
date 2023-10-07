package net.fileme.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.fileme.domain.pojo.EmailTemplate;
import net.fileme.enums.EmailTemplateEnum;

public interface EmailTemplateService extends IService<EmailTemplate> {
    EmailTemplate findTemplate(EmailTemplateEnum emailTemplateEnum);
}
