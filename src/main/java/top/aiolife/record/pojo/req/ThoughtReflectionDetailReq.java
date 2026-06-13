package top.aiolife.record.pojo.req;

import lombok.Data;

/**
 * 闪念复盘沉淀型详情保存请求。
 *
 * @author Ethan
 * @date 2026-06-12
 */
@Data
public class ThoughtReflectionDetailReq {

    private String reflectionSummary;

    private String lessonType;

    private String archiveType;

    private String valueLevel;

    private String improvementAction;

    private String relatedProject;

    private String tags;
}
