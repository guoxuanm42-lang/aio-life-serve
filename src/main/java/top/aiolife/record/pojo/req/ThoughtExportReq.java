package top.aiolife.record.pojo.req;

import lombok.Data;

/**
 * 闪念 Excel 导出筛选请求。
 *
 * @author Ethan
 * @date 2026-06-13
 */
@Data
public class ThoughtExportReq {

    private String themeKey;

    private String status;

    private String thoughtType;

    private String subject;
}
