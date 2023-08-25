package net.fileme.domain.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
@TableName("file_data")
@Data
public class File extends BasePojo{
    @TableId(value = "file_id")
    private Long id;
    private Long userId;
    private String fileName;
    private String ext;
    private Long size;
    private Long folderId;
    private Integer accessLevel;
    private LocalDateTime deleteEta;

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", userId=" + userId +
                ", fileName='" + fileName + '\'' +
                ", ext='" + ext + '\'' +
                ", size=" + size +
                ", folderId=" + folderId +
                ", accessLevel=" + accessLevel +
                ", createTime=" + super.getCreateTime() +
                ", updateTime=" + super.getUpdateTime() +
                '}';
    }
}
