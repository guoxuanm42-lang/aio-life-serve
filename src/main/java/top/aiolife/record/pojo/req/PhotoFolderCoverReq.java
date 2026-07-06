package top.aiolife.record.pojo.req;

import lombok.Data;

/**
 * 相册文件夹封面设置请求。
 *
 * @author Ethan
 * @date 2026-07-02
 */
@Data
public class PhotoFolderCoverReq {

    private Long folderId;

    private Long imageId;
}
