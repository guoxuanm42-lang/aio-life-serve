package top.aiolife.sso.convertor;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import top.aiolife.sso.pojo.entity.ApiKeyEntity;
import top.aiolife.sso.pojo.vo.ApiKeyVO;
import top.aiolife.sso.util.ApiKeyMaskUtil;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API Key 对象转换器，负责将持久化对象转换为脱敏展示对象。
 *
 * @author Ethan
 * @date 2026-07-14
 */
@Mapper(builder = @Builder(disableBuilder = true))
public interface ApiKeyConvertor {

    ApiKeyConvertor INSTANCE = Mappers.getMapper(ApiKeyConvertor.class);

    /**
     * 将 API Key 实体转换为脱敏展示对象。
     *
     * @param entity API Key 实体
     * @return 包含脱敏 Key 和过期状态的展示对象
     *
     * @author Ethan
     * @date 2026-07-14
     */
    @Mapping(target = "apiKey", source = "apiKey", qualifiedByName = "maskApiKey")
    @Mapping(target = "isExpired", source = "expiredAt", qualifiedByName = "checkExpired")
    ApiKeyVO entity2VO(ApiKeyEntity entity);

    /**
     * 批量将 API Key 实体转换为脱敏展示对象。
     *
     * @param entities API Key 实体列表
     * @return 脱敏后的 API Key 展示对象列表
     *
     * @author Ethan
     * @date 2026-07-14
     */
    List<ApiKeyVO> entityList2VOList(List<ApiKeyEntity> entities);

    /**
     * 脱敏 API Key。
     *
     * @param apiKey 待脱敏的 API Key
     * @return 脱敏后的 API Key
     *
     * @author Ethan
     * @date 2026-07-14
     */
    @Named("maskApiKey")
    default String maskApiKey(String apiKey) {
        return ApiKeyMaskUtil.mask(apiKey);
    }

    /**
     * 检查 API Key 是否过期。
     *
     * @param expiredAt API Key 过期时间
     * @return 已过期返回 true，未过期或永久有效返回 false
     *
     * @author Ethan
     * @date 2026-07-14
     */
    @Named("checkExpired")
    default Boolean checkExpired(LocalDateTime expiredAt) {
        if (expiredAt == null) {
            return false;
        }
        return expiredAt.isBefore(LocalDateTime.now());
    }
}
