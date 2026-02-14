package com.evomind.service;

import com.evomind.entity.SourceContent;

import java.util.Optional;

public interface SourceContentService {

    /**
     * 获取原文内容
     * @param sourceId 来源ID
     * @return 原文内容
     */
    Optional<SourceContent> getContentBySourceId(Long sourceId);

    /**
     * 获取原文段落
     * @param contentId 内容ID
     * @param paragraphIndex 段落索引
     * @return 段落文本
     */
    String getParagraph(Long contentId, Integer paragraphIndex);

    /**
     * 保存原文内容
     * @param sourceId 来源ID
     * @param content 内容文本
     * @param title 标题
     * @param author 作者
     * @return 保存的内容
     */
    SourceContent saveContent(Long sourceId, String content, String title, String author);

    /**
     * 保存原文内容（完整版）
     * @param userId 用户ID
     * @param sourceId 来源ID
     * @param title 标题
     * @param content 内容文本
     * @param sourceUrl 来源URL
     * @param contentType 内容类型
     * @return 保存的内容
     */
    SourceContent saveContent(Long userId, Long sourceId, String title, String content,
                              String sourceUrl, String contentType);

    /**
     * 更新内容抓取状态
     * @param contentId 内容ID
     * @param status 状态
     * @param error 错误信息
     */
    void updateFetchStatus(Long contentId, String status, String error);

    /**
     * 检查内容是否重复
     * @param contentHash 内容哈希
     * @return 是否重复
     */
    boolean isDuplicate(String contentHash);

    /**
     * 删除原文内容
     * @param sourceId 来源ID
     */
    void deleteBySourceId(Long sourceId);
}
