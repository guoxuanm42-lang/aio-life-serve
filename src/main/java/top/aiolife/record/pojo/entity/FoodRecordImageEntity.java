package top.aiolife.record.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 美食记录图片实体，保存美食记录图片在 MinIO 中的对象信息和展示元数据。
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("food_record_image")
public class FoodRecordImageEntity extends BaseEntity {

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 美食记录 ID。
     */
    private Long recordId;

    /**
     * MinIO 桶名。
     */
    private String bucketName;

    /**
     * MinIO 对象键。
     */
    private String objectKey;

    /**
     * 图片类型：ingredient/process/finished/failed/other。
     */
    private String imageType;

    /**
     * 图片说明。
     */
    private String caption;

    /**
     * 排序值。
     */
    private Integer sortOrder;
}
