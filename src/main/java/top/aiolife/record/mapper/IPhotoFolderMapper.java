package top.aiolife.record.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.aiolife.record.pojo.entity.PhotoFolderEntity;

/**
 * 相册文件夹 Mapper，负责相册目录结构的持久化。
 *
 * @author Ethan
 * @date 2026-07-02
 */
@Mapper
public interface IPhotoFolderMapper extends BaseMapper<PhotoFolderEntity> {
}
