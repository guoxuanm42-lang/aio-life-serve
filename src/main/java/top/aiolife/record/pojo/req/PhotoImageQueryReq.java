package top.aiolife.record.pojo.req;

import lombok.Data;

/**
 * 相册图片分页查询请求。
 *
 * @author Ethan
 * @date 2026-07-02
 */
@Data
public class PhotoImageQueryReq {

    /**
     * 文件夹 ID。
     */
    private Long folderId;

    /**
     * 页码，从 1 开始。
     */
    private Integer page;

    /**
     * 每页数量。
     */
    private Integer pageSize;

    private String keyword;
}
