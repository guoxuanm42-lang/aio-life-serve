package top.aiolife.record.pojo.req;

import lombok.Data;

/**
 * 相册图片移动请求。
 *
 * @author Ethan
 * @date 2026-07-02
 */
@Data
public class PhotoImageMoveReq {

    private Long id;

    private Long targetFolderId;
}
