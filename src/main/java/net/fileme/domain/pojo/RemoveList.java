package net.fileme.domain.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("remove_list")
public class RemoveList extends BasePojo {
    @TableId
    private Long fileId;
    private Long userId;
    private String filePath;
    private Integer location;
    private Integer state;
}
