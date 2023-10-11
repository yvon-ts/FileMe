package net.fileme.domain.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class EmailTemplate extends BasePojo{
    @TableId("template_id")
    private Long id;
    private Integer mapping;
    private String title;
    private String content;
    private String endpoint;
    private String btn;
}
