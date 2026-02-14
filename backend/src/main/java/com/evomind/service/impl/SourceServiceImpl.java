package com.evomind.service.impl;

import com.evomind.entity.Source;
import com.evomind.exception.BusinessException;
import com.evomind.repository.SourceRepository;
import com.evomind.service.SourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SourceServiceImpl implements SourceService {

    private final SourceRepository sourceRepository;

    @Override
    @Transactional
    public Source createSource(Long userId, String name, String platform, String homeUrl, String category) {
        if (sourceRepository.existsByUserIdAndHomeUrl(userId, homeUrl)) {
            throw new BusinessException("该来源已存在");
        }
        
        Source source = new Source();
        source.setUserId(userId);
        source.setName(name);
        source.setPlatform(platform);
        source.setHomeUrl(homeUrl);
        source.setCategory(category);
        source.setIsPinned(false);
        source.setEnabled(true);
        source.setArticleCount(0);
        return sourceRepository.save(source);
    }

    @Override
    @Transactional(readOnly = true)
    public Source getSourceById(Long id, Long userId) {
        return sourceRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException("来源不存在"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Source> getSourcesByUserId(Long userId) {
        return sourceRepository.findByUserIdOrderByIsPinnedDescCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Source> getSourcesByCategory(Long userId, String category) {
        return sourceRepository.findByUserIdAndCategoryOrderByIsPinnedDescCreatedAtDesc(userId, category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Source> getEnabledSources(Long userId) {
        return sourceRepository.findByUserIdAndEnabledTrueOrderByIsPinnedDescCreatedAtDesc(userId);
    }

    @Override
    @Transactional
    public Source updateSource(Long id, Long userId, String name, String category) {
        Source source = getSourceById(id, userId);
        if (name != null) {
            source.setName(name);
        }
        if (category != null) {
            source.setCategory(category);
        }
        return sourceRepository.save(source);
    }

    @Override
    @Transactional
    public void deleteSource(Long id, Long userId) {
        Source source = getSourceById(id, userId);
        sourceRepository.delete(source);
    }

    @Override
    @Transactional
    public void togglePin(Long id, Long userId) {
        Source source = getSourceById(id, userId);
        source.setIsPinned(!Boolean.TRUE.equals(source.getIsPinned()));
        sourceRepository.save(source);
    }

    @Override
    @Transactional
    public void toggleEnabled(Long id, Long userId) {
        Source source = getSourceById(id, userId);
        source.setEnabled(!Boolean.TRUE.equals(source.getEnabled()));
        sourceRepository.save(source);
    }

    @Override
    @Transactional
    public void updateSyncStatus(Long id, String status) {
        sourceRepository.updateSyncStatus(id, status);
    }

    @Override
    @Transactional
    public void updateArticleCount(Long id, Integer count) {
        sourceRepository.updateArticleCount(id, count, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSourceExists(Long userId, String homeUrl) {
        return sourceRepository.existsByUserIdAndHomeUrl(userId, homeUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByUserId(Long userId) {
        return sourceRepository.countByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countEnabledByUserId(Long userId) {
        return sourceRepository.countByUserIdAndEnabledTrue(userId);
    }
}
