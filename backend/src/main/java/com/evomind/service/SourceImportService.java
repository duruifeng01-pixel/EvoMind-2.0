package com.evomind.service;

import com.evomind.dto.request.ConfirmImportRequest;
import com.evomind.dto.request.LinkImportRequest;
import com.evomind.dto.request.OcrImportRequest;
import com.evomind.dto.response.SourceImportJobResponse;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 信息源导入服务接口
 * 支持OCR截图识别和链接抓取两种方式导入信息源
 */
public interface SourceImportService {

    /**
     * 导入成功的信息源结果
     */
    @Data
    class ImportResult {
        private Long sourceId;
        private String name;
        private String platform;
        private Boolean existed;
    }

    /**
     * 提交OCR截图识别任务
     *
     * @param userId        用户ID
     * @param ocrRequest    OCR请求
     * @return 创建的任务响应
     */
    SourceImportJobResponse submitOcrTask(Long userId, OcrImportRequest ocrRequest);

    /**
     * 提交链接抓取任务
     *
     * @param userId        用户ID
     * @param linkRequest   链接请求
     * @return 创建的任务响应
     */
    SourceImportJobResponse submitLinkTask(Long userId, LinkImportRequest linkRequest);

    /**
     * 获取任务状态
     *
     * @param userId 用户ID
     * @param jobId  任务ID
     * @return 任务响应
     */
    SourceImportJobResponse getJobStatus(Long userId, Long jobId);

    /**
     * 获取用户的导入任务列表
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 任务列表
     */
    Page<SourceImportJobResponse> getUserJobs(Long userId, Pageable pageable);

    /**
     * 确认导入选中的作者
     *
     * @param userId          用户ID
     * @param confirmRequest  确认请求
     * @return 导入结果
     */
    List<ImportResult> confirmImport(Long userId, ConfirmImportRequest confirmRequest);

    /**
     * 取消导入任务
     *
     * @param userId 用户ID
     * @param jobId  任务ID
     */
    void cancelJob(Long userId, Long jobId);

    /**
     * 重新执行失败的任务
     *
     * @param userId 用户ID
     * @param jobId  任务ID
     * @return 更新后的任务
     */
    SourceImportJobResponse retryJob(Long userId, Long jobId);

    /**
     * 检查用户今日导入次数是否超限
     *
     * @param userId    用户ID
     * @param maxLimit  最大限制
     * @return true表示已超限
     */
    boolean isDailyLimitExceeded(Long userId, int maxLimit);
}
