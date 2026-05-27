package top.aiolife.record.pojo.req;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.List;

/**
 * 思考（闪念）保存请求体
 *
 * @author Ethan
 */
@Data
public class ThoughtSaveReq {

    @JsonAlias({"topic"})
    private String subject;

    private String content;

    private String themeKey;

    private String status;

    private List<ThoughtSaveEventReq> events;
}
