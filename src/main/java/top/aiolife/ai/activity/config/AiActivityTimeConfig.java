package top.aiolife.ai.activity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * AI 活动统计时间配置，统一提供上海时区时钟。
 *
 * @author Ethan
 * @date 2026-08-12
 */
@Configuration
public class AiActivityTimeConfig {

    /**
     * 创建 AI 活动统计使用的系统时钟。
     *
     * @return 使用 Asia/Shanghai 时区的系统时钟
     *
     * @author Ethan
     * @date 2026-08-12
     */
    @Bean("aiActivityClock")
    public Clock aiActivityClock() {
        return Clock.system(ZoneId.of("Asia/Shanghai"));
    }
}
