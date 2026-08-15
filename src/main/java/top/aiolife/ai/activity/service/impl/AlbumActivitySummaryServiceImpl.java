package top.aiolife.ai.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.aiolife.ai.activity.model.AiActivityDateRange;
import top.aiolife.ai.activity.pojo.summary.AlbumSummary;
import top.aiolife.ai.activity.service.AlbumActivitySummaryService;
import top.aiolife.core.constant.StatusConst;
import top.aiolife.record.mapper.IPhotoFolderMapper;
import top.aiolife.record.pojo.entity.PhotoFolderEntity;

import java.util.List;

/**
 * 相册活动统计服务实现，通过相册文件夹数据构建新建数量和名称明细。
 *
 * @author Ethan
 * @date 2026-08-13
 */
@Service
@RequiredArgsConstructor
public class AlbumActivitySummaryServiceImpl implements AlbumActivitySummaryService {

    private final IPhotoFolderMapper photoFolderMapper;

    /**
     * 汇总指定用户在活动周期内新建的相册文件夹。
     *
     * @param userId 当前用户 ID
     * @param range 活动统计时间范围
     * @return 相册活动统计结果
     * @throws IllegalArgumentException 用户或时间范围无效时抛出
     *
     * @author Ethan
     * @date 2026-08-13
     */
    @Override
    public AlbumSummary summarize(Long userId, AiActivityDateRange range) {
        ActivitySummarySupport.validate(userId, range);
        LambdaQueryWrapper<PhotoFolderEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(PhotoFolderEntity::getName, PhotoFolderEntity::getCreateTime);
        wrapper.eq(PhotoFolderEntity::getUserId, userId);
        wrapper.eq(PhotoFolderEntity::getIsDeleted, StatusConst.NO_DELETE);
        wrapper.ge(PhotoFolderEntity::getCreateTime, range.getStartTime());
        wrapper.lt(PhotoFolderEntity::getCreateTime, range.getEndTime());
        wrapper.orderByDesc(PhotoFolderEntity::getCreateTime);
        List<PhotoFolderEntity> records = photoFolderMapper.selectList(wrapper);

        AlbumSummary summary = new AlbumSummary();
        summary.setNewFolderCount(records.size());
        summary.setFolderNames(ActivitySummarySupport.detailTexts(records, PhotoFolderEntity::getName));
        return summary;
    }
}
