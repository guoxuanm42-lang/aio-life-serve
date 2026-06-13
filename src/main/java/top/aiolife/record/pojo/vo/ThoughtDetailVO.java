package top.aiolife.record.pojo.vo;

import lombok.Data;
import top.aiolife.record.pojo.entity.ThoughtActionDetailEntity;
import top.aiolife.record.pojo.entity.ThoughtEmotionDetailEntity;
import top.aiolife.record.pojo.entity.ThoughtEntity;
import top.aiolife.record.pojo.entity.ThoughtReflectionDetailEntity;
import top.aiolife.record.pojo.entity.ThoughtRelaEventEntity;
import top.aiolife.record.pojo.entity.ThoughtStatusLogEntity;

import java.util.List;

/**
 * 闪念详情视图对象，包含主记录、事件流和三类结构化详情。
 *
 * @author Ethan
 * @date 2026-06-12
 */
@Data
public class ThoughtDetailVO {

    private ThoughtEntity thought;

    private List<ThoughtRelaEventEntity> events;

    private ThoughtActionDetailEntity actionDetail;

    private ThoughtEmotionDetailEntity emotionDetail;

    private ThoughtReflectionDetailEntity reflectionDetail;

    private List<ThoughtStatusLogEntity> statusLogs;
}
