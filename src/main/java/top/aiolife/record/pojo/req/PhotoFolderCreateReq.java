package top.aiolife.record.pojo.req;

import lombok.Data;

/**
 * 相册文件夹创建请求。
 *
 * @author Ethan
 * @date 2026-07-02
 */
@Data
public class PhotoFolderCreateReq {

    /**
     * 父文件夹 ID，根级文件夹可为空或 0。
     */
    private Long parentId;

    /**
     * 文件夹名称。
     */
    private String name;
}
