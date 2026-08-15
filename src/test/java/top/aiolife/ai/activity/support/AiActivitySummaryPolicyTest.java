package top.aiolife.ai.activity.support;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AI 活动总结文章时间口径测试。
 *
 * @author Ethan
 * @date 2026-08-12
 */
class AiActivitySummaryPolicyTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 8, 1, 0, 0);

    private static final LocalDateTime END = LocalDateTime.of(2026, 8, 12, 15, 30);

    @Test
    void shouldTreatRangeAsStartInclusiveAndEndExclusive() {
        assertTrue(AiActivitySummaryPolicy.isNewArticle(START, START, END));
        assertFalse(AiActivitySummaryPolicy.isNewArticle(END, START, END));
    }

    @Test
    void shouldKeepNewAndUpdatedArticleRulesMutuallyExclusive() {
        LocalDateTime createdInRange = START.plusDays(1);
        LocalDateTime createdBeforeRange = START.minusDays(1);
        LocalDateTime updatedInRange = START.plusDays(2);

        assertTrue(AiActivitySummaryPolicy.isNewArticle(createdInRange, START, END));
        assertFalse(AiActivitySummaryPolicy.isUpdatedArticle(createdInRange, updatedInRange, START, END));
        assertFalse(AiActivitySummaryPolicy.isNewArticle(createdBeforeRange, START, END));
        assertTrue(AiActivitySummaryPolicy.isUpdatedArticle(createdBeforeRange, updatedInRange, START, END));
    }
}
