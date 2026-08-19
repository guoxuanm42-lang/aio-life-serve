package top.aiolife.ai.activity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 活动总结异步执行配置，为耗时的模型生成任务提供独立有界线程池。
 *
 * @author Ethan
 * @date 2026-08-16
 */
@Configuration
public class AiActivitySummaryAsyncConfig {

    /**
     * 创建活动总结专用的有界任务执行器，避免阻塞 MVC 请求线程。
     *
     * @return 活动总结生成任务执行器
     *
     * @author Ethan
     * @date 2026-08-16
     */
    @Bean(name = "activitySummaryTaskExecutor")
    public Executor activitySummaryTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("activity-summary-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }
}
