package com.evomind.repository;

import com.evomind.entity.UserCorpus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户语料库数据访问层
 */
@Repository
public interface UserCorpusRepository extends JpaRepository<UserCorpus, Long> {

    /**
     * 根据用户ID分页查询语料
     */
    Page<UserCorpus> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 查询用户未归档的语料
     */
    Page<UserCorpus> findByUserIdAndIsArchivedFalseOrderByIsPinnedDescCreatedAtDesc(
            Long userId, Pageable pageable);

    /**
     * 查询用户已归档的语料
     */
    Page<UserCorpus> findByUserIdAndIsArchivedTrueOrderByArchivedAtDesc(
            Long userId, Pageable pageable);

    /**
     * 查询用户收藏的语料
     */
    Page<UserCorpus> findByUserIdAndIsFavoriteTrueOrderByCreatedAtDesc(
            Long userId, Pageable pageable);

    /**
     * 根据语料类型查询
     */
    Page<UserCorpus> findByUserIdAndCorpusTypeOrderByCreatedAtDesc(
            Long userId, UserCorpus.CorpusType corpusType, Pageable pageable);

    /**
     * 查询置顶的语料
     */
    List<UserCorpus> findByUserIdAndIsPinnedTrueOrderByPinnedAtDesc(Long userId);

    /**
     * 根据来源ID和来源类型查询
     */
    Optional<UserCorpus> findByUserIdAndSourceIdAndSourceType(
            Long userId, Long sourceId, UserCorpus.SourceType sourceType);

    /**
     * 检查是否存在
     */
    boolean existsByUserIdAndSourceIdAndSourceType(
            Long userId, Long sourceId, UserCorpus.SourceType sourceType);

    /**
     * 搜索语料内容
     */
    @Query("SELECT uc FROM UserCorpus uc WHERE uc.userId = :userId AND " +
           "(uc.title LIKE %:keyword% OR uc.contentText LIKE %:keyword% OR uc.keywords LIKE %:keyword%)")
    Page<UserCorpus> searchByUserIdAndKeyword(@Param("userId") Long userId,
                                               @Param("keyword") String keyword,
                                               Pageable pageable);

    /**
     * 统计用户语料数量
     */
    long countByUserId(Long userId);

    /**
     * 统计用户各类语料数量
     */
    long countByUserIdAndCorpusType(Long userId, UserCorpus.CorpusType corpusType);

    /**
     * 更新查看次数
     */
    @Modifying
    @Query("UPDATE UserCorpus uc SET uc.viewCount = uc.viewCount + 1, uc.lastViewedAt = :viewTime " +
           "WHERE uc.id = :id")
    void incrementViewCount(@Param("id") Long id, @Param("viewTime") LocalDateTime viewTime);

    /**
     * 根据讨论ID查询相关洞察
     */
    List<UserCorpus> findByUserIdAndDiscussionIdAndCorpusType(
            Long userId, Long discussionId, UserCorpus.CorpusType corpusType);

    /**
     * 查询最近创建的语料
     */
    List<UserCorpus> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
}
