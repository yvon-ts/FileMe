package net.fileme.domain.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemoveList extends BasePojo {
    @TableId("row_id")
    private Long id;
    private Long fileId;
    private Long userId;
    private String filePath;
    private Integer location;
    private Integer state;
}
