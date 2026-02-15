package com.evomind.service;

import com.evomind.dto.request.SendMessageRequest;
import com.evomind.dto.request.StartSocraticRequest;
import com.evomind.dto.response.SocraticDialogueResponse;
import com.evomind.dto.response.SocraticInsightResponse;
import com.evomind.dto.response.SocraticMessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 苏格拉底式对话服务接口
 * 通过AI追问引导用户深度思考
 */
public interface SocraticDialogueService {

    /**
     * 开始新的苏格拉底式对话
     * @param userId 用户ID
     * @param request 开始对话请求
     * @return 对话会话响应
     */
    SocraticDialogueResponse startDialogue(Long userId, StartSocraticRequest request);

    /**
     * 发送消息并获取AI追问
     * @param userId 用户ID
     * @param request 发送消息请求
     * @return AI回复消息
     */
    SocraticMessageResponse sendMessage(Long userId, SendMessageRequest request);

    /**
     * 获取对话详情
     * @param dialogueId 对话ID
     * @param userId 用户ID（权限校验）
     * @return 对话详情
     */
    SocraticDialogueResponse getDialogue(Long dialogueId, Long userId);

    /**
     * 获取对话的所有消息
     * @param dialogueId 对话ID
     * @param userId 用户ID（权限校验）
     * @return 消息列表
     */
    List<SocraticMessageResponse> getDialogueMessages(Long dialogueId, Long userId);

    /**
     * 获取用户的对话列表
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 对话列表
     */
    Page<SocraticDialogueResponse> getUserDialogues(Long userId, Pageable pageable);

    /**
     * 结束对话并生成洞察
     * @param userId 用户ID
     * @param dialogueId 对话ID
     * @param satisfaction 用户满意度评分
     * @return 洞察响应
     */
    SocraticInsightResponse finalizeDialogue(Long userId, Long dialogueId, Integer satisfaction);

    /**
     * 放弃对话
     * @param userId 用户ID
     * @param dialogueId 对话ID
     */
    void abandonDialogue(Long userId, Long dialogueId);

    /**
     * 获取用户在某个讨论下的活动对话
     * @param userId 用户ID
     * @param discussionId 讨论ID
     * @return 对话响应（如果有）
     */
    SocraticDialogueResponse getActiveDialogue(Long userId, Long discussionId);

    /**
     * 检查用户是否可以开始新对话
     * @param userId 用户ID
     * @param discussionId 讨论ID
     * @return 是否可以开始
     */
    boolean canStartDialogue(Long userId, Long discussionId);

    /**
     * 重新生成AI回复（如果用户对回复不满意）
     * @param userId 用户ID
     * @param messageId 需要重新生成的AI消息ID
     * @return 新的AI回复
     */
    SocraticMessageResponse regenerateResponse(Long userId, Long messageId);

    /**
     * 获取用户的对话统计
     * @param userId 用户ID
     * @return 统计数据
     */
    DialogueStats getDialogueStats(Long userId);

    /**
     * 将对话洞察保存为认知卡片
     * @param userId 用户ID
     * @param dialogueId 对话ID
     * @return 创建的卡片ID
     */
    Long saveInsightAsCard(Long userId, Long dialogueId);

    /**
     * 对话统计
     */
    class DialogueStats {
        private Long totalDialogues;
        private Long completedDialogues;
        private Long inProgressDialogues;
        private Double averageRounds;
        private Long totalMessages;
        private Double averageDepthLevel;

        // Getters and Setters
        public Long getTotalDialogues() { return totalDialogues; }
        public void setTotalDialogues(Long totalDialogues) { this.totalDialogues = totalDialogues; }
        public Long getCompletedDialogues() { return completedDialogues; }
        public void setCompletedDialogues(Long completedDialogues) { this.completedDialogues = completedDialogues; }
        public Long getInProgressDialogues() { return inProgressDialogues; }
        public void setInProgressDialogues(Long inProgressDialogues) { this.inProgressDialogues = inProgressDialogues; }
        public Double getAverageRounds() { return averageRounds; }
        public void setAverageRounds(Double averageRounds) { this.averageRounds = averageRounds; }
        public Long getTotalMessages() { return totalMessages; }
        public void setTotalMessages(Long totalMessages) { this.totalMessages = totalMessages; }
        public Double getAverageDepthLevel() { return averageDepthLevel; }
        public void setAverageDepthLevel(Double averageDepthLevel) { this.averageDepthLevel = averageDepthLevel; }
    }
}
