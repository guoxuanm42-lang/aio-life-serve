package top.aiolife.record.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.aiolife.record.pojo.entity.ArticleCategoryEntity;

/**
 * 文章分类 Mapper，提供文章分类表的基础数据访问能力。
 *
 * @author Ethan
 * @date 2026-06-24
 */
@Mapper
public interface IArticleCategoryMapper extends BaseMapper<ArticleCategoryEntity> {
}
