package top.aiolife.record.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.aiolife.record.pojo.entity.ArticleEntity;

/**
 * 文章 Mapper，提供文章表的基础数据访问能力。
 *
 * @author Ethan
 * @date 2026-06-24
 */
@Mapper
public interface IArticleMapper extends BaseMapper<ArticleEntity> {
}
