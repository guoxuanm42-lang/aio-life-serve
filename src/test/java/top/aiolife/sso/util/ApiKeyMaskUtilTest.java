package top.aiolife.sso.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * API Key 脱敏工具测试，验证正常凭证和异常输入均不会泄露完整内容。
 *
 * @author Ethan
 * @date 2026-07-14
 */
class ApiKeyMaskUtilTest {

    @Test
    void shouldKeepPrefixAndSuffixForNormalApiKey() {
        assertEquals("ak-12345***cdef", ApiKeyMaskUtil.mask("ak-1234567890abcdef"));
    }

    @Test
    void shouldFullyMaskShortApiKey() {
        assertEquals("***", ApiKeyMaskUtil.mask("ak-short"));
    }

    @Test
    void shouldFullyMaskNullBlankAndWhitespaceValues() {
        assertEquals("***", ApiKeyMaskUtil.mask(null));
        assertEquals("***", ApiKeyMaskUtil.mask(""));
        assertEquals("***", ApiKeyMaskUtil.mask("   "));
    }
}
