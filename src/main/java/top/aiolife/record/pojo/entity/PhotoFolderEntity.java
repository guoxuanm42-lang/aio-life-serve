package top.aiolife.record.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 相册文件夹实体，保存当前用户的多级相册目录结构。
 *
 * @author Ethan
 * @date 2026-07-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("photo_folder")
public class PhotoFolderEntity extends BaseEntity {

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 父文件夹 ID，根级文件夹固定为 0。
     */
    private Long parentId;

    /**
     * 文件夹名称。
     */
    private String name;

    /**
     * 排序值。
     */
    private Integer sortOrder;

    private Long coverImageId;
}
