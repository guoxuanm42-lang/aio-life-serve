package top.aiolife.record.pojo.req;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 思考（闪念）保存请求体
 *
 * @author Ethan
 * @date 2026-06-13
 */
@Data
public class ThoughtSaveReq {

    private Long id;

    @JsonAlias({"topic"})
    private String subject;

    private String content;

    private String themeKey;

    private String status;

    private String thoughtType;

    private String changeReason;

    @JsonAlias({"recordTime", "happenedAt"})
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private List<ThoughtSaveEventReq> events;

    private ThoughtActionDetailReq actionDetail;

    private ThoughtEmotionDetailReq emotionDetail;

    private ThoughtReflectionDetailReq reflectionDetail;
}
