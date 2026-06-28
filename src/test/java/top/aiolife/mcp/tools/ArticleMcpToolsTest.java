package top.aiolife.mcp.tools;

import org.junit.jupiter.api.Test;
import top.aiolife.mcp.pojo.req.ArticleSaveToolReq;
import top.aiolife.record.pojo.req.ArticleSaveReq;
import top.aiolife.record.service.ArticleAiFacade;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

/**
 * 文章 MCP 工具测试，验证新增请求到业务保存请求的字段映射。
 *
 * @author Ethan
 * @date 2026-06-25
 */
class ArticleMcpToolsTest {

    @Test
    void shouldMapSaveToolRequestToArticleSaveRequest() throws Exception {
        ArticleSaveToolReq req = new ArticleSaveToolReq();
        req.setIdempotencyKey("article-key-1");
        req.setCategoryId(10L);
        req.setTitle("Markdown 写作规范");
        req.setSummary("记录 Markdown 写作结构");
        req.setMarkdownContent("# 标题\n\n正文");
        req.setTags("写作,Markdown");
        req.setStatus("draft");

        ArticleSaveReq saveReq = invokeToSaveReq(req);

        assertNull(saveReq.getId());
        assertEquals(10L, saveReq.getCategoryId());
        assertEquals("Markdown 写作规范", saveReq.getTitle());
        assertEquals("记录 Markdown 写作结构", saveReq.getSummary());
        assertEquals("# 标题\n\n正文", saveReq.getMarkdownContent());
        assertEquals("写作,Markdown", saveReq.getTags());
        assertEquals("draft", saveReq.getStatus());
    }

    private ArticleSaveReq invokeToSaveReq(ArticleSaveToolReq req) throws Exception {
        ArticleMcpTools tools = new ArticleMcpTools(mock(ArticleAiFacade.class));
        Method method = ArticleMcpTools.class.getDeclaredMethod("toSaveReq", ArticleSaveToolReq.class);
        method.setAccessible(true);
        return (ArticleSaveReq) method.invoke(tools, req);
    }
}
