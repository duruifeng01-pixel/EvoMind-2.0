package com.evomind.service.impl;

import com.evomind.entity.CrawledContent;
import com.evomind.repository.CrawledContentRepository;
import com.evomind.service.ContentDeduplicationService;
import com.evomind.service.TextSimilarityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 内容去重服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentDeduplicationServiceImpl implements ContentDeduplicationService {

    private final CrawledContentRepository crawledContentRepository;
    private final TextSimilarityService textSimilarityService;

    @Override
    public String calculateContentHash(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        try {
            // 标准化内容：去除空白、转小写
            String normalized = content.replaceAll("\\s+", "").toLowerCase();

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalized.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to calculate content hash", e);
            return String.valueOf(content.hashCode());
        }
    }

    @Override
    public boolean isDuplicate(String contentHash) {
        if (contentHash == null || contentHash.isEmpty()) return false;
        return crawledContentRepository.existsByContentHash(contentHash);
    }

    @Override
    public List<CrawledContent> findSimilarContents(String content, double threshold) {
        // 获取最近的内容进行相似度比较
        List<CrawledContent> recentContents = crawledContentRepository
                .findByStatusAndCrawledAtBefore(
                        CrawledContent.ContentStatus.COMPLETED,
                        LocalDateTime.now().minusDays(30)
                );

        return recentContents.stream()
                .filter(c -> c.getContent() != null)
                .filter(c -> {
                    double similarity = textSimilarityService.calculateSimilarity(
                            content, c.getContent());
                    return similarity >= threshold;
                })
                .toList();
    }

    @Override
    public DeduplicationResult deduplicate(CrawledContent content) {
        DeduplicationResult result = new DeduplicationResult();

        // 1. 精确匹配（哈希）
        String hash = calculateContentHash(content.getContent());
        content.setContentHash(hash);

        Optional<CrawledContent> exactDuplicate = crawledContentRepository
                .findByContentHash(hash);

        if (exactDuplicate.isPresent()) {
            result.setDuplicate(true);
            result.setDuplicateOfId(exactDuplicate.get().getId());
            result.setSimilarityScore(1.0);
            return result;
        }

        // 2. 相似度匹配
        List<CrawledContent> similarContents = findSimilarContents(content.getContent(), 0.85);

        if (!similarContents.isEmpty()) {
            CrawledContent mostSimilar = similarContents.get(0);
            double similarity = textSimilarityService.calculateSimilarity(
                    content.getContent(), mostSimilar.getContent());

            result.setDuplicate(true);
            result.setDuplicateOfId(mostSimilar.getId());
            result.setSimilarityScore(similarity);
            return result;
        }

        result.setDuplicate(false);
        result.setSimilarityScore(0.0);
        return result;
    }

    @Override
    public void cleanupOldRecords(int daysToKeep) {
        LocalDateTime before = LocalDateTime.now().minusDays(daysToKeep);
        List<CrawledContent> oldRecords = crawledContentRepository
                .findByStatusAndCrawledAtBefore(CrawledContent.ContentStatus.RAW, before);

        crawledContentRepository.deleteAll(oldRecords);
        log.info("Cleaned up {} old crawl records", oldRecords.size());
    }
}
