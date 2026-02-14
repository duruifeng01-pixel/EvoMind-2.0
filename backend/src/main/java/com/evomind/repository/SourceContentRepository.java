package com.evomind.repository;

import com.evomind.entity.SourceContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SourceContentRepository extends JpaRepository<SourceContent, Long> {

    Optional<SourceContent> findBySourceId(Long sourceId);

    List<SourceContent> findByFetchStatusOrderByCreatedAtDesc(String fetchStatus);

    @Query("SELECT sc FROM SourceContent sc WHERE sc.contentHash = :hash AND sc.id != :excludeId")
    Optional<SourceContent> findByContentHashExcludingId(@Param("hash") String hash, @Param("excludeId") Long excludeId);

    boolean existsByContentHash(String contentHash);

    long countBySourceId(Long sourceId);

    @Query(value = "SELECT SUBSTRING_INDEX(SUBSTRING_INDEX(sc.content_text, '\n', :paragraphIndex + 1), '\n', -1) " +
           "FROM source_contents sc WHERE sc.id = :contentId", nativeQuery = true)
    String getParagraphByIndex(@Param("contentId") Long contentId, @Param("paragraphIndex") Integer paragraphIndex);
}
