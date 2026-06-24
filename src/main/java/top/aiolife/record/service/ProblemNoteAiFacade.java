package top.aiolife.record.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.aiolife.core.resq.PageResp;
import top.aiolife.mcp.pojo.req.ProblemNoteQueryToolReq;
import top.aiolife.mcp.pojo.vo.ProblemNoteQueryToolVO;
import top.aiolife.record.pojo.entity.ProblemNoteEntity;
import top.aiolife.record.pojo.req.ProblemNoteQueryReq;
import top.aiolife.record.pojo.req.ProblemNoteSaveReq;
import top.aiolife.record.util.RedisUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 题目记录 MCP/AI 适配门面，负责题库查询、新增题目和幂等控制。
 *
 * @author Ethan
 * @date 2026-06-24
 */
@Component
@RequiredArgsConstructor
public class ProblemNoteAiFacade {

    private static final long IDEMPOTENCY_TTL_SECONDS = TimeUnit.HOURS.toSeconds(24);

    private static final String LOCK_VALUE = "LOCK";

    private final IProblemNoteService problemNoteService;

    private final RedisUtil redisUtil;

    /**
     * 查询当前用户题库，并转换为 MCP 专用返回结构。
     *
     * @param req MCP 查询请求
     * @param userId 当前用户 ID
     * @return 题目记录分页结果
     *
     * @author Ethan
     * @date 2026-06-24
     */
    public PageResp<ProblemNoteQueryToolVO> query(ProblemNoteQueryToolReq req, long userId) {
        PageResp<ProblemNoteEntity> page = problemNoteService.query(toQueryReq(req), userId);
        List<ProblemNoteQueryToolVO> items = page.getItems() == null
                ? List.of()
                : page.getItems().stream().map(this::toToolVO).toList();
        return PageResp.of(items, page.getTotal());
    }

    /**
     * 新增当前用户题目记录，支持基于幂等键避免重复写入。
     *
     * @param req 题目保存请求
     * @param userId 当前用户 ID
     * @param idempotencyKey 幂等键
     * @return 新增后的题目记录
     *
     * @author Ethan
     * @date 2026-06-24
     */
    public ProblemNoteEntity create(ProblemNoteSaveReq req, long userId, String idempotencyKey) {
        String idempotencyRedisKey = buildIdempotencyKey(userId, idempotencyKey);
        if (idempotencyRedisKey == null) {
            return problemNoteService.create(req, userId);
        }

        String existing = redisUtil.get(idempotencyRedisKey);
        if (StringUtils.hasText(existing) && !LOCK_VALUE.equals(existing)) {
            return problemNoteService.detail(Long.parseLong(existing), userId);
        }

        Boolean locked = redisUtil.setIfAbsent(idempotencyRedisKey, LOCK_VALUE, IDEMPOTENCY_TTL_SECONDS, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            String savedId = redisUtil.get(idempotencyRedisKey);
            if (StringUtils.hasText(savedId) && !LOCK_VALUE.equals(savedId)) {
                return problemNoteService.detail(Long.parseLong(savedId), userId);
            }
            return null;
        }

        try {
            ProblemNoteEntity created = problemNoteService.create(req, userId);
            redisUtil.set(idempotencyRedisKey, String.valueOf(created.getId()), IDEMPOTENCY_TTL_SECONDS, TimeUnit.SECONDS);
            return created;
        } catch (Exception e) {
            redisUtil.delete(idempotencyRedisKey);
            throw e;
        }
    }

    private ProblemNoteQueryReq toQueryReq(ProblemNoteQueryToolReq req) {
        ProblemNoteQueryToolReq safeReq = req == null ? new ProblemNoteQueryToolReq() : req;
        ProblemNoteQueryReq queryReq = new ProblemNoteQueryReq();
        queryReq.setPage(safeReq.getPage());
        queryReq.setPageSize(safeReq.getPageSize());
        queryReq.setKeyword(safeReq.getKeyword());
        queryReq.setDifficulty(safeReq.getDifficulty());
        queryReq.setStatus(safeReq.getStatus());
        queryReq.setTags(safeReq.getTags());
        queryReq.setCategoryId(safeReq.getCategoryId());
        queryReq.setUncategorized(safeReq.getUncategorized());
        return queryReq;
    }

    private ProblemNoteQueryToolVO toToolVO(ProblemNoteEntity entity) {
        ProblemNoteQueryToolVO vo = new ProblemNoteQueryToolVO();
        vo.setId(entity.getId());
        vo.setCategoryId(entity.getCategoryId());
        vo.setTitle(entity.getTitle());
        vo.setProblemContent(entity.getProblemContent());
        vo.setSolutionCode(entity.getSolutionCode());
        vo.setPseudoCode(entity.getPseudoCode());
        vo.setIdeaNote(entity.getIdeaNote());
        vo.setDifficulty(entity.getDifficulty());
        vo.setTags(entity.getTags());
        vo.setStatus(entity.getStatus());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    private String buildIdempotencyKey(long userId, String idempotencyKey) {
        if (!StringUtils.hasText(idempotencyKey)) {
            return null;
        }
        return "mcp:idemp:problem_note_save:" + userId + ":" + idempotencyKey.trim();
    }
}
