package top.aiolife.record.pojo.req;

import lombok.Data;

/**
 * 相册文件夹更新请求。
 *
 * @author Ethan
 * @date 2026-07-02
 */
@Data
public class PhotoFolderUpdateReq {

    /**
     * 文件夹 ID。
     */
    private Long id;

    /**
     * 文件夹名称。
     */
    private String name;
}
