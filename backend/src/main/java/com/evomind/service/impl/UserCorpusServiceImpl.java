package com.evomind.service.impl;

import com.evomind.entity.UserCorpus;
import com.evomind.exception.BusinessException;
import com.evomind.exception.ResourceNotFoundException;
import com.evomind.repository.UserCorpusRepository;
import com.evomind.service.UserCorpusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户语料库服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCorpusServiceImpl implements UserCorpusService {

    private final UserCorpusRepository userCorpusRepository;

    @Override
    @Transactional
    public UserCorpus createCorpus(Long userId, String title, String content, UserCorpus.CorpusType corpusType) {
        UserCorpus corpus = new UserCorpus();
        corpus.setUserId(userId);
        corpus.setTitle(title);
        corpus.setContentText(content);
        corpus.setCorpusType(corpusType);
        corpus.setSummaryText(generateSummary(content));
        corpus.setOneSentenceSummary(generateOneSentenceSummary(content));
        
        UserCorpus saved = userCorpusRepository.save(corpus);
        log.info("创建语料成功: userId={}, corpusId={}, type={}", userId, saved.getId(), corpusType);
        return saved;
    }

    @Override
    @Transactional
    public UserCorpus createFromSocraticDialogue(Long userId, Long dialogueId, Long discussionId,
                                                  String title, String insight, String dialogueSummary) {
        // 检查是否已存在
        Optional<UserCorpus> existing = userCorpusRepository.findByUserIdAndSourceIdAndSourceType(
                userId, dialogueId, UserCorpus.SourceType.SOCRATIC_DIALOGUE);
        
        if (existing.isPresent()) {
            log.info("苏格拉底对话洞察已存在: userId={}, dialogueId={}", userId, dialogueId);
            return existing.get();
        }

        UserCorpus corpus = new UserCorpus();
        corpus.setUserId(userId);
        corpus.setTitle(title);
        corpus.setContentText(buildSocraticContent(insight, dialogueSummary));
        corpus.setSummaryText(insight);
        corpus.setOneSentenceSummary(generateOneSentenceSummary(insight));
        corpus.setCorpusType(UserCorpus.CorpusType.SOCRATIC_INSIGHT);
        corpus.setSourceType(UserCorpus.SourceType.SOCRATIC_DIALOGUE);
        corpus.setSourceId(dialogueId);
        corpus.setSourceRef("/socratic/dialogues/" + dialogueId);
        corpus.setDiscussionId(discussionId);
        corpus.setKeywords("苏格拉底对话,深度思考,认知洞察");
        corpus.setReadingTimeMinutes(calculateReadingTime(insight));

        UserCorpus saved = userCorpusRepository.save(corpus);
        log.info("苏格拉底对话洞察保存到语料库: userId={}, corpusId={}, dialogueId={}", 
                userId, saved.getId(), dialogueId);
        return saved;
    }

    @Override
    public Optional<UserCorpus> getById(Long id) {
        return userCorpusRepository.findById(id);
    }

    @Override
    public Page<UserCorpus> getUserCorpus(Long userId, Pageable pageable) {
        return userCorpusRepository.findByUserIdAndIsArchivedFalseOrderByIsPinnedDescCreatedAtDesc(userId, pageable);
    }

    @Override
    public Page<UserCorpus> getArchivedCorpus(Long userId, Pageable pageable) {
        return userCorpusRepository.findByUserIdAndIsArchivedTrueOrderByArchivedAtDesc(userId, pageable);
    }

    @Override
    public Page<UserCorpus> getFavoriteCorpus(Long userId, Pageable pageable) {
        return userCorpusRepository.findByUserIdAndIsFavoriteTrueOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public Page<UserCorpus> getCorpusByType(Long userId, UserCorpus.CorpusType corpusType, Pageable pageable) {
        return userCorpusRepository.findByUserIdAndCorpusTypeOrderByCreatedAtDesc(userId, corpusType, pageable);
    }

    @Override
    public List<UserCorpus> getPinnedCorpus(Long userId) {
        return userCorpusRepository.findByUserIdAndIsPinnedTrueOrderByPinnedAtDesc(userId);
    }

    @Override
    public Page<UserCorpus> searchCorpus(Long userId, String keyword, Pageable pageable) {
        return userCorpusRepository.searchByUserIdAndKeyword(userId, keyword, pageable);
    }

    @Override
    @Transactional
    public UserCorpus updateCorpus(Long userId, Long corpusId, String title, String content) {
        UserCorpus corpus = userCorpusRepository.findById(corpusId)
                .orElseThrow(() -> new ResourceNotFoundException("语料不存在"));
        
        if (!corpus.getUserId().equals(userId)) {
            throw new BusinessException("无权访问此语料");
        }

        corpus.setTitle(title);
        corpus.setContentText(content);
        corpus.setSummaryText(generateSummary(content));
        corpus.setOneSentenceSummary(generateOneSentenceSummary(content));

        return userCorpusRepository.save(corpus);
    }

    @Override
    @Transactional
    public void deleteCorpus(Long userId, Long corpusId) {
        UserCorpus corpus = userCorpusRepository.findById(corpusId)
                .orElseThrow(() -> new ResourceNotFoundException("语料不存在"));
        
        if (!corpus.getUserId().equals(userId)) {
            throw new BusinessException("无权访问此语料");
        }

        userCorpusRepository.delete(corpus);
        log.info("删除语料: userId={}, corpusId={}", userId, corpusId);
    }

    @Override
    @Transactional
    public UserCorpus toggleFavorite(Long userId, Long corpusId) {
        UserCorpus corpus = userCorpusRepository.findById(corpusId)
                .orElseThrow(() -> new ResourceNotFoundException("语料不存在"));
        
        if (!corpus.getUserId().equals(userId)) {
            throw new BusinessException("无权访问此语料");
        }

        corpus.setIsFavorite(!Boolean.TRUE.equals(corpus.getIsFavorite()));
        return userCorpusRepository.save(corpus);
    }

    @Override
    @Transactional
    public UserCorpus togglePin(Long userId, Long corpusId) {
        UserCorpus corpus = userCorpusRepository.findById(corpusId)
                .orElseThrow(() -> new ResourceNotFoundException("语料不存在"));
        
        if (!corpus.getUserId().equals(userId)) {
            throw new BusinessException("无权访问此语料");
        }

        if (Boolean.TRUE.equals(corpus.getIsPinned())) {
            corpus.unpin();
        } else {
            corpus.pin();
        }
        return userCorpusRepository.save(corpus);
    }

    @Override
    @Transactional
    public UserCorpus archiveCorpus(Long userId, Long corpusId) {
        UserCorpus corpus = userCorpusRepository.findById(corpusId)
                .orElseThrow(() -> new ResourceNotFoundException("语料不存在"));
        
        if (!corpus.getUserId().equals(userId)) {
            throw new BusinessException("无权访问此语料");
        }

        corpus.archive();
        return userCorpusRepository.save(corpus);
    }

    @Override
    @Transactional
    public UserCorpus unarchiveCorpus(Long userId, Long corpusId) {
        UserCorpus corpus = userCorpusRepository.findById(corpusId)
                .orElseThrow(() -> new ResourceNotFoundException("语料不存在"));
        
        if (!corpus.getUserId().equals(userId)) {
            throw new BusinessException("无权访问此语料");
        }

        corpus.unarchive();
        return userCorpusRepository.save(corpus);
    }

    @Override
    @Transactional
    public void recordView(Long corpusId) {
        userCorpusRepository.incrementViewCount(corpusId, LocalDateTime.now());
    }

    @Override
    public long countUserCorpus(Long userId) {
        return userCorpusRepository.countByUserId(userId);
    }

    @Override
    public long countUserCorpusByType(Long userId, UserCorpus.CorpusType corpusType) {
        return userCorpusRepository.countByUserIdAndCorpusType(userId, corpusType);
    }

    @Override
    public boolean existsBySource(Long userId, Long sourceId, UserCorpus.SourceType sourceType) {
        return userCorpusRepository.existsByUserIdAndSourceIdAndSourceType(userId, sourceId, sourceType);
    }

    @Override
    public List<UserCorpus> getInsightsByDiscussion(Long userId, Long discussionId) {
        return userCorpusRepository.findByUserIdAndDiscussionIdAndCorpusType(
                userId, discussionId, UserCorpus.CorpusType.SOCRATIC_INSIGHT);
    }

    @Override
    public List<UserCorpus> getRecentCorpus(Long userId, int limit) {
        return userCorpusRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .limit(limit)
                .toList();
    }

    // ==================== 私有辅助方法 ====================

    private String generateSummary(String content) {
        if (content == null || content.length() <= 200) {
            return content;
        }
        return content.substring(0, 200) + "...";
    }

    private String generateOneSentenceSummary(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        // 提取第一句话或前100个字符
        String[] sentences = content.split("[。\\.\\n]");
        if (sentences.length > 0 && !sentences[0].trim().isEmpty()) {
            String first = sentences[0].trim();
            return first.length() > 100 ? first.substring(0, 100) + "..." : first;
        }
        return content.length() > 100 ? content.substring(0, 100) + "..." : content;
    }

    private String buildSocraticContent(String insight, String dialogueSummary) {
        StringBuilder content = new StringBuilder();
        content.append("## 核心洞察\n\n");
        content.append(insight).append("\n\n");
        if (dialogueSummary != null && !dialogueSummary.isEmpty()) {
            content.append("## 对话过程\n\n");
            content.append(dialogueSummary);
        }
        return content.toString();
    }

    private Integer calculateReadingTime(String content) {
        if (content == null || content.isEmpty()) {
            return 1;
        }
        // 假设平均阅读速度 300 字/分钟
        int wordCount = content.length();
        return Math.max(1, wordCount / 300);
    }
}
