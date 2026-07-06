package top.aiolife.record.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.aiolife.record.pojo.entity.PhotoImageEntity;

/**
 * 相册图片 Mapper，负责图片元数据的持久化。
 *
 * @author Ethan
 * @date 2026-07-02
 */
@Mapper
public interface IPhotoImageMapper extends BaseMapper<PhotoImageEntity> {
}
