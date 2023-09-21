package net.fileme.domain.pojo;

import com.baomidou.mybatisplus.annotation.TableId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Folder{

    @TableId(value = "folder_id")
    private Long id;

    @NotNull
    private Long userId;

    @NotBlank
    private String folderName;

    @NotNull
    private Long parentId;

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", userId=" + userId +
                ", folderName='" + folderName + '\'' +
                ", parentId=" + parentId +
                '}';
    }
    public interface Create extends Default{}
    public interface Rename extends Default{}
    public interface Relocate extends Default{}
}
