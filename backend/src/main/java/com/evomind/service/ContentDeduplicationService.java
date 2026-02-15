package com.evomind.service;

import com.evomind.entity.CrawledContent;

import java.util.List;

/**
 * 内容去重服务接口
 */
public interface ContentDeduplicationService {

    /**
     * 计算内容哈希
     */
    String calculateContentHash(String content);

    /**
     * 检查是否重复
     */
    boolean isDuplicate(String contentHash);

    /**
     * 查找相似内容
     */
    List<CrawledContent> findSimilarContents(String content, double threshold);

    /**
     * 执行去重检查
     */
    DeduplicationResult deduplicate(CrawledContent content);

    /**
     * 清理过期去重记录
     */
    void cleanupOldRecords(int daysToKeep);

    class DeduplicationResult {
        private boolean isDuplicate;
        private Long duplicateOfId;
        private double similarityScore;

        public boolean isDuplicate() { return isDuplicate; }
        public void setDuplicate(boolean duplicate) { isDuplicate = duplicate; }
        public Long getDuplicateOfId() { return duplicateOfId; }
        public void setDuplicateOfId(Long duplicateOfId) { this.duplicateOfId = duplicateOfId; }
        public double getSimilarityScore() { return similarityScore; }
        public void setSimilarityScore(double similarityScore) { this.similarityScore = similarityScore; }
    }
}
