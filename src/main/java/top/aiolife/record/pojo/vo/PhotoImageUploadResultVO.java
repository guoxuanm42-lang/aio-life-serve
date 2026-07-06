package top.aiolife.record.pojo.vo;

import lombok.Data;
import top.aiolife.record.pojo.entity.PhotoImageEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 相册图片批量上传结果。
 *
 * @author Ethan
 * @date 2026-07-02
 */
@Data
public class PhotoImageUploadResultVO {

    /**
     * 上传成功的图片列表。
     */
    private List<PhotoImageEntity> successList = new ArrayList<>();

    /**
     * 上传失败的文件列表。
     */
    private List<FailureItem> failureList = new ArrayList<>();

    /**
     * 单个文件失败信息。
     *
     * @author Ethan
     * @date 2026-07-02
     */
    @Data
    public static class FailureItem {

        /**
         * 原始文件名。
         */
        private String filename;

        /**
         * 失败原因。
         */
        private String reason;
    }
}
