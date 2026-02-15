package com.evomind.service;

import com.evomind.entity.Card;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 观点分析服务
 * 使用AI分析两张卡片是否存在观点对立
 */
public interface OpinionAnalysisService {

    /**
     * 分析两张卡片是否存在观点冲突
     * @param card1 卡片1
     * @param card2 卡片2
     * @return 分析结果，包含是否存在冲突、冲突类型、冲突描述等
     */
    OpinionConflictResult analyzeConflict(Card card1, Card card2);

    /**
     * 批量分析卡片间的冲突
     * @param targetCard 目标卡片
     * @param candidateCards 候选卡片列表
     * @return 分析结果列表
     */
    java.util.List<OpinionConflictResult> analyzeBatchConflicts(Card targetCard, java.util.List<Card> candidateCards);

    /**
     * 提取卡片的观点立场
     * @param card 卡片
     * @return 观点信息
     */
    OpinionStance extractOpinionStance(Card card);

    /**
     * 观点冲突分析结果
     */
    class OpinionConflictResult {
        private boolean hasConflict;
        private String conflictType;
        private String conflictDescription;
        private BigDecimal conflictScore;
        private String aiAnalysis;
        private String topic;
        private Card card1;
        private Card card2;

        // Getters and Setters
        public boolean isHasConflict() { return hasConflict; }
        public void setHasConflict(boolean hasConflict) { this.hasConflict = hasConflict; }
        
        public String getConflictType() { return conflictType; }
        public void setConflictType(String conflictType) { this.conflictType = conflictType; }
        
        public String getConflictDescription() { return conflictDescription; }
        public void setConflictDescription(String conflictDescription) { this.conflictDescription = conflictDescription; }
        
        public BigDecimal getConflictScore() { return conflictScore; }
        public void setConflictScore(BigDecimal conflictScore) { this.conflictScore = conflictScore; }
        
        public String getAiAnalysis() { return aiAnalysis; }
        public void setAiAnalysis(String aiAnalysis) { this.aiAnalysis = aiAnalysis; }
        
        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        
        public Card getCard1() { return card1; }
        public void setCard1(Card card1) { this.card1 = card1; }
        
        public Card getCard2() { return card2; }
        public void setCard2(Card card2) { this.card2 = card2; }
    }

    /**
     * 观点立场信息
     */
    class OpinionStance {
        private String mainTopic;
        private String coreViewpoint;
        private String supportingEvidence;
        private String stanceCategory;
        private java.util.List<String> keyClaims;

        // Getters and Setters
        public String getMainTopic() { return mainTopic; }
        public void setMainTopic(String mainTopic) { this.mainTopic = mainTopic; }
        
        public String getCoreViewpoint() { return coreViewpoint; }
        public void setCoreViewpoint(String coreViewpoint) { this.coreViewpoint = coreViewpoint; }
        
        public String getSupportingEvidence() { return supportingEvidence; }
        public void setSupportingEvidence(String supportingEvidence) { this.supportingEvidence = supportingEvidence; }
        
        public String getStanceCategory() { return stanceCategory; }
        public void setStanceCategory(String stanceCategory) { this.stanceCategory = stanceCategory; }
        
        public java.util.List<String> getKeyClaims() { return keyClaims; }
        public void setKeyClaims(java.util.List<String> keyClaims) { this.keyClaims = keyClaims; }
    }
}
