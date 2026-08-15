package top.aiolife.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus 分页与 Mapper 扫描配置。
 *
 * @author Ethan
 * @date 2026-08-14
 */
@Configuration
@MapperScan({"top.aiolife.*.mapper", "top.aiolife.ai.memory.mapper", "top.aiolife.ai.activity.mapper"})
public class MybatisPlusConfig {

  /**
   * 创建 MyBatis Plus 分页拦截器。
   *
   * @return MyBatis Plus 拦截器，包含 MySQL 分页插件
   *
   * @author Ethan
   * @date 2026-06-29
   */
  @Bean
  public MybatisPlusInterceptor mybatisPlusInterceptor() {
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
    // 如果配置多个插件, 切记分页最后添加
    interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
    // 如果有多数据源可以不配具体类型, 否则都建议配上具体的 DbType
    return interceptor;
  }
}
