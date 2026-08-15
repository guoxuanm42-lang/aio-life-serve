package top.aiolife.ai.activity.pojo.summary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AI 活动总结上下文序列化与空数据协议测试。
 *
 * @author Ethan
 * @date 2026-08-12
 */
class AiActivitySummaryContextTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void shouldOmitNullModulesAndKeepRangeFields() throws Exception {
        AiActivitySummaryContext context = baseContext();

        JsonNode root = objectMapper.readTree(objectMapper.writeValueAsString(context));

        assertEquals("week", root.get("period").asText());
        assertNotNull(root.get("startTime"));
        assertNotNull(root.get("endTime"));
        assertFalse(root.has("timeRecord"));
        assertFalse(root.has("thought"));
        assertFalse(root.has("mcp"));
    }

    @Test
    void shouldSerializeInitializedCollectionsAsEmptyArrays() throws Exception {
        AiActivitySummaryContext context = baseContext();
        ThoughtSummary thought = new ThoughtSummary();
        thought.setNewCount(1);
        context.setThought(thought);

        JsonNode thoughtNode = objectMapper.readTree(objectMapper.writeValueAsString(context)).get("thought");

        assertTrue(thoughtNode.get("typeDistribution").isArray());
        assertTrue(thoughtNode.get("typeDistribution").isEmpty());
        assertTrue(thoughtNode.get("themeDistribution").isArray());
        assertTrue(thoughtNode.get("titles").isArray());
    }

    @Test
    void shouldApplyModuleEmptyRules() {
        assertTrue(new TimeRecordSummary().isEmpty());
        assertTrue(new ThoughtSummary().isEmpty());
        assertTrue(new FoodSummary().isEmpty());
        assertTrue(new TodoSummary().isEmpty());
        assertTrue(new ProblemSummary().isEmpty());
        assertTrue(new NoteSummary().isEmpty());
        assertTrue(new AlbumSummary().isEmpty());
        assertTrue(new ArticleSummary().isEmpty());
        assertTrue(new McpSummary().isEmpty());

        ArticleSummary article = new ArticleSummary();
        article.setUpdatedCount(1);
        assertFalse(article.isEmpty());
    }

    @Test
    void shouldKeepFullCountWhenTitleDetailsAreLimited() {
        ThoughtSummary thought = new ThoughtSummary();
        thought.setNewCount(25);
        for (int index = 0; index < 10; index++) {
            thought.getTitles().add("标题" + index);
        }

        assertEquals(25, thought.getNewCount());
        assertEquals(10, thought.getTitles().size());
    }

    private AiActivitySummaryContext baseContext() {
        AiActivitySummaryContext context = new AiActivitySummaryContext();
        context.setPeriod("week");
        context.setStartTime(LocalDateTime.of(2026, 8, 10, 0, 0));
        context.setEndTime(LocalDateTime.of(2026, 8, 12, 15, 30));
        return context;
    }
}
