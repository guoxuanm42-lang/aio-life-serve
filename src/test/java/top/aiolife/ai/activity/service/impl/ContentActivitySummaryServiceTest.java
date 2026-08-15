package top.aiolife.ai.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import top.aiolife.ai.activity.model.AiActivityDateRange;
import top.aiolife.ai.activity.model.AiActivitySummaryPeriod;
import top.aiolife.ai.activity.pojo.summary.ArticleSummary;
import top.aiolife.ai.activity.pojo.summary.CountItem;
import top.aiolife.ai.activity.pojo.summary.ProblemSummary;
import top.aiolife.ai.activity.pojo.summary.ThoughtSummary;
import top.aiolife.record.mapper.IArticleCategoryMapper;
import top.aiolife.record.mapper.IArticleMapper;
import top.aiolife.record.mapper.IFoodRecordMapper;
import top.aiolife.record.mapper.IMemoMapper;
import top.aiolife.record.mapper.IPhotoFolderMapper;
import top.aiolife.record.mapper.IProblemCategoryMapper;
import top.aiolife.record.mapper.IProblemNoteMapper;
import top.aiolife.record.mapper.ITaskMapper;
import top.aiolife.record.mapper.IThoughtMapper;
import top.aiolife.record.pojo.entity.ArticleCategoryEntity;
import top.aiolife.record.pojo.entity.ArticleEntity;
import top.aiolife.record.pojo.entity.FoodRecordEntity;
import top.aiolife.record.pojo.entity.MemoEntity;
import top.aiolife.record.pojo.entity.PhotoFolderEntity;
import top.aiolife.record.pojo.entity.ProblemCategoryEntity;
import top.aiolife.record.pojo.entity.ProblemNoteEntity;
import top.aiolife.record.pojo.entity.TaskEntity;
import top.aiolife.record.pojo.entity.ThoughtEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 基础内容活动统计服务单元测试，验证各模块聚合口径、明细限制和用户时间查询条件。
 *
 * @author Ethan
 * @date 2026-08-13
 */
class ContentActivitySummaryServiceTest {

    private static final Long USER_ID = 101L;
    private static final LocalDateTime START = LocalDateTime.of(2026, 8, 10, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 8, 13, 9, 0);
    private static final AiActivityDateRange RANGE = AiActivityDateRange.builder()
            .period(AiActivitySummaryPeriod.WEEK)
            .startTime(START)
            .endTime(END)
            .startDate(START.toLocalDate())
            .endDate(END.toLocalDate())
            .build();

    @BeforeAll
    static void initTableInfo() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        List.of(ThoughtEntity.class, FoodRecordEntity.class, TaskEntity.class, ProblemNoteEntity.class,
                        ProblemCategoryEntity.class, MemoEntity.class, PhotoFolderEntity.class,
                        ArticleEntity.class, ArticleCategoryEntity.class)
                .forEach(entityClass -> TableInfoHelper.initTableInfo(assistant, entityClass));
    }

    @Test
    void shouldSummarizeThoughtDistributionsAndSkipBlankTitles() {
        IThoughtMapper mapper = mock(IThoughtMapper.class);
        List<ThoughtEntity> records = List.of(
                thought(" 新增想法 ", "action", "indigo"),
                thought("", "emotion", "blue"),
                thought("复盘", "unknown", null));
        when(mapper.selectList(any())).thenReturn(records);

        ThoughtSummary summary = new ThoughtActivitySummaryServiceImpl(mapper).summarize(USER_ID, RANGE);

        assertEquals(3, summary.getNewCount());
        assertEquals(List.of("新增想法", "复盘"), summary.getTitles());
        assertEquals(Map.of("action", 1L, "emotion", 1L, "uncategorized", 1L), counts(summary.getTypeDistribution()));
        assertEquals(Map.of("indigo", 1L, "blue", 1L, "uncategorized", 1L), counts(summary.getThemeDistribution()));
    }

    @Test
    void shouldKeepCompleteFoodCountWhenDetailsAreLimitedAndExposeQueryBoundaries() {
        IFoodRecordMapper mapper = mock(IFoodRecordMapper.class);
        List<FoodRecordEntity> records = new ArrayList<>();
        for (int index = 0; index < 12; index++) {
            FoodRecordEntity item = new FoodRecordEntity();
            item.setDishName("菜品" + index);
            records.add(item);
        }
        when(mapper.selectList(any())).thenReturn(records);

        var summary = new FoodActivitySummaryServiceImpl(mapper).summarize(USER_ID, RANGE);

        assertEquals(12, summary.getNewCount());
        assertEquals(10, summary.getDishNames().size());
        ArgumentCaptor<LambdaQueryWrapper<FoodRecordEntity>> captor = wrapperCaptor();
        verify(mapper).selectList(captor.capture());
        String sqlSegment = captor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("user_id"));
        assertTrue(sqlSegment.contains("create_time"));
        assertTrue(captor.getValue().getParamNameValuePairs().containsValue(USER_ID));
        assertTrue(captor.getValue().getParamNameValuePairs().containsValue(START));
        assertTrue(captor.getValue().getParamNameValuePairs().containsValue(END));
        assertTrue(captor.getValue().getParamNameValuePairs().containsValue(0));
    }

    @Test
    void shouldReturnEmptyCollectionsForEmptyTodoNoteAndAlbumData() {
        ITaskMapper taskMapper = mock(ITaskMapper.class);
        IMemoMapper memoMapper = mock(IMemoMapper.class);
        IPhotoFolderMapper folderMapper = mock(IPhotoFolderMapper.class);
        when(taskMapper.selectList(any())).thenReturn(List.of());
        when(memoMapper.selectList(any())).thenReturn(List.of());
        when(folderMapper.selectList(any())).thenReturn(List.of());

        var todo = new TodoActivitySummaryServiceImpl(taskMapper).summarize(USER_ID, RANGE);
        var note = new NoteActivitySummaryServiceImpl(memoMapper).summarize(USER_ID, RANGE);
        var album = new AlbumActivitySummaryServiceImpl(folderMapper).summarize(USER_ID, RANGE);

        assertEquals(0, todo.getNewCount());
        assertEquals(List.of(), todo.getContents());
        assertEquals(List.of(), note.getTitles());
        assertEquals(List.of(), album.getFolderNames());
    }

    @Test
    void shouldSummarizeProblemCategoriesAndDifficultiesWithFallbacks() {
        IProblemNoteMapper noteMapper = mock(IProblemNoteMapper.class);
        IProblemCategoryMapper categoryMapper = mock(IProblemCategoryMapper.class);
        when(noteMapper.selectList(any())).thenReturn(List.of(
                problem("两数之和", 11L, "medium"),
                problem("二叉树", 99L, null),
                problem("动态规划", null, "hard")));
        ProblemCategoryEntity category = new ProblemCategoryEntity();
        category.setId(11L);
        category.setName("数组");
        when(categoryMapper.selectList(any())).thenReturn(List.of(category));

        ProblemSummary summary = new ProblemActivitySummaryServiceImpl(noteMapper, categoryMapper)
                .summarize(USER_ID, RANGE);

        assertEquals(3, summary.getNewCount());
        assertEquals(Map.of("11", 1L, "uncategorized", 2L), counts(summary.getCategoryDistribution()));
        assertEquals(Map.of("medium", 1L, "hard", 1L, "unset", 1L), counts(summary.getDifficultyDistribution()));
        ArgumentCaptor<LambdaQueryWrapper<ProblemCategoryEntity>> captor = wrapperCaptor();
        verify(categoryMapper).selectList(captor.capture());
        assertTrue(captor.getValue().getSqlSegment().contains("user_id"));
        assertTrue(captor.getValue().getParamNameValuePairs().containsValue(USER_ID));
    }

    @Test
    void shouldKeepArticleNewAndUpdatedQueriesSeparate() {
        IArticleMapper articleMapper = mock(IArticleMapper.class);
        IArticleCategoryMapper categoryMapper = mock(IArticleCategoryMapper.class);
        ArticleEntity created = article("新增文章", 21L);
        ArticleEntity updated = article("更新文章", 21L);
        when(articleMapper.selectList(any())).thenReturn(List.of(created), List.of(updated));
        ArticleCategoryEntity category = new ArticleCategoryEntity();
        category.setId(21L);
        category.setName("研发");
        when(categoryMapper.selectList(any())).thenReturn(List.of(category));

        ArticleSummary summary = new ArticleActivitySummaryServiceImpl(articleMapper, categoryMapper)
                .summarize(USER_ID, RANGE);

        assertEquals(1, summary.getNewCount());
        assertEquals(1, summary.getUpdatedCount());
        assertEquals(List.of("新增文章"), summary.getNewTitles());
        assertEquals(List.of("更新文章"), summary.getUpdatedTitles());
        assertEquals(Map.of("21", 1L), counts(summary.getCategoryDistribution()));
        verify(articleMapper, times(2)).selectList(any());
    }

    @Test
    void shouldReturnTrimmedTodoNoteAndAlbumDetails() {
        ITaskMapper taskMapper = mock(ITaskMapper.class);
        IMemoMapper memoMapper = mock(IMemoMapper.class);
        IPhotoFolderMapper folderMapper = mock(IPhotoFolderMapper.class);
        TaskEntity task = new TaskEntity();
        task.setContent(" 完成统计 ");
        MemoEntity memo = new MemoEntity();
        memo.setTitle(" 统计笔记 ");
        PhotoFolderEntity folder = new PhotoFolderEntity();
        folder.setName(" 开发截图 ");
        when(taskMapper.selectList(any())).thenReturn(List.of(task));
        when(memoMapper.selectList(any())).thenReturn(List.of(memo));
        when(folderMapper.selectList(any())).thenReturn(List.of(folder));

        assertEquals(List.of("完成统计"), new TodoActivitySummaryServiceImpl(taskMapper).summarize(USER_ID, RANGE).getContents());
        assertEquals(List.of("统计笔记"), new NoteActivitySummaryServiceImpl(memoMapper).summarize(USER_ID, RANGE).getTitles());
        assertEquals(List.of("开发截图"), new AlbumActivitySummaryServiceImpl(folderMapper).summarize(USER_ID, RANGE).getFolderNames());
    }

    @Test
    void shouldRejectInvalidUserAndRange() {
        IFoodRecordMapper mapper = mock(IFoodRecordMapper.class);
        FoodActivitySummaryServiceImpl service = new FoodActivitySummaryServiceImpl(mapper);

        assertThrows(IllegalArgumentException.class, () -> service.summarize(null, RANGE));
        assertThrows(IllegalArgumentException.class, () -> service.summarize(USER_ID, null));
        AiActivityDateRange invalidRange = AiActivityDateRange.builder().startTime(END).endTime(START).build();
        assertThrows(IllegalArgumentException.class, () -> service.summarize(USER_ID, invalidRange));
    }

    private static ThoughtEntity thought(String title, String type, String theme) {
        ThoughtEntity entity = new ThoughtEntity();
        entity.setSubject(title);
        entity.setThoughtType(type);
        entity.setThemeKey(theme);
        return entity;
    }

    private static ProblemNoteEntity problem(String title, Long categoryId, String difficulty) {
        ProblemNoteEntity entity = new ProblemNoteEntity();
        entity.setTitle(title);
        entity.setCategoryId(categoryId);
        entity.setDifficulty(difficulty);
        return entity;
    }

    private static ArticleEntity article(String title, Long categoryId) {
        ArticleEntity entity = new ArticleEntity();
        entity.setTitle(title);
        entity.setCategoryId(categoryId);
        return entity;
    }

    private static Map<String, Long> counts(List<CountItem> items) {
        return items.stream().collect(java.util.stream.Collectors.toMap(CountItem::getKey, CountItem::getCount));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> ArgumentCaptor<LambdaQueryWrapper<T>> wrapperCaptor() {
        return (ArgumentCaptor) ArgumentCaptor.forClass(LambdaQueryWrapper.class);
    }
}
