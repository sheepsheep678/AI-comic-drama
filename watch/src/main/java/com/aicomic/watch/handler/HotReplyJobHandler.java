package com.aicomic.watch.handler;

import com.aicomic.watch.service.IHotReplyService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 热门二级评论 Top4 刷新与回填任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HotReplyJobHandler {

    private final IHotReplyService hotReplyService;

    /**
     * 增量刷新：消费 Redis 脏集合，更新 DB 中的热门二级评论字段
     */
    @XxlJob("hotReplyRefreshJob")
    public void refreshDirty() {
        try {
            hotReplyService.refreshDirty();
            XxlJobHelper.handleSuccess("热门二级评论刷新完成");
        } catch (Exception e) {
            log.error("热门二级评论刷新失败", e);
            XxlJobHelper.handleFail("热门二级评论刷新失败：" + e.getMessage());
        }
    }

    /**
     * 全量回填：jobParam 传起始一级评论 id，默认 0
     */
    @XxlJob("hotReplyBackfillJob")
    public void backfill() {
        try {
            long startId = parseStartId(XxlJobHelper.getJobParam());
            Long lastId = hotReplyService.backfill(startId, 200);
            XxlJobHelper.handleSuccess("热门二级评论回填完成，最后一级评论id=" + lastId);
        } catch (Exception e) {
            log.error("热门二级评论回填失败", e);
            XxlJobHelper.handleFail("热门二级评论回填失败：" + e.getMessage());
        }
    }

    private long parseStartId(String param) {
        if (param == null || param.trim().isEmpty()) {
            return 0L;
        }
        try {
            return Math.max(Long.parseLong(param.trim()), 0L);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
