package top.aiolife.ai.activity.service.impl;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 活动报告核心文本质量规则测试，验证确定无效文本与保守放行边界。
 *
 * @author Ethan
 * @date 2026-08-16
 */
class ActivitySummaryTextQualityTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "   ", "\u200B\u200C\u200D\uFEFF", "损坏�标题", "\0标题", "标题\7",
            "???", "？？？？", "? ？ ?", "***", "_ _ _", "---"
    })
    void shouldRejectDefinitelyInvalidCoreText(String text) {
        assertFalse(ActivitySummarySupport.isValidCoreText(text));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "?", "??", "正常标点！", "aaa", "test", "测试", "123", "单", "API", "😀", "未分类",
            "正文\n第二行", "带\t制表符", "有效\u200B文本", "...", "==="
    })
    void shouldKeepConservativelyValidCoreText(String text) {
        assertTrue(ActivitySummarySupport.isValidCoreText(text));
    }
}
