package com.evomind.repository;

import com.evomind.entity.SensitiveWord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 敏感词库数据访问层
 */
@Repository
public interface SensitiveWordRepository extends JpaRepository<SensitiveWord, Long> {

    /**
     * 根据敏感词内容精确查询
     */
    Optional<SensitiveWord> findByWord(String word);

    /**
     * 检查敏感词是否存在
     */
    boolean existsByWord(String word);

    /**
     * 查询所有启用的敏感词
     */
    @Query("SELECT s FROM SensitiveWord s WHERE s.enabled = true ORDER BY s.level DESC")
    List<SensitiveWord> findAllEnabled();

    /**
     * 根据分类查询敏感词
     */
    List<SensitiveWord> findByCategoryAndEnabledTrueOrderByLevelDesc(SensitiveWord.WordCategory category);

    /**
     * 根据敏感级别查询
     */
    List<SensitiveWord> findByLevelAndEnabledTrueOrderByHitCountDesc(SensitiveWord.SensitiveLevel level);

    /**
     * 分页查询敏感词
     */
    Page<SensitiveWord> findByEnabledTrueOrderByHitCountDesc(Pageable pageable);

    /**
     * 模糊搜索敏感词
     */
    @Query("SELECT s FROM SensitiveWord s WHERE s.word LIKE %:keyword% OR s.description LIKE %:keyword%")
    Page<SensitiveWord> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据分类统计数量
     */
    @Query("SELECT s.category, COUNT(s) FROM SensitiveWord s WHERE s.enabled = true GROUP BY s.category")
    List<Object[]> countByCategory();

    /**
     * 根据敏感级别统计数量
     */
    @Query("SELECT s.level, COUNT(s) FROM SensitiveWord s WHERE s.enabled = true GROUP BY s.level")
    List<Object[]> countByLevel();

    /**
     * 获取热点敏感词（命中次数最多）
     */
    List<SensitiveWord> findTop20ByEnabledTrueOrderByHitCountDesc();

    /**
     * 根据来源查询
     */
    List<SensitiveWord> findBySourceOrderByCreatedAtDesc(SensitiveWord.WordSource source);

    /**
     * 批量更新命中次数
     */
    @Modifying
    @Query("UPDATE SensitiveWord s SET s.hitCount = s.hitCount + 1, s.lastHitAt = CURRENT_TIMESTAMP WHERE s.id = :id")
    void incrementHitCount(@Param("id") Long id);

    /**
     * 批量启用/禁用
     */
    @Modifying
    @Query("UPDATE SensitiveWord s SET s.enabled = :enabled WHERE s.category = :category")
    int updateEnabledByCategory(@Param("category") SensitiveWord.WordCategory category, 
                                @Param("enabled") boolean enabled);

    /**
     * 查询系统预设的敏感词
     */
    List<SensitiveWord> findBySourceAndEnabledTrue(SensitiveWord.WordSource source);

    /**
     * 删除长期未命中的敏感词（清理冷词）
     */
    @Modifying
    @Query("DELETE FROM SensitiveWord s WHERE s.hitCount = 0 AND s.source = 'MANUAL' " +
           "AND s.createdAt < :before")
    int deleteUnusedWords(@Param("before") java.time.LocalDateTime before);

    /**
     * 统计总数
     */
    long countByEnabledTrue();
}