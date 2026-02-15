package com.evomind.repository;

import com.evomind.entity.SocraticMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 苏格拉底式对话消息数据访问层
 */
@Repository
public interface SocraticMessageRepository extends JpaRepository<SocraticMessage, Long> {

    /**
     * 查找对话的所有消息（按序号排序）
     */
    List<SocraticMessage> findByDialogueIdOrderBySequenceNumberAsc(Long dialogueId);

    /**
     * 查找对话的所有消息（分页）
     */
    Page<SocraticMessage> findByDialogueIdOrderBySequenceNumberAsc(Long dialogueId, Pageable pageable);

    /**
     * 查找特定轮次的消息
     */
    List<SocraticMessage> findByDialogueIdAndRoundOrderBySequenceNumberAsc(Long dialogueId, Integer round);

    /**
     * 查找对话的AI消息
     */
    @Query("SELECT m FROM SocraticMessage m WHERE m.dialogueId = :dialogueId AND m.role = 'AI' ORDER BY m.sequenceNumber")
    List<SocraticMessage> findAiMessagesByDialogueId(@Param("dialogueId") Long dialogueId);

    /**
     * 查找对话的用户消息
     */
    @Query("SELECT m FROM SocraticMessage m WHERE m.dialogueId = :dialogueId AND m.role = 'USER' ORDER BY m.sequenceNumber")
    List<SocraticMessage> findUserMessagesByDialogueId(@Param("dialogueId") Long dialogueId);

    /**
     * 查找最新的消息
     */
    Optional<SocraticMessage> findFirstByDialogueIdOrderBySequenceNumberDesc(Long dialogueId);

    /**
     * 查找特定类型的消息
     */
    List<SocraticMessage> findByDialogueIdAndTypeOrderBySequenceNumberAsc(
            Long dialogueId, SocraticMessage.MessageType type);

    /**
     * 统计对话的消息数量
     */
    Long countByDialogueId(Long dialogueId);

    /**
     * 统计特定角色的消息数量
     */
    @Query("SELECT COUNT(m) FROM SocraticMessage m WHERE m.dialogueId = :dialogueId AND m.role = :role")
    Long countByDialogueIdAndRole(@Param("dialogueId") Long dialogueId, @Param("role") SocraticMessage.MessageRole role);

    /**
     * 查找特定深度的消息
     */
    List<SocraticMessage> findByDialogueIdAndDepthLevelOrderBySequenceNumberAsc(
            Long dialogueId, Integer depthLevel);

    /**
     * 获取最大的序号
     */
    @Query("SELECT COALESCE(MAX(m.sequenceNumber), 0) FROM SocraticMessage m WHERE m.dialogueId = :dialogueId")
    Integer findMaxSequenceNumberByDialogueId(@Param("dialogueId") Long dialogueId);

    /**
     * 查找追问消息
     */
    List<SocraticMessage> findByDialogueIdAndIsFollowUpTrueOrderBySequenceNumberAsc(Long dialogueId);

    /**
     * 查找最终总结消息
     */
    Optional<SocraticMessage> findFirstByDialogueIdAndIsFinalSummaryTrue(Long dialogueId);

    /**
     * 删除对话的所有消息
     */
    void deleteByDialogueId(Long dialogueId);

    /**
     * 获取用户累计的对话消息统计
     */
    @Query("SELECT COUNT(m) FROM SocraticMessage m JOIN SocraticDialogue d ON m.dialogueId = d.id " +
           "WHERE d.userId = :userId AND m.role = 'USER'")
    Long countUserMessagesByUserId(@Param("userId") Long userId);

    /**
     * 获取用户的深度思考统计（平均深度层级）
     */
    @Query("SELECT AVG(m.depthLevel) FROM SocraticMessage m JOIN SocraticDialogue d ON m.dialogueId = d.id " +
           "WHERE d.userId = :userId AND m.role = 'USER'")
    Double calculateAverageDepthLevelByUserId(@Param("userId") Long userId);
}
