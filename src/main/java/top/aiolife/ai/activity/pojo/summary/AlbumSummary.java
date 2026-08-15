package top.aiolife.ai.activity.pojo.summary;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 相册活动总结统计，承载新建相册数量和名称。
 *
 * @author Ethan
 * @date 2026-08-12
 */
@Data
public class AlbumSummary {

    private long newFolderCount;

    private List<String> folderNames = new ArrayList<>();

    /**
     * 判断当前相册统计是否没有可输出数据。
     *
     * @return 没有新建相册时返回 true
     *
     * @author Ethan
     * @date 2026-08-12
     */
    public boolean isEmpty() {
        return newFolderCount == 0;
    }
}
