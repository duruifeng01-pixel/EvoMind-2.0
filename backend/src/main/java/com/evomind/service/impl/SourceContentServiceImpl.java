package com.evomind.service.impl;

import com.evomind.entity.SourceContent;
import com.evomind.repository.SourceContentRepository;
import com.evomind.service.SourceContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SourceContentServiceImpl implements SourceContentService {

    private final SourceContentRepository sourceContentRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<SourceContent> getContentBySourceId(Long sourceId) {
        return sourceContentRepository.findBySourceId(sourceId);
    }

    @Override
    @Transactional(readOnly = true)
    public String getParagraph(Long contentId, Integer paragraphIndex) {
        if (paragraphIndex == null || paragraphIndex < 0) {
            return null;
        }
        return sourceContentRepository.getParagraphByIndex(contentId, paragraphIndex);
    }

    @Override
    @Transactional
    public SourceContent saveContent(Long sourceId, String content, String title, String author) {
        String hash = calculateHash(content);
        
        SourceContent sourceContent = new SourceContent();
        sourceContent.setSourceId(sourceId);
        sourceContent.setTitle(title);
        sourceContent.setAuthor(author);
        sourceContent.setContentText(content);
        sourceContent.setContentHash(hash);
        sourceContent.setFetchStatus("SUCCESS");
        sourceContent.setIsProcessed(false);
        
        // 计算段落数和字数
        if (content != null) {
            String[] paragraphs = content.split("\\n");
            sourceContent.setParagraphCount(paragraphs.length);
            sourceContent.setWordCount(content.length());
        }
        
        return sourceContentRepository.save(sourceContent);
    }

    @Override
    @Transactional
    public void updateFetchStatus(Long contentId, String status, String error) {
        Optional<SourceContent> optional = sourceContentRepository.findById(contentId);
        if (optional.isPresent()) {
            SourceContent content = optional.get();
            content.setFetchStatus(status);
            content.setFetchError(error);
            sourceContentRepository.save(content);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDuplicate(String contentHash) {
        return sourceContentRepository.existsByContentHash(contentHash);
    }

    @Override
    @Transactional
    public void deleteBySourceId(Long sourceId) {
        Optional<SourceContent> content = sourceContentRepository.findBySourceId(sourceId);
        content.ifPresent(sourceContentRepository::delete);
    }

    @Override
    @Transactional
    public SourceContent saveContent(Long userId, Long sourceId, String title, String content,
                                     String sourceUrl, String contentType) {
        String hash = calculateHash(content);
        
        SourceContent sourceContent = new SourceContent();
        sourceContent.setUserId(userId);
        sourceContent.setSourceId(sourceId);
        sourceContent.setTitle(title);
        sourceContent.setContentText(content);
        sourceContent.setSourceUrl(sourceUrl);
        sourceContent.setContentType(contentType);
        sourceContent.setContentHash(hash);
        sourceContent.setFetchStatus("SUCCESS");
        sourceContent.setIsProcessed(true);
        
        // 计算段落数和字数
        if (content != null) {
            String[] paragraphs = content.split("\\n\\s*\\n");
            sourceContent.setParagraphCount(paragraphs.length);
            sourceContent.setWordCount(content.length());
        }
        
        return sourceContentRepository.save(sourceContent);
    }

    private String calculateHash(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(content.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("计算内容哈希失败", e);
            return String.valueOf(content.hashCode());
        }
    }
}
