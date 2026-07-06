package top.aiolife.record.pojo.req;

import lombok.Data;

/**
 * 相册图片信息更新请求。
 *
 * @author Ethan
 * @date 2026-07-02
 */
@Data
public class PhotoImageUpdateReq {

    private Long id;

    private String title;

    private String caption;
}
