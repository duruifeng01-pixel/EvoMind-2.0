package com.evomind.service;

import com.evomind.entity.Card;
import com.evomind.entity.UserCognitiveProfile;

import java.util.List;

/**
 * 用户认知画像服务
 * 从用户语料库构建和管理用户的认知画像
 */
public interface CognitiveProfileService {

    /**
     * 从用户卡片构建认知画像
     * @param userId 用户ID
     */
    void buildCognitiveProfile(Long userId);

    /**
     * 获取用户的所有认知画像
     * @param userId 用户ID
     * @return 认知画像列表
     */
    List<UserCognitiveProfile> getUserProfiles(Long userId);

    /**
     * 根据主题获取认知画像
     * @param userId 用户ID
     * @param topic 主题
     * @return 认知画像
     */
    UserCognitiveProfile getProfileByTopic(Long userId, String topic);

    /**
     * 检查卡片是否与用户认知画像冲突
     * @param userId 用户ID
     * @param card 新卡片
     * @return 冲突结果列表
     */
    List<CognitiveConflictResult> checkConflictWithProfile(Long userId, Card card);

    /**
     * 更新认知画像（当添加新卡片时）
     * @param userId 用户ID
     * @param card 新卡片
     */
    void updateProfileWithCard(Long userId, Card card);

    /**
     * 认知冲突结果
     */
    class CognitiveConflictResult {
        private boolean hasConflict;
        private UserCognitiveProfile profile;
        private String conflictType;
        private String conflictDescription;
        private Double conflictScore;
        private String userBelief;
        private String cardViewpoint;
        private String aiAnalysis;

        // Getters and Setters
        public boolean isHasConflict() { return hasConflict; }
        public void setHasConflict(boolean hasConflict) { this.hasConflict = hasConflict; }

        public UserCognitiveProfile getProfile() { return profile; }
        public void setProfile(UserCognitiveProfile profile) { this.profile = profile; }

        public String getConflictType() { return conflictType; }
        public void setConflictType(String conflictType) { this.conflictType = conflictType; }

        public String getConflictDescription() { return conflictDescription; }
        public void setConflictDescription(String conflictDescription) { this.conflictDescription = conflictDescription; }

        public Double getConflictScore() { return conflictScore; }
        public void setConflictScore(Double conflictScore) { this.conflictScore = conflictScore; }

        public String getUserBelief() { return userBelief; }
        public void setUserBelief(String userBelief) { this.userBelief = userBelief; }

        public String getCardViewpoint() { return cardViewpoint; }
        public void setCardViewpoint(String cardViewpoint) { this.cardViewpoint = cardViewpoint; }

        public String getAiAnalysis() { return aiAnalysis; }
        public void setAiAnalysis(String aiAnalysis) { this.aiAnalysis = aiAnalysis; }
    }
}
