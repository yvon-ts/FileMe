package net.fileme.domain.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("folder_trash")
public class FolderTrash {
    @TableId("folder_id")
    private Long id;
    private Long origin;
    private LocalDateTime createTime;
}