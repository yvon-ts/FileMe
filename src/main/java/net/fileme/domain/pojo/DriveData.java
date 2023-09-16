package net.fileme.domain.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class DriveData {

    @TableId
    private Long id;
    private String DataName;
    private Integer dataType;
}
