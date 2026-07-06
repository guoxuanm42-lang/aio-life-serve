package top.aiolife.record.pojo.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 相册文件夹树节点。
 *
 * @author Ethan
 * @date 2026-07-02
 */
@Data
public class PhotoFolderTreeVO {

    /**
     * 文件夹 ID。
     */
    private Long id;

    /**
     * 父文件夹 ID。
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

    private Long imageCount;

    private Long coverImageId;

    private String coverImageUrl;

    /**
     * 子文件夹列表。
     */
    private List<PhotoFolderTreeVO> children = new ArrayList<>();
}
