package top.aiolife.record.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import top.aiolife.record.pojo.csdn.CsdnArticleVO;
import top.aiolife.record.pojo.csdn.CsdnStatsVO;
import top.aiolife.record.service.ICsdnService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CSDN 编程看板数据服务实现。
 *
 * @author Ethan
 * @date 2026-06-09
 */
@Slf4j
@Service
public class CsdnServiceImpl implements ICsdnService {

    private static final String CSDN_BLOG_URL = "https://blog.csdn.net/%s";

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36";

    private static final Pattern ARTICLE_ID_PATTERN = Pattern.compile("/article/details/(\\d+)");

    private static final Pattern NUMBER_WITH_UNIT_PATTERN = Pattern.compile("([0-9]+(?:,[0-9]{3})*(?:\\.[0-9]+)?|[0-9]+(?:\\.[0-9]+)?)(万?)");

    /**
     * 获取 CSDN 用户主页统计数据。
     *
     * @param username CSDN 用户名
     * @return CSDN 用户统计数据
     *
     * @author Ethan
     * @date 2026-06-09
     */
    @Override
    public CsdnStatsVO getStats(String username) {
        if (isBlank(username)) {
            return new CsdnStatsVO();
        }
        try {
            Document document = fetchHomeDocument(username);
            return parseStats(document);
        } catch (Exception exception) {
            log.warn("获取 CSDN 用户统计失败，username={}", username, exception);
            return new CsdnStatsVO();
        }
    }

    /**
     * 获取 CSDN 用户近期文章列表。
     *
     * @param username CSDN 用户名
     * @param limit 最大返回文章数量
     * @return CSDN 近期文章列表
     *
     * @author Ethan
     * @date 2026-06-09
     */
    @Override
    public List<CsdnArticleVO> getArticles(String username, Integer limit) {
        if (isBlank(username)) {
            return List.of();
        }
        try {
            Document document = fetchHomeDocument(username);
            return parseArticles(document, normalizeLimit(limit));
        } catch (Exception exception) {
            log.warn("获取 CSDN 文章列表失败，username={}", username, exception);
            return List.of();
        }
    }

    CsdnStatsVO parseStats(Document document) {
        CsdnStatsVO stats = new CsdnStatsVO();
        Map<String, Long> values = new LinkedHashMap<>();

        Elements statNums = document.select(".user-profile-statistics-num");
        Elements statNames = document.select(".user-profile-statistics-name");
        for (int i = 0; i < Math.min(statNums.size(), statNames.size()); i++) {
            String name = normalizeText(statNames.get(i).text());
            Long value = firstNumber(statNums.get(i).text()).orElse(0L);
            putStatValue(values, name, value);
        }

        for (Element item : document.select(".aside-common-box-content-text")) {
            String text = normalizeText(item.text());
            firstNumber(text).ifPresent(number -> putStatValue(values, text, number));
        }

        for (Element item : document.select(".user-profile-statistics li, .user-profile-statistics .item, .data-info dl, .data-info div")) {
            String text = normalizeText(item.text());
            if (text.isEmpty()) {
                continue;
            }
            firstNumber(text).ifPresent(number -> putStatValue(values, text, number));
        }

        String allText = normalizeText(document.body() == null ? document.text() : document.body().text());
        stats.setViewCount(firstStatValue(values, "view", allText, "总访问量", "访问量", "访问"));
        stats.setOriginalCount(firstStatValue(values, "original", allText, "原创", "原创数"));
        stats.setRank(firstStatValue(values, "rank", allText, "全站排名", "排名"));
        stats.setFansCount(firstStatValue(values, "fans", allText, "粉丝数", "粉丝"));
        stats.setLikeCount(firstStatValue(values, "like", allText, "获赞数", "获赞", "点赞", "赞"));
        stats.setCommentCount(firstStatValue(values, "comment", allText, "评论数", "评论"));
        return stats;
    }

    List<CsdnArticleVO> parseArticles(Document document, int limit) {
        List<CsdnArticleVO> articles = new ArrayList<>();
        Elements candidates = document.select(".article-item-box, .blog-list-box, .community-list, article, .article-list .list-item");
        if (candidates.isEmpty()) {
            candidates = document.select("a[href*=/article/details/]");
        }

        for (Element candidate : candidates) {
            if (articles.size() >= limit) {
                break;
            }

            CsdnArticleVO article = parseArticle(candidate);
            if (article != null && articles.stream().noneMatch(item -> article.getUrl().equals(item.getUrl()))) {
                articles.add(article);
            }
        }
        return articles;
    }

    private CsdnArticleVO parseArticle(Element element) {
        Element link = findArticleLink(element);
        if (link == null) {
            return null;
        }

        String url = link.absUrl("href");
        if (isBlank(url)) {
            url = link.attr("href");
        }
        if (isBlank(url) || !url.contains("/article/details/")) {
            return null;
        }

        String title = firstNotBlank(extractTitleText(element), link.attr("title"));
        if (isBlank(title)) {
            return null;
        }

        String text = normalizeText(element.text());
        CsdnArticleVO article = new CsdnArticleVO();
        article.setId(extractArticleId(url));
        article.setTitle(cleanTitle(title));
        article.setUrl(url);
        article.setDescription(extractDescription(element));
        article.setPostTime(firstNotBlank(
                extractDateText(text),
                element.select(".date, .time, .article-time, .view-time-box").text()
        ));
        ArticleCounters counters = parseArticleCounters(element);
        article.setViewCount(counters.viewCount());
        article.setCommentCount(counters.commentCount());
        article.setLikeCount(counters.likeCount());
        article.setCollectCount(counters.collectCount());
        return article;
    }

    private Document fetchHomeDocument(String username) throws IOException {
        String url = String.format(CSDN_BLOG_URL, normalizeUsername(username));
        return Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .referrer("https://www.csdn.net/")
                .timeout(10_000)
                .get();
    }

    private Element findArticleLink(Element element) {
        if (element.tagName().equals("a") && element.attr("href").contains("/article/details/")) {
            return element;
        }
        Element link = element.selectFirst("a[href*=/article/details/]");
        if (link != null) {
            return link;
        }
        return element.selectFirst("h4 a, h2 a, .title a, .article-title-box a");
    }

    private String extractTitleText(Element element) {
        Element titleElement = element.selectFirst("h4 a[href*=/article/details/], h2 a[href*=/article/details/], "
                + ".title a[href*=/article/details/], .article-title-box a[href*=/article/details/], "
                + ".blog-list-title a[href*=/article/details/], .article-title a[href*=/article/details/]");
        if (titleElement == null) {
            titleElement = element.selectFirst("h4, h2, .title, .article-title-box, .blog-list-title, .article-title");
        }
        if (titleElement != null) {
            return firstNotBlank(titleElement.attr("title"), titleElement.text());
        }
        return "";
    }

    private String extractArticleId(String url) {
        Matcher matcher = ARTICLE_ID_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return url;
    }

    private String extractDescription(Element element) {
        String description = firstNotBlank(
                element.select(".article-description, .content, .desc, .blog-list-content, .summary").text(),
                element.select("p").text()
        );
        return normalizeText(description);
    }

    private String extractDateText(String text) {
        Matcher matcher = Pattern.compile("(\\d{4}[-./]\\d{1,2}[-./]\\d{1,2}(?:\\s+\\d{1,2}:\\d{2})?)").matcher(text);
        if (matcher.find()) {
            return matcher.group(1).replace('/', '-');
        }
        return "";
    }

    private ArticleCounters parseArticleCounters(Element element) {
        Map<String, Long> counters = new LinkedHashMap<>();
        List<Long> unlabeledNumbers = new ArrayList<>();

        firstNumber(element.select(".view-num").text()).ifPresent(value -> counters.putIfAbsent("view", value));
        firstNumber(element.select(".give-like-num").text()).ifPresent(value -> counters.putIfAbsent("like", value));
        for (Element item : element.select(".comment-num")) {
            String itemText = normalizeText(item.text());
            firstNumber(itemText).ifPresent(number -> putArticleCounterValue(counters, unlabeledNumbers, itemText, number));
        }

        Elements counterItems = element.select(".blog-list-footer span, .blog-list-footer div, "
                + ".article-info-box span, .article-info-box div, .article-info span, .article-info div, "
                + ".article-bar span, .article-bar div, .info-box span, .info-box div, "
                + ".operating span, .operating div, .meta span, .meta div");

        for (Element item : counterItems) {
            String itemText = normalizeText(item.text());
            if (itemText.isEmpty() || isDateLikeText(itemText)) {
                continue;
            }
            firstNumber(itemText).ifPresent(number -> putArticleCounterValue(counters, unlabeledNumbers, itemText, number));
        }

        if (counterItems.isEmpty()) {
            Elements directItems = element.select("> span, > div");
            for (Element item : directItems) {
                String itemText = normalizeText(item.text());
                if (itemText.isEmpty() || isDateLikeText(itemText)) {
                    continue;
                }
                firstNumber(itemText).ifPresent(number -> putArticleCounterValue(counters, unlabeledNumbers, itemText, number));
            }
        }

        applyUnlabeledArticleCounters(counters, unlabeledNumbers);
        Long viewCount = counters.getOrDefault("view", 0L);
        Long commentCount = counters.getOrDefault("comment", 0L);
        Long likeCount = counters.getOrDefault("like", 0L);
        Long collectCount = counters.getOrDefault("collect", 0L);
        return new ArticleCounters(viewCount, commentCount, likeCount, collectCount);
    }

    private void putArticleCounterValue(Map<String, Long> counters, List<Long> unlabeledNumbers, String label, Long value) {
        if (label.contains("阅读") || label.contains("浏览")) {
            counters.putIfAbsent("view", value);
        } else if (label.contains("评论")) {
            counters.putIfAbsent("comment", value);
        } else if (label.contains("点赞") || label.contains("获赞") || label.contains("赞")) {
            counters.putIfAbsent("like", value);
        } else if (label.contains("收藏")) {
            counters.putIfAbsent("collect", value);
        } else {
            unlabeledNumbers.add(value);
        }
    }

    private void applyUnlabeledArticleCounters(Map<String, Long> counters, List<Long> numbers) {
        if (numbers.size() < 4) {
            return;
        }
        counters.putIfAbsent("view", numbers.get(0));
        counters.putIfAbsent("comment", numbers.get(1));
        counters.putIfAbsent("like", numbers.get(2));
        counters.putIfAbsent("collect", numbers.get(3));
    }

    private boolean isDateLikeText(String text) {
        return Pattern.compile("^\\d{4}[-./]\\d{1,2}[-./]\\d{1,2}").matcher(text).find()
                || text.contains("小时前")
                || text.contains("分钟前")
                || text.contains("刚刚");
    }

    private void putStatValue(Map<String, Long> values, String label, Long value) {
        if (label.contains("访问")) {
            values.putIfAbsent("view", value);
        } else if (label.contains("原创")) {
            values.putIfAbsent("original", value);
        } else if (label.contains("排名")) {
            values.putIfAbsent("rank", value);
        } else if (label.contains("粉丝")) {
            values.putIfAbsent("fans", value);
        } else if (label.contains("获赞") || label.contains("点赞") || label.contains("赞")) {
            values.putIfAbsent("like", value);
        } else if (label.contains("评论")) {
            values.putIfAbsent("comment", value);
        }
    }

    private Long firstStatValue(Map<String, Long> values, String key, String text, String... labels) {
        Long value = values.get(key);
        if (value != null) {
            return value;
        }
        return firstLabelNumber(text, labels).orElse(0L);
    }

    private Optional<Long> firstLabelNumber(String text, String... labels) {
        for (String label : labels) {
            Matcher matcher = Pattern.compile(Pattern.quote(label) + "\\D{0,12}" + NUMBER_WITH_UNIT_PATTERN.pattern()).matcher(text);
            if (matcher.find()) {
                return Optional.of(parseNumber(matcher.group(1), matcher.group(2)));
            }
            Matcher reverseMatcher = Pattern.compile(NUMBER_WITH_UNIT_PATTERN.pattern() + "\\D{0,12}" + Pattern.quote(label)).matcher(text);
            if (reverseMatcher.find()) {
                return Optional.of(parseNumber(reverseMatcher.group(1), reverseMatcher.group(2)));
            }
        }
        return Optional.empty();
    }

    private Optional<Long> firstNumber(String text) {
        Matcher matcher = NUMBER_WITH_UNIT_PATTERN.matcher(text);
        if (matcher.find()) {
            return Optional.of(parseNumber(matcher.group(1), matcher.group(2)));
        }
        return Optional.empty();
    }

    private Long parseNumber(String rawValue, String unit) {
        try {
            BigDecimal number = new BigDecimal(rawValue.replace(",", ""));
            if ("万".equals(unit)) {
                number = number.multiply(BigDecimal.valueOf(10_000));
            }
            return number.setScale(0, RoundingMode.HALF_UP).longValue();
        } catch (NumberFormatException exception) {
            return 0L;
        }
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return 20;
        }
        return Math.max(1, Math.min(limit, 50));
    }

    private String normalizeUsername(String username) {
        String value = username.trim();
        Matcher urlMatcher = Pattern.compile("blog\\.csdn\\.net/([^/?#]+)").matcher(value);
        if (urlMatcher.find()) {
            return urlMatcher.group(1);
        }
        int queryIndex = value.indexOf('?');
        if (queryIndex >= 0) {
            value = value.substring(0, queryIndex);
        }
        int fragmentIndex = value.indexOf('#');
        if (fragmentIndex >= 0) {
            value = value.substring(0, fragmentIndex);
        }
        String[] parts = value.replace('\\', '/').split("/");
        for (int i = parts.length - 1; i >= 0; i--) {
            if (!parts[i].isBlank()) {
                return parts[i].trim();
            }
        }
        return value.trim();
    }

    private String firstNotBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return normalizeText(value);
            }
        }
        return "";
    }

    private String cleanTitle(String title) {
        String cleanTitle = normalizeText(title).replaceFirst("^原创\\s*", "").trim();
        Matcher matcher = Pattern.compile("(.+?)(?:\\s+(?:\\d{4}[-./]\\d{1,2}[-./]\\d{1,2}|阅读量?|浏览量?|点赞|获赞|评论|收藏)\\b.*)?").matcher(cleanTitle);
        if (matcher.matches()) {
            return matcher.group(1).trim();
        }
        return cleanTitle;
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private record ArticleCounters(Long viewCount, Long commentCount, Long likeCount, Long collectCount) {
    }
}
