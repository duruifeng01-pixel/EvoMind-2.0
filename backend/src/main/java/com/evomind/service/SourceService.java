package com.evomind.service;

import com.evomind.entity.Source;

import java.util.List;

public interface SourceService {

    Source createSource(Long userId, String name, String platform, String homeUrl, String category);

    Source getSourceById(Long id, Long userId);

    List<Source> getSourcesByUserId(Long userId);

    List<Source> getSourcesByCategory(Long userId, String category);

    List<Source> getEnabledSources(Long userId);

    Source updateSource(Long id, Long userId, String name, String category);

    void deleteSource(Long id, Long userId);

    void togglePin(Long id, Long userId);

    void toggleEnabled(Long id, Long userId);

    void updateSyncStatus(Long id, String status);

    void updateArticleCount(Long id, Integer count);

    boolean isSourceExists(Long userId, String homeUrl);

    long countByUserId(Long userId);

    long countEnabledByUserId(Long userId);
}
