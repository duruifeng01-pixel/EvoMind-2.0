package com.evomind.service.impl;

import com.evomind.entity.MindMapNode;
import com.evomind.repository.MindMapNodeRepository;
import com.evomind.service.MindMapService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MindMapServiceImpl implements MindMapService {

    private final MindMapNodeRepository mindMapNodeRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<MindMapNode> getMindMapNodes(Long cardId) {
        return mindMapNodeRepository.findByCardIdOrderByLevelAscSortOrderAsc(cardId);
    }

    @Override
    @Transactional(readOnly = true)
    public MindMapNode getRootNode(Long cardId) {
        List<MindMapNode> nodes = mindMapNodeRepository.findByCardIdAndLevel(cardId, 0);
        return nodes.isEmpty() ? null : nodes.get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MindMapNode> getChildNodes(Long cardId, String parentNodeId) {
        return mindMapNodeRepository.findByCardIdAndParentNodeId(cardId, parentNodeId);
    }

    @Override
    @Transactional
    public MindMapNode createNode(Long cardId, String parentNodeId, String text, Integer level) {
        MindMapNode node = new MindMapNode();
        node.setCardId(cardId);
        node.setNodeId(UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        node.setParentNodeId(parentNodeId);
        node.setText(text);
        node.setLevel(level != null ? level : 0);
        node.setNodeType("TOPIC");
        node.setIsExpanded(true);
        
        // 计算排序号
        long count = mindMapNodeRepository.countByCardId(cardId);
        node.setSortOrder((int) count);
        
        return mindMapNodeRepository.save(node);
    }

    @Override
    @Transactional
    public void saveMindMapNodes(Long cardId, String nodesJson) {
        try {
            // 先删除旧节点
            mindMapNodeRepository.deleteByCardId(cardId);
            
            JsonNode rootNode = objectMapper.readTree(nodesJson);
            List<MindMapNode> nodes = new ArrayList<>();
            parseMindMapJson(cardId, rootNode, null, 0, nodes);
            
            mindMapNodeRepository.saveAll(nodes);
            log.info("保存脑图节点成功: cardId={}, nodeCount={}", cardId, nodes.size());
        } catch (Exception e) {
            log.error("保存脑图节点失败: cardId={}", cardId, e);
            throw new RuntimeException("保存脑图失败", e);
        }
    }

    private void parseMindMapJson(Long cardId, JsonNode jsonNode, String parentId, int level, List<MindMapNode> nodes) {
        if (jsonNode == null || !jsonNode.isObject()) {
            return;
        }
        
        String nodeId = jsonNode.has("id") ? jsonNode.get("id").asText() : 
                       UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        
        MindMapNode node = new MindMapNode();
        node.setCardId(cardId);
        node.setNodeId(nodeId);
        node.setParentNodeId(parentId);
        node.setText(jsonNode.has("text") ? jsonNode.get("text").asText() : "");
        node.setLevel(level);
        node.setSortOrder(nodes.size());
        
        if (jsonNode.has("description")) {
            node.setDescription(jsonNode.get("description").asText());
        }
        if (jsonNode.has("nodeType")) {
            node.setNodeType(jsonNode.get("nodeType").asText());
        }
        if (jsonNode.has("originalContentId")) {
            node.setOriginalContentId(jsonNode.get("originalContentId").asLong());
            node.setHasOriginalReference(true);
        }
        if (jsonNode.has("originalParagraphIndex")) {
            node.setOriginalParagraphIndex(jsonNode.get("originalParagraphIndex").asInt());
        }
        
        nodes.add(node);
        
        // 递归处理子节点
        if (jsonNode.has("children") && jsonNode.get("children").isArray()) {
            Iterator<JsonNode> children = jsonNode.get("children").elements();
            while (children.hasNext()) {
                parseMindMapJson(cardId, children.next(), nodeId, level + 1, nodes);
            }
        }
    }

    @Override
    @Transactional
    public void deleteMindMap(Long cardId) {
        mindMapNodeRepository.deleteByCardId(cardId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MindMapNode> getNodesWithOriginalReference(Long cardId) {
        return mindMapNodeRepository.findByCardIdAndHasOriginalReferenceTrue(cardId);
    }
}
