package net.fileme.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.fileme.domain.mapper.EmailTemplateMapper;
import net.fileme.domain.pojo.EmailTemplate;
import net.fileme.enums.EmailTemplateEnum;
import net.fileme.service.EmailTemplateService;
import org.springframework.stereotype.Service;

@Service
public class EmailTemplateServiceImpl extends ServiceImpl<EmailTemplateMapper, EmailTemplate>
        implements EmailTemplateService {
    @Override
    public EmailTemplate findTemplate(EmailTemplateEnum emailTemplateEnum){

        LambdaQueryWrapper<EmailTemplate> lqw = new LambdaQueryWrapper<>();
        lqw.eq(EmailTemplate::getMapping, emailTemplateEnum.getMapping())
                .eq(EmailTemplate::getState, 0);
        return getOne(lqw);
    }
}
