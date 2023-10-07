package net.fileme.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.fileme.domain.pojo.EmailTemplate;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmailTemplateMapper extends BaseMapper<EmailTemplate> {
}
