package top.aiolife.mcp.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 闪念 MCP 查询返回结果。
 *
 * @author Ethan
 * @date 2026-06-13
 */
@Data
public class ThoughtQueryToolResp {

    private Integer page;

    private Integer pageSize;

    private Long total;

    private Boolean hasMore;

    private List<Item> items;

    /**
     * 闪念查询结果条目。
     *
     * @author Ethan
     * @date 2026-06-13
     */
    @Data
    public static class Item {

        private Long id;

        private String subject;

        private String summary;

        private String content;

        private String themeKey;

        private String categoryName;

        private String status;

        private String statusName;

        private String thoughtType;

        private String thoughtTypeName;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updateTime;

        private List<Event> events;
    }

    /**
     * 闪念关联事件查询结果条目。
     *
     * @author Ethan
     * @date 2026-06-10
     */
    @Data
    public static class Event {

        private Long id;

        private String content;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
    }
}
