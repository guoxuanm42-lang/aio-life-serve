package top.aiolife.record.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 相册图片实体，保存图片在 MinIO 中的对象信息和基础展示元数据。
 *
 * @author Ethan
 * @date 2026-07-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("photo_image")
public class PhotoImageEntity extends BaseEntity {

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 文件夹 ID。
     */
    private Long folderId;

    /**
     * MinIO 桶名。
     */
    private String bucketName;

    /**
     * MinIO 对象键。
     */
    private String objectKey;

    /**
     * 原始文件名。
     */
    private String originalFilename;

    /**
     * 文件 MIME 类型。
     */
    private String contentType;

    /**
     * 文件大小，单位字节。
     */
    private Long fileSize;

    private String title;

    /**
     * 图片备注。
     */
    private String caption;

    /**
     * 排序值。
     */
    private Integer sortOrder;
}
