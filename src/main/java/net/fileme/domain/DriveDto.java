package net.fileme.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class DriveDto {

    @TableId
    private Long id;
    private String dataName;
    private Integer dataType;
}
