package top.aiolife.record.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.aiolife.record.mapper.IFoodRecordIngredientMapper;
import top.aiolife.record.mapper.IFoodRecordMapper;
import top.aiolife.record.mapper.IFoodRecordStepMapper;
import top.aiolife.record.pojo.entity.FoodRecordEntity;
import top.aiolife.record.pojo.entity.FoodRecordIngredientEntity;
import top.aiolife.record.pojo.entity.FoodRecordStepEntity;
import top.aiolife.record.pojo.req.FoodRecordIngredientSaveReq;
import top.aiolife.record.pojo.req.FoodRecordSaveReq;
import top.aiolife.record.pojo.req.FoodRecordStepSaveReq;
import top.aiolife.record.service.IFoodRecordImageService;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 美食记录服务测试，验证基础保存校验和逻辑删除流程。
 *
 * @author Ethan
 * @date 2026-05-31
 */
@ExtendWith(MockitoExtension.class)
class FoodRecordServiceImplTest {

    @Mock
    private IFoodRecordMapper foodRecordMapper;

    @Mock
    private IFoodRecordIngredientMapper ingredientMapper;

    @Mock
    private IFoodRecordStepMapper stepMapper;

    @Mock
    private IFoodRecordImageService imageService;

    @Test
    void shouldRejectBlankDishName() {
        FoodRecordServiceImpl service = service();
        FoodRecordSaveReq req = new FoodRecordSaveReq();
        req.setDishName(" ");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.create(req, 1L));

        assertEquals("菜名不能为空", exception.getMessage());
    }

    @Test
    void shouldCreateRecordWithIngredientsAndSteps() {
        FoodRecordServiceImpl service = service();
        FoodRecordSaveReq req = createSaveReq();
        AtomicReference<FoodRecordEntity> savedRecord = new AtomicReference<>();
        doAnswer(invocation -> {
            FoodRecordEntity entity = invocation.getArgument(0);
            entity.setId(100L);
            savedRecord.set(entity);
            return 1;
        }).when(foodRecordMapper).insert(any(FoodRecordEntity.class));
        when(foodRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenAnswer(invocation -> savedRecord.get());
        when(ingredientMapper.selectList(any())).thenReturn(List.of());
        when(stepMapper.selectList(any())).thenReturn(List.of());
        when(imageService.listByRecord(any(), any())).thenReturn(List.of());

        service.create(req, 1L);

        ArgumentCaptor<FoodRecordEntity> recordCaptor = ArgumentCaptor.forClass(FoodRecordEntity.class);
        ArgumentCaptor<FoodRecordIngredientEntity> ingredientCaptor = ArgumentCaptor.forClass(FoodRecordIngredientEntity.class);
        ArgumentCaptor<FoodRecordStepEntity> stepCaptor = ArgumentCaptor.forClass(FoodRecordStepEntity.class);
        verify(foodRecordMapper).insert(recordCaptor.capture());
        verify(ingredientMapper).insert(ingredientCaptor.capture());
        verify(stepMapper).insert(stepCaptor.capture());

        FoodRecordEntity record = recordCaptor.getValue();
        assertEquals("番茄炒蛋", record.getDishName());
        assertEquals("draft", record.getStatus());
        assertEquals(25, record.getTotalMinutes());
        assertEquals(1L, record.getUserId());
        assertEquals("番茄", ingredientCaptor.getValue().getName());
        assertEquals("切配", stepCaptor.getValue().getTitle());
    }

    @Test
    void shouldLogicDeleteRecordAndChildren() {
        FoodRecordServiceImpl service = service();
        FoodRecordEntity exist = new FoodRecordEntity();
        exist.setId(100L);
        exist.setUserId(1L);
        when(foodRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(exist);

        service.delete(100L, 1L);

        ArgumentCaptor<FoodRecordEntity> recordCaptor = ArgumentCaptor.forClass(FoodRecordEntity.class);
        verify(foodRecordMapper).updateById(recordCaptor.capture());
        verify(ingredientMapper).update(any(FoodRecordIngredientEntity.class), any(UpdateWrapper.class));
        verify(stepMapper).update(any(FoodRecordStepEntity.class), any(UpdateWrapper.class));
        assertEquals(1, recordCaptor.getValue().getIsDeleted());
        assertTrue(recordCaptor.getValue().getUpdateTime() != null);
    }

    private FoodRecordServiceImpl service() {
        return new FoodRecordServiceImpl(foodRecordMapper, ingredientMapper, stepMapper, imageService);
    }

    private FoodRecordSaveReq createSaveReq() {
        FoodRecordIngredientSaveReq ingredient = new FoodRecordIngredientSaveReq();
        ingredient.setName("番茄");
        ingredient.setQuantity("2");
        ingredient.setUnit("个");

        FoodRecordStepSaveReq step = new FoodRecordStepSaveReq();
        step.setTitle("切配");
        step.setDescription("番茄切块，鸡蛋打散");

        FoodRecordSaveReq req = new FoodRecordSaveReq();
        req.setDishName(" 番茄炒蛋 ");
        req.setCookDate(LocalDate.of(2026, 5, 31));
        req.setPrepMinutes(10);
        req.setCookMinutes(15);
        req.setIngredients(List.of(ingredient));
        req.setSteps(List.of(step));
        return req;
    }
}
