package top.aiolife.record.service;

import org.junit.jupiter.api.Test;
import top.aiolife.record.mapper.IFoodRecordMapper;
import top.aiolife.record.pojo.entity.FoodRecordEntity;
import top.aiolife.record.pojo.req.FoodRecordSaveReq;
import top.aiolife.record.pojo.vo.FoodRecordDetailVO;
import top.aiolife.record.util.RedisUtil;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 美食记录 AI 门面测试，验证 MCP 写入幂等和更新分流。
 *
 * @author Ethan
 * @date 2026-06-10
 */
class FoodRecordAiFacadeTest {

    @Test
    void shouldReturnExistingRecordWhenIdempotencyKeyWasUsed() {
        IFoodRecordService foodRecordService = mock(IFoodRecordService.class);
        RedisUtil redisUtil = mock(RedisUtil.class);
        FoodRecordAiFacade facade = new FoodRecordAiFacade(foodRecordService, mock(IFoodRecordMapper.class), redisUtil);
        FoodRecordDetailVO existing = detail(100L);
        when(redisUtil.get("mcp:idemp:food_record_save:1:key-1")).thenReturn("100");
        when(foodRecordService.detail(100L, 1L)).thenReturn(existing);

        FoodRecordDetailVO result = facade.save(new FoodRecordSaveReq(), 1L, "key-1");

        assertSame(existing, result);
        verify(foodRecordService, never()).create(any(), any());
    }

    @Test
    void shouldStoreCreatedRecordIdForIdempotencyKey() {
        IFoodRecordService foodRecordService = mock(IFoodRecordService.class);
        RedisUtil redisUtil = mock(RedisUtil.class);
        FoodRecordAiFacade facade = new FoodRecordAiFacade(foodRecordService, mock(IFoodRecordMapper.class), redisUtil);
        FoodRecordDetailVO created = detail(100L);
        when(redisUtil.setIfAbsent(eq("mcp:idemp:food_record_save:1:key-1"), eq("LOCK"), anyLong(), eq(TimeUnit.SECONDS)))
                .thenReturn(true);
        when(foodRecordService.create(any(), eq(1L))).thenReturn(created);

        FoodRecordDetailVO result = facade.save(new FoodRecordSaveReq(), 1L, "key-1");

        assertSame(created, result);
        verify(redisUtil).set("mcp:idemp:food_record_save:1:key-1", "100", TimeUnit.HOURS.toSeconds(24), TimeUnit.SECONDS);
    }

    @Test
    void shouldUpdateWhenRequestContainsId() {
        IFoodRecordService foodRecordService = mock(IFoodRecordService.class);
        FoodRecordAiFacade facade = new FoodRecordAiFacade(foodRecordService, mock(IFoodRecordMapper.class), mock(RedisUtil.class));
        FoodRecordSaveReq req = new FoodRecordSaveReq();
        req.setId(10L);
        FoodRecordDetailVO updated = detail(10L);
        when(foodRecordService.update(req, 1L)).thenReturn(updated);

        FoodRecordDetailVO result = facade.save(req, 1L, "key-1");

        assertSame(updated, result);
        verify(foodRecordService).update(req, 1L);
    }

    private FoodRecordDetailVO detail(Long id) {
        FoodRecordEntity record = new FoodRecordEntity();
        record.setId(id);
        FoodRecordDetailVO detail = new FoodRecordDetailVO();
        detail.setRecord(record);
        return detail;
    }
}
