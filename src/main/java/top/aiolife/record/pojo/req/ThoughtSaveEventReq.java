package top.aiolife.record.pojo.req;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 想法关联事件保存请求体。
 *
 * @author Ethan
 * @date 2026-06-13
 */
@Data
public class ThoughtSaveEventReq {

    private String content;

    @JsonAlias({"eventTime", "recordTime", "happenedAt"})
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
