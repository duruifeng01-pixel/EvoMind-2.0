package com.evomind.repository;

import com.evomind.entity.MindMapNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MindMapNodeRepository extends JpaRepository<MindMapNode, Long> {

    List<MindMapNode> findByCardIdOrderByLevelAscSortOrderAsc(Long cardId);

    List<MindMapNode> findByCardIdAndLevel(Long cardId, Integer level);

    Optional<MindMapNode> findByCardIdAndNodeId(Long cardId, String nodeId);

    List<MindMapNode> findByCardIdAndParentNodeId(Long cardId, String parentNodeId);

    List<MindMapNode> findByCardIdAndHasOriginalReferenceTrue(Long cardId);

    @Modifying
    @Query("DELETE FROM MindMapNode m WHERE m.cardId = :cardId")
    void deleteByCardId(@Param("cardId") Long cardId);

    long countByCardId(Long cardId);
}
