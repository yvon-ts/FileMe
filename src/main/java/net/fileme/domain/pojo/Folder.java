package net.fileme.domain.pojo;

import com.baomidou.mybatisplus.annotation.TableId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Folder{

    @TableId(value = "folder_id")
    private Long id;

    private Long userId;

    private String folderName;

    private Long parentId;

    private Integer accessLevel;

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", userId=" + userId +
                ", folderName='" + folderName + '\'' +
                ", parentId=" + parentId +
                '}';
    }
}
