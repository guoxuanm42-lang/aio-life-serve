package top.aiolife.record.pojo.req;

import lombok.Data;

/**
 * 相册文件夹删除请求。
 *
 * @author Ethan
 * @date 2026-07-02
 */
@Data
public class PhotoFolderDeleteReq {

    /**
     * 文件夹 ID。
     */
    private Long id;
}
