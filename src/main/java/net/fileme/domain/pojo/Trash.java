package net.fileme.domain.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trash {

    @TableId("row_id")
    private Long id;
    private Long dataId;
    private Long userId;
    private Long origin;
    private Integer dataType;
    private LocalDateTime createTime;
}
