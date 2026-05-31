package top.aiolife.mcp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MCP 工具方法暴露配置。
 *
 * @author Ethan
 * @date 2026-05-31
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface McpOperation {

    /**
     * 工具名称。
     */
    String name() default "";

    /**
     * 工具说明。
     */
    String description() default "";

    /**
     * 需要从输入 schema 中排除的字段路径。
     */
    String[] ignoreInputFields() default {};

    /**
     * 是否解包 ApiResponse.data。
     */
    boolean unwrapApiResponseData() default true;
}
