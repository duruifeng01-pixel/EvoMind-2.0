package com.evomind.service;

import com.evomind.entity.UserCorpus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 用户语料库服务接口
 */
public interface UserCorpusService {

    /**
     * 创建语料
     */
    UserCorpus createCorpus(Long userId, String title, String content, UserCorpus.CorpusType corpusType);

    /**
     * 从苏格拉底式对话创建洞察语料
     */
    UserCorpus createFromSocraticDialogue(Long userId, Long dialogueId, Long discussionId,
                                          String title, String insight, String dialogueSummary);

    /**
     * 根据ID获取语料
     */
    Optional<UserCorpus> getById(Long id);

    /**
     * 获取用户的语料列表（默认未归档）
     */
    Page<UserCorpus> getUserCorpus(Long userId, Pageable pageable);

    /**
     * 获取用户归档的语料
     */
    Page<UserCorpus> getArchivedCorpus(Long userId, Pageable pageable);

    /**
     * 获取用户收藏的语料
     */
    Page<UserCorpus> getFavoriteCorpus(Long userId, Pageable pageable);

    /**
     * 按类型获取语料
     */
    Page<UserCorpus> getCorpusByType(Long userId, UserCorpus.CorpusType corpusType, Pageable pageable);

    /**
     * 获取置顶的语料
     */
    List<UserCorpus> getPinnedCorpus(Long userId);

    /**
     * 搜索语料
     */
    Page<UserCorpus> searchCorpus(Long userId, String keyword, Pageable pageable);

    /**
     * 更新语料
     */
    UserCorpus updateCorpus(Long userId, Long corpusId, String title, String content);

    /**
     * 删除语料
     */
    void deleteCorpus(Long userId, Long corpusId);

    /**
     * 切换收藏状态
     */
    UserCorpus toggleFavorite(Long userId, Long corpusId);

    /**
     * 切换置顶状态
     */
    UserCorpus togglePin(Long userId, Long corpusId);

    /**
     * 归档语料
     */
    UserCorpus archiveCorpus(Long userId, Long corpusId);

    /**
     * 取消归档
     */
    UserCorpus unarchiveCorpus(Long userId, Long corpusId);

    /**
     * 记录查看
     */
    void recordView(Long corpusId);

    /**
     * 统计用户语料数量
     */
    long countUserCorpus(Long userId);

    /**
     * 统计用户某类型语料数量
     */
    long countUserCorpusByType(Long userId, UserCorpus.CorpusType corpusType);

    /**
     * 检查是否存在来自特定源的语料
     */
    boolean existsBySource(Long userId, Long sourceId, UserCorpus.SourceType sourceType);

    /**
     * 根据讨论ID获取相关洞察
     */
    List<UserCorpus> getInsightsByDiscussion(Long userId, Long discussionId);

    /**
     * 获取用户的最新语料
     */
    List<UserCorpus> getRecentCorpus(Long userId, int limit);
}
