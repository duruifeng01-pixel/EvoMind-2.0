package com.evomind.service;

import com.evomind.entity.MindMapNode;

import java.util.List;

public interface MindMapService {

    /**
     * 获取卡片的脑图节点
     * @param cardId 卡片ID
     * @return 节点列表
     */
    List<MindMapNode> getMindMapNodes(Long cardId);

    /**
     * 获取脑树根节点
     * @param cardId 卡片ID
     * @return 根节点
     */
    MindMapNode getRootNode(Long cardId);

    /**
     * 获取节点的子节点
     * @param cardId 卡片ID
     * @param parentNodeId 父节点ID
     * @return 子节点列表
     */
    List<MindMapNode> getChildNodes(Long cardId, String parentNodeId);

    /**
     * 创建脑图节点
     * @param cardId 卡片ID
     * @param parentNodeId 父节点ID（可为null）
     * @param text 节点文本
     * @param level 层级
     * @return 创建的节点
     */
    MindMapNode createNode(Long cardId, String parentNodeId, String text, Integer level);

    /**
     * 批量保存脑图节点
     * @param cardId 卡片ID
     * @param nodesJson 节点JSON数组
     */
    void saveMindMapNodes(Long cardId, String nodesJson);

    /**
     * 删除卡片的脑图
     * @param cardId 卡片ID
     */
    void deleteMindMap(Long cardId);

    /**
     * 获取有原文引用的节点
     * @param cardId 卡片ID
     * @return 节点列表
     */
    List<MindMapNode> getNodesWithOriginalReference(Long cardId);
}
