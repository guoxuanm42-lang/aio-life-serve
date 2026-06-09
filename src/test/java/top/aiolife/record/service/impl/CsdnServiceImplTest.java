package top.aiolife.record.service.impl;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import top.aiolife.record.pojo.csdn.CsdnArticleVO;
import top.aiolife.record.pojo.csdn.CsdnStatsVO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * CSDN 编程看板解析逻辑测试。
 *
 * @author Ethan
 * @date 2026-06-09
 */
class CsdnServiceImplTest {

    @Test
    void shouldParseStatsFromSampleHtml() {
        CsdnServiceImpl service = new CsdnServiceImpl();

        CsdnStatsVO stats = service.parseStats(Jsoup.parse("""
                <html><body>
                  <ul class="user-profile-statistics">
                    <li><span>总访问量</span><span>184,216</span></li>
                    <li><span>原创</span><span>221</span></li>
                    <li><span>全站排名</span><span>0</span></li>
                    <li><span>粉丝</span><span>1,420</span></li>
                    <li><span>获赞</span><span>1.5万</span></li>
                    <li><span>评论</span><span>38</span></li>
                  </ul>
                </body></html>
                """));

        assertEquals(184_216L, stats.getViewCount());
        assertEquals(221L, stats.getOriginalCount());
        assertEquals(0L, stats.getRank());
        assertEquals(1_420L, stats.getFansCount());
        assertEquals(15_000L, stats.getLikeCount());
        assertEquals(38L, stats.getCommentCount());
    }

    @Test
    void shouldParseStatsWhenNumberAppearsBeforeLabel() {
        CsdnServiceImpl service = new CsdnServiceImpl();

        CsdnStatsVO stats = service.parseStats(Jsoup.parse("""
                <html><body>
                  <div class="data-info">
                    <div><span>184,657</span><span>总访问量</span></div>
                    <div><span>222</span><span>原创</span></div>
                    <div><span>0</span><span>全站排名</span></div>
                    <div><span>1,422</span><span>粉丝数</span></div>
                    <div><span>1,549</span><span>获赞数</span></div>
                  </div>
                </body></html>
                """));

        assertEquals(184_657L, stats.getViewCount());
        assertEquals(222L, stats.getOriginalCount());
        assertEquals(0L, stats.getRank());
        assertEquals(1_422L, stats.getFansCount());
        assertEquals(1_549L, stats.getLikeCount());
    }

    @Test
    void shouldParseStatsFromCsdnProfileStatisticClasses() {
        CsdnServiceImpl service = new CsdnServiceImpl();

        CsdnStatsVO stats = service.parseStats(Jsoup.parse("""
                <html><body>
                  <div class="user-profile-statistics">
                    <span class="user-profile-statistics-num">184,659</span>
                    <span class="user-profile-statistics-name">总访问量</span>
                    <span class="user-profile-statistics-num">222</span>
                    <span class="user-profile-statistics-name">原创</span>
                    <span class="user-profile-statistics-num">11070</span>
                    <span class="user-profile-statistics-name">排名</span>
                    <span class="user-profile-statistics-num">1,422</span>
                    <span class="user-profile-statistics-name">粉丝</span>
                  </div>
                  <div class="aside-common-box-content-text">获得 1,550 次点赞</div>
                  <div class="aside-common-box-content-text">获得 38 次评论</div>
                </body></html>
                """));

        assertEquals(184_659L, stats.getViewCount());
        assertEquals(222L, stats.getOriginalCount());
        assertEquals(11_070L, stats.getRank());
        assertEquals(1_422L, stats.getFansCount());
        assertEquals(1_550L, stats.getLikeCount());
        assertEquals(38L, stats.getCommentCount());
    }

    @Test
    void shouldParseArticleCountersFromCsdnCounterClasses() {
        CsdnServiceImpl service = new CsdnServiceImpl();

        List<CsdnArticleVO> articles = service.parseArticles(Jsoup.parse("""
                <html><body>
                  <article class="blog-list-box">
                    <h4><a href="https://blog.csdn.net/lys1313013/article/details/321">CSDN class 统计测试</a></h4>
                    <div class="blog-list-content">测试 CSDN 文章统计 class。</div>
                    <div class="view-time-box">博文更新于 2026.06.09 ·</div>
                    <div class="blog-list-footer">
                      <span class="view-num">234</span>
                      <span class="give-like-num">3</span>
                      <span class="comment-num">评论 0</span>
                      <span class="comment-num">收藏 3</span>
                    </div>
                  </article>
                </body></html>
                """), 20);

        assertEquals(1, articles.size());
        CsdnArticleVO article = articles.get(0);
        assertEquals(234L, article.getViewCount());
        assertEquals(0L, article.getCommentCount());
        assertEquals(3L, article.getLikeCount());
        assertEquals(3L, article.getCollectCount());
    }

    @Test
    void shouldParseArticlesFromSampleHtml() {
        CsdnServiceImpl service = new CsdnServiceImpl();

        List<CsdnArticleVO> articles = service.parseArticles(Jsoup.parse("""
                <html><body>
                  <div class="article-item-box">
                    <h4><a href="https://blog.csdn.net/lys1313013/article/details/123456" title="API Key 生成和鉴权机制">API Key 生成和鉴权机制</a></h4>
                    <p class="content">本文介绍一种基于数据库表的 API Key 实现方案。</p>
                    <div class="blog-list-footer">
                      <span>2026.06.01</span>
                      <span>阅读量 171</span>
                      <span>点赞 4</span>
                      <span>评论 0</span>
                      <span>收藏 5</span>
                    </div>
                  </div>
                </body></html>
                """), 20);

        assertEquals(1, articles.size());
        CsdnArticleVO article = articles.get(0);
        assertEquals("123456", article.getId());
        assertEquals("API Key 生成和鉴权机制", article.getTitle());
        assertEquals("本文介绍一种基于数据库表的 API Key 实现方案。", article.getDescription());
        assertEquals("2026.06.01", article.getPostTime());
        assertEquals(171L, article.getViewCount());
        assertEquals(4L, article.getLikeCount());
        assertEquals(0L, article.getCommentCount());
        assertEquals(5L, article.getCollectCount());
    }

    @Test
    void shouldParsePureTitleWhenArticleLinkWrapsWholeCard() {
        CsdnServiceImpl service = new CsdnServiceImpl();

        List<CsdnArticleVO> articles = service.parseArticles(Jsoup.parse("""
                <html><body>
                  <div class="article-item-box">
                    <a href="https://blog.csdn.net/lys1313013/article/details/789">
                      <h4 class="blog-list-title">记录这段在 CSDN 写作的日子</h4>
                      <p class="content">平时写文章的时候，其实没有太强的仪式感。</p>
                      <span>2026.06.09</span>
                      <span>阅读 234</span>
                      <span>点赞 3</span>
                      <span>评论 0</span>
                      <span>收藏 3</span>
                    </a>
                  </div>
                </body></html>
                """), 20);

        assertEquals(1, articles.size());
        CsdnArticleVO article = articles.get(0);
        assertEquals("记录这段在 CSDN 写作的日子", article.getTitle());
        assertEquals("平时写文章的时候，其实没有太强的仪式感。", article.getDescription());
    }

    @Test
    void shouldParseArticleCountersByCsdnFooterOrderWhenLabelsAreMissing() {
        CsdnServiceImpl service = new CsdnServiceImpl();

        List<CsdnArticleVO> articles = service.parseArticles(Jsoup.parse("""
                <html><body>
                  <div class="article-item-box">
                    <h4><a href="https://blog.csdn.net/lys1313013/article/details/456">CSDN 统计顺序测试</a></h4>
                    <p class="content">测试无文字标签时的文章统计解析。</p>
                    <div class="blog-list-footer">
                      <span>2026.06.09</span>
                      <span><i class="view"></i>3</span>
                      <span><i class="comment"></i>0</span>
                      <span><i class="like"></i>3</span>
                      <span><i class="collect"></i>3</span>
                    </div>
                  </div>
                </body></html>
                """), 20);

        assertEquals(1, articles.size());
        CsdnArticleVO article = articles.get(0);
        assertEquals(3L, article.getViewCount());
        assertEquals(0L, article.getCommentCount());
        assertEquals(3L, article.getLikeCount());
        assertEquals(3L, article.getCollectCount());
    }
}
