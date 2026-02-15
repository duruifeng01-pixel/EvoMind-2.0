package com.evomind.service.impl;

import com.evomind.dto.request.SendMessageRequest;
import com.evomind.dto.request.StartSocraticRequest;
import com.evomind.dto.response.SocraticDialogueResponse;
import com.evomind.dto.response.SocraticInsightResponse;
import com.evomind.dto.response.SocraticMessageResponse;
import com.evomind.entity.Card;
import com.evomind.entity.Discussion;
import com.evomind.entity.SocraticDialogue;
import com.evomind.entity.SocraticMessage;
import com.evomind.entity.UserCorpus;
import com.evomind.exception.BusinessException;
import com.evomind.exception.ResourceNotFoundException;
import com.evomind.repository.DiscussionRepository;
import com.evomind.repository.SocraticDialogueRepository;
import com.evomind.repository.SocraticMessageRepository;
import com.evomind.service.AiGenerationService;
import com.evomind.service.CardService;
import com.evomind.service.SocraticDialogueService;
import com.evomind.service.UserCorpusService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 苏格拉底式对话服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SocraticDialogueServiceImpl implements SocraticDialogueService {

    private final SocraticDialogueRepository dialogueRepository;
    private final SocraticMessageRepository messageRepository;
    private final DiscussionRepository discussionRepository;
    private final AiGenerationService aiGenerationService;
    private final CardService cardService;
    private final UserCorpusService userCorpusService;
    private final ObjectMapper objectMapper;

    private static final int MAX_ROUNDS_DEFAULT = 5;
    private static final int MAX_RETRY_COUNT = 2;
    private static final int MAX_MESSAGE_LENGTH = 2000;

    @Override
    @Transactional
    public SocraticDialogueResponse startDialogue(Long userId, StartSocraticRequest request) {
        log.info("用户 {} 开始苏格拉底式对话，讨论ID: {}", userId, request.getDiscussionId());

        // 检查讨论是否存在
        Discussion discussion = discussionRepository.findById(request.getDiscussionId())
                .orElseThrow(() -> new ResourceNotFoundException("讨论不存在"));

        // 检查是否已有活动对话
        dialogueRepository.findByUserIdAndDiscussionIdAndStatus(
                userId, request.getDiscussionId(), SocraticDialogue.DialogueStatus.IN_PROGRESS)
                .ifPresent(d -> {
                    throw new BusinessException("您已有进行中的对话，请先结束当前对话");
                });

        // 创建新对话
        SocraticDialogue dialogue = new SocraticDialogue();
        dialogue.setDiscussionId(request.getDiscussionId());
        dialogue.setUserId(userId);
        dialogue.setMaxRounds(request.getMaxRounds() != null ? request.getMaxRounds() : MAX_ROUNDS_DEFAULT);
        dialogue.setStatus(SocraticDialogue.DialogueStatus.IN_PROGRESS);
        dialogue.setCurrentRound(0);
        dialogue.setTotalMessages(0);

        SocraticDialogue savedDialogue = dialogueRepository.save(dialogue);

        // 生成初始问题
        String initialQuestion = generateInitialQuestion(discussion, request.getInitialThought());
        dialogue.setInitialQuestion(initialQuestion);

        // 创建AI初始消息
        SocraticMessage aiMessage = createAiMessage(
                savedDialogue.getId(),
                0,
                initialQuestion,
                SocraticMessage.MessageType.INITIAL_QUESTION,
                1,
                false,
                buildInitialThinkingHints(discussion.getTitle())
        );

        savedDialogue.nextRound();
        savedDialogue.incrementMessageCount();
        dialogueRepository.save(savedDialogue);

        log.info("苏格拉底式对话创建成功，ID: {}", savedDialogue.getId());
        return convertToDialogueResponse(savedDialogue, aiMessage);
    }

    @Override
    @Transactional
    public SocraticMessageResponse sendMessage(Long userId, SendMessageRequest request) {
        log.info("用户 {} 发送消息到对话 {}", userId, request.getDialogueId());

        SocraticDialogue dialogue = dialogueRepository.findById(request.getDialogueId())
                .orElseThrow(() -> new ResourceNotFoundException("对话不存在"));

        // 权限校验
        if (!dialogue.getUserId().equals(userId)) {
            throw new BusinessException("无权访问此对话");
        }

        // 检查对话状态
        if (!dialogue.canContinue()) {
            throw new BusinessException("对话已结束或已达到最大轮次");
        }

        // 截断过长消息
        String content = truncateMessage(request.getContent());

        // 保存用户消息
        Integer sequenceNum = messageRepository.findMaxSequenceNumberByDialogueId(dialogue.getId()) + 1;
        SocraticMessage userMessage = createUserMessage(
                dialogue.getId(),
                dialogue.getCurrentRound(),
                content,
                sequenceNum
        );
        dialogue.incrementMessageCount();

        // 生成AI追问
        SocraticMessage aiResponse = generateAiResponse(dialogue, userMessage);

        dialogue.nextRound();
        dialogue.incrementMessageCount();
        dialogueRepository.save(dialogue);

        return convertToMessageResponse(aiResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SocraticDialogueResponse getDialogue(Long dialogueId, Long userId) {
        SocraticDialogue dialogue = dialogueRepository.findById(dialogueId)
                .orElseThrow(() -> new ResourceNotFoundException("对话不存在"));

        if (!dialogue.getUserId().equals(userId)) {
            throw new BusinessException("无权访问此对话");
        }

        SocraticMessage lastMessage = messageRepository
                .findFirstByDialogueIdOrderBySequenceNumberDesc(dialogue.getId())
                .orElse(null);

        return convertToDialogueResponse(dialogue, lastMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SocraticMessageResponse> getDialogueMessages(Long dialogueId, Long userId) {
        SocraticDialogue dialogue = dialogueRepository.findById(dialogueId)
                .orElseThrow(() -> new ResourceNotFoundException("对话不存在"));

        if (!dialogue.getUserId().equals(userId)) {
            throw new BusinessException("无权访问此对话");
        }

        return messageRepository.findByDialogueIdOrderBySequenceNumberAsc(dialogueId)
                .stream()
                .map(this::convertToMessageResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SocraticDialogueResponse> getUserDialogues(Long userId, Pageable pageable) {
        return dialogueRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(d -> convertToDialogueResponse(d, null));
    }

    @Override
    @Transactional
    public SocraticInsightResponse finalizeDialogue(Long userId, Long dialogueId, Integer satisfaction) {
        log.info("用户 {} 结束对话 {} 并生成洞察", userId, dialogueId);

        SocraticDialogue dialogue = dialogueRepository.findById(dialogueId)
                .orElseThrow(() -> new ResourceNotFoundException("对话不存在"));

        if (!dialogue.getUserId().equals(userId)) {
            throw new BusinessException("无权访问此对话");
        }

        // 获取所有消息
        List<SocraticMessage> messages = messageRepository.findByDialogueIdOrderBySequenceNumberAsc(dialogueId);

        // 生成洞察
        String insight = generateInsight(dialogue, messages);
        dialogue.markInsightGenerated(insight);
        dialogue.markCompleted();
        dialogue.setUserSatisfaction(satisfaction);
        dialogueRepository.save(dialogue);

        return buildInsightResponse(dialogue, messages);
    }

    @Override
    @Transactional
    public void abandonDialogue(Long userId, Long dialogueId) {
        SocraticDialogue dialogue = dialogueRepository.findById(dialogueId)
                .orElseThrow(() -> new ResourceNotFoundException("对话不存在"));

        if (!dialogue.getUserId().equals(userId)) {
            throw new BusinessException("无权访问此对话");
        }

        dialogue.markAbandoned();
        dialogueRepository.save(dialogue);
    }

    @Override
    @Transactional(readOnly = true)
    public SocraticDialogueResponse getActiveDialogue(Long userId, Long discussionId) {
        return dialogueRepository
                .findByUserIdAndDiscussionIdAndStatus(userId, discussionId, SocraticDialogue.DialogueStatus.IN_PROGRESS)
                .map(d -> {
                    SocraticMessage lastMessage = messageRepository
                            .findFirstByDialogueIdOrderBySequenceNumberDesc(d.getId())
                            .orElse(null);
                    return convertToDialogueResponse(d, lastMessage);
                })
                .orElse(null);
    }

    @Override
    public boolean canStartDialogue(Long userId, Long discussionId) {
        return !dialogueRepository.existsByUserIdAndDiscussionIdAndStatus(
                userId, discussionId, SocraticDialogue.DialogueStatus.IN_PROGRESS);
    }

    @Override
    @Transactional
    public SocraticMessageResponse regenerateResponse(Long userId, Long messageId) {
        SocraticMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("消息不存在"));

        SocraticDialogue dialogue = dialogueRepository.findById(message.getDialogueId())
                .orElseThrow(() -> new ResourceNotFoundException("对话不存在"));

        if (!dialogue.getUserId().equals(userId)) {
            throw new BusinessException("无权访问此对话");
        }

        // 获取上一条用户消息
        List<SocraticMessage> messages = messageRepository.findByDialogueIdOrderBySequenceNumberAsc(dialogue.getId());
        SocraticMessage userMessage = messages.stream()
                .filter(m -> m.getSequenceNumber() < message.getSequenceNumber() && m.isFromUser())
                .reduce((first, second) -> second)
                .orElseThrow(() -> new BusinessException("无法重新生成"));

        // 删除旧AI消息
        messageRepository.delete(message);
        dialogue.setTotalMessages(dialogue.getTotalMessages() - 1);
        dialogueRepository.save(dialogue);

        // 生成新回复
        SocraticMessage newResponse = generateAiResponse(dialogue, userMessage);
        return convertToMessageResponse(newResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DialogueStats getDialogueStats(Long userId) {
        DialogueStats stats = new DialogueStats();
        stats.setTotalDialogues(dialogueRepository.countByUserId(userId));
        stats.setCompletedDialogues(
                (long) dialogueRepository.findByUserIdAndStatus(userId, SocraticDialogue.DialogueStatus.COMPLETED).size()
        );
        stats.setInProgressDialogues(
                (long) dialogueRepository.findByUserIdAndStatus(userId, SocraticDialogue.DialogueStatus.IN_PROGRESS).size()
        );
        stats.setTotalMessages(messageRepository.countUserMessagesByUserId(userId));
        stats.setAverageDepthLevel(messageRepository.calculateAverageDepthLevelByUserId(userId));
        return stats;
    }

    // ==================== 私有辅助方法 ====================

    private String truncateMessage(String content) {
        if (content == null || content.length() <= MAX_MESSAGE_LENGTH) {
            return content;
        }
        return content.substring(0, MAX_MESSAGE_LENGTH) + "...";
    }

    private String generateInitialQuestion(Discussion discussion, String initialThought) {
        String topic = discussion.getTitle();
        String context = discussion.getContent();

        String prompt = buildInitialQuestionPrompt(topic, context, initialThought);

        try {
            String response = aiGenerationService.generateContent(prompt, "deepseek-chat");
            return extractQuestionFromResponse(response);
        } catch (Exception e) {
            log.error("生成初始问题失败", e);
            return "关于\"" + topic + "\"，你持什么样的观点？能详细阐述一下你的想法吗？";
        }
    }

    private SocraticMessage generateAiResponse(SocraticDialogue dialogue, SocraticMessage userMessage) {
        // 获取对话历史
        List<SocraticMessage> history = messageRepository.findByDialogueIdOrderBySequenceNumberAsc(dialogue.getId());

        // 构建苏格拉底式追问Prompt
        String prompt = buildSocraticPrompt(dialogue, history, userMessage);

        int retryCount = 0;
        while (retryCount < MAX_RETRY_COUNT) {
            try {
                long startTime = System.currentTimeMillis();
                String aiResponse = aiGenerationService.generateContent(prompt, "deepseek-chat");
                long responseTime = (System.currentTimeMillis() - startTime) / 1000;

                // 解析AI响应
                SocraticResponse parsed = parseSocraticResponse(aiResponse);

                // 确定消息类型
                SocraticMessage.MessageType messageType = determineMessageType(dialogue.getCurrentRound(), parsed.depthLevel);

                Integer sequenceNum = messageRepository.findMaxSequenceNumberByDialogueId(dialogue.getId()) + 1;

                return createAiMessage(
                        dialogue.getId(),
                        dialogue.getCurrentRound(),
                        parsed.question,
                        messageType,
                        parsed.depthLevel,
                        dialogue.getCurrentRound() > 0,
                        parsed.thinkingHints,
                        parsed.analysis,
                        responseTime,
                        parsed.estimatedTokens
                );

            } catch (Exception e) {
                log.error("生成AI响应失败，重试次数: {}", retryCount + 1, e);
                retryCount++;
            }
        }

        // 使用默认追问
        return createDefaultFollowUp(dialogue);
    }

    private SocraticMessage createUserMessage(Long dialogueId, Integer round, String content, Integer sequenceNum) {
        SocraticMessage message = new SocraticMessage();
        message.setDialogueId(dialogueId);
        message.setRound(round);
        message.setRole(SocraticMessage.MessageRole.USER);
        message.setContent(content);
        message.setType(SocraticMessage.MessageType.USER_RESPONSE);
        message.setSequenceNumber(sequenceNum);
        message.setDepthLevel(1);
        message.setIsFollowUp(false);

        // 提取关键点（异步或简化处理）
        message.setKeyPointsExtracted(extractKeyPoints(content));

        return messageRepository.save(message);
    }

    private SocraticMessage createAiMessage(Long dialogueId, Integer round, String content,
                                            SocraticMessage.MessageType type, Integer depthLevel,
                                            boolean isFollowUp, String thinkingHints) {
        return createAiMessage(dialogueId, round, content, type, depthLevel, isFollowUp,
                thinkingHints, null, 0, 0);
    }

    private SocraticMessage createAiMessage(Long dialogueId, Integer round, String content,
                                            SocraticMessage.MessageType type, Integer depthLevel,
                                            boolean isFollowUp, String thinkingHints,
                                            String analysis, long responseTime, int tokenCount) {
        Integer sequenceNum = messageRepository.findMaxSequenceNumberByDialogueId(dialogueId) + 1;

        SocraticMessage message = new SocraticMessage();
        message.setDialogueId(dialogueId);
        message.setRound(round);
        message.setRole(SocraticMessage.MessageRole.AI);
        message.setContent(content);
        message.setType(type);
        message.setDepthLevel(depthLevel);
        message.setIsFollowUp(isFollowUp);
        message.setThinkingHints(thinkingHints);
        message.setAiAnalysis(analysis);
        message.setResponseTimeSeconds((int) responseTime);
        message.setTokenCount(tokenCount);
        message.setSequenceNumber(sequenceNum);

        return messageRepository.save(message);
    }

    private SocraticMessage createDefaultFollowUp(SocraticDialogue dialogue) {
        String[] defaultQuestions = {
                "你能具体说明一下你的想法吗？",
                "为什么你这样认为？",
                "还有其他可能性吗？",
                "这个观点的前提条件是什么？",
                "如果情况发生变化，你的看法会改变吗？"
        };

        String question = defaultQuestions[dialogue.getCurrentRound() % defaultQuestions.length];

        Integer sequenceNum = messageRepository.findMaxSequenceNumberByDialogueId(dialogue.getId()) + 1;

        return createAiMessage(
                dialogue.getId(),
                dialogue.getCurrentRound(),
                question,
                SocraticMessage.MessageType.FOLLOW_UP_QUESTION,
                Math.min(dialogue.getCurrentRound(), 5),
                true,
                "尝试从不同的角度思考这个问题"
        );
    }

    // ==================== Prompt构建方法 ====================

    private String buildInitialQuestionPrompt(String topic, String context, String initialThought) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位苏格拉底式对话引导者。请根据以下讨论主题，生成一个引人深思的开放性问题，引导用户开始深度思考。\n\n");
        prompt.append("讨论主题：").append(topic).append("\n");
        prompt.append("讨论背景：").append(context != null ? context.substring(0, Math.min(context.length(), 500)) : "").append("\n\n");

        if (initialThought != null && !initialThought.isEmpty()) {
            prompt.append("用户的初步想法：").append(initialThought).append("\n\n");
        }

        prompt.append("请生成一个问题，要求：\n");
        prompt.append("1. 开放性问题，不能用\"是/否\"回答\n");
        prompt.append("2. 引导用户阐述自己的观点\n");
        prompt.append("3. 与讨论主题相关但不过于狭窄\n");
        prompt.append("4. 语气友好、鼓励思考\n\n");
        prompt.append("只输出问题本身，不要有任何前缀或解释。");

        return prompt.toString();
    }

    private String buildSocraticPrompt(SocraticDialogue dialogue, List<SocraticMessage> history, SocraticMessage userMessage) {
        StringBuilder prompt = new StringBuilder();

        // 系统角色设定
        prompt.append("你是苏格拉底式对话的AI引导者。你的任务是通过精心设计的追问，帮助用户深入思考问题，发现认知盲点，澄清概念，最终形成更深刻的理解。\n\n");
        prompt.append("核心原则：\n");
        prompt.append("1. 不直接给出答案，而是通过提问引导用户自己思考\n");
        prompt.append("2. 追问应该基于用户的回答，体现连贯性\n");
        prompt.append("3. 逐步深入，从表层观点到深层前提\n");
        prompt.append("4. 尊重用户的思考，鼓励而非否定\n\n");

        // 对话历史
        prompt.append("=== 对话历史 ===\n");
        for (SocraticMessage msg : history) {
            String role = msg.isFromAi() ? "AI" : "用户";
            prompt.append(role).append("：").append(msg.getContent()).append("\n");
        }
        prompt.append("\n");

        // 当前轮次信息
        prompt.append("当前轮次：").append(dialogue.getCurrentRound()).append("/").append(dialogue.getMaxRounds()).append("\n");
        prompt.append("追问深度：建议").append(calculateTargetDepth(dialogue.getCurrentRound())).append("级深度\n\n");

        // 追问策略
        prompt.append("=== 追问策略 ===\n");
        if (dialogue.getCurrentRound() == 0) {
            prompt.append("这是第一轮，请先确认理解用户的观点，然后提出一个澄清性问题。\n");
        } else if (dialogue.getCurrentRound() < dialogue.getMaxRounds() / 2) {
            prompt.append("探索阶段：深入挖掘用户的思考过程，询问具体例子和依据。\n");
        } else if (dialogue.getCurrentRound() < dialogue.getMaxRounds() - 1) {
            prompt.append("深化阶段：挑战潜在前提，探索不同视角，提出反例思考。\n");
        } else {
            prompt.append("反思阶段：引导用户总结思考过程，连接不同观点，准备结束对话。\n");
        }
        prompt.append("\n");

        // 输出格式要求
        prompt.append("=== 输出格式（JSON） ===\n");
        prompt.append("{\n");
        prompt.append("  \"question\": \"追问内容（150字以内）\",\n");
        prompt.append("  \"thinkingHints\": \"给用户的书面思考提示（50字以内）\",\n");
        prompt.append("  \"analysis\": \"对用户上一条回答的简要分析（50字以内）\",\n");
        prompt.append("  \"depthLevel\": 追问深度等级1-5\n");
        prompt.append("}\n\n");
        prompt.append("追问类型选择：澄清问题、深化问题、挑战问题、反思问题。\n");
        prompt.append("请确保JSON格式正确，不要包含markdown代码块标记。");

        return prompt.toString();
    }

    private String buildInsightPrompt(SocraticDialogue dialogue, List<SocraticMessage> messages) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("作为苏格拉底式对话的AI引导者，请基于以下完整对话，生成一份深度洞察总结。\n\n");
        prompt.append("=== 完整对话 ===\n");
        for (SocraticMessage msg : messages) {
            String role = msg.isFromAi() ? "AI引导" : "用户思考";
            prompt.append(role).append("：").append(msg.getContent()).append("\n");
        }
        prompt.append("\n");

        prompt.append("=== 输出格式（JSON） ===\n");
        prompt.append("{\n");
        prompt.append("  \"coreInsight\": \"核心洞察：用户在思考过程中最关键的认识突破（200字以内）\",\n");
        prompt.append("  \"thinkingEvolution\": [\n");
        prompt.append("    {\"stage\": 1, \"description\": \"初始阶段\", \"userThinking\": \"...\", \"aiGuidance\": \"...\"},\n");
        prompt.append("    {\"stage\": 2, \"description\": \"探索阶段\", \"userThinking\": \"...\", \"aiGuidance\": \"...\"}\n");
        prompt.append("  ],\n");
        prompt.append("  \"turningPoints\": [\n");
        prompt.append("    {\"round\": X, \"description\": \"转折点描述\", \"beforeAfter\": \"前后对比\"}\n");
        prompt.append("  ],\n");
        prompt.append("  \"unresolvedQuestions\": [\"未解决的问题1\", \"未解决的问题2\"],\n");
        prompt.append("  \"reflectionSuggestion\": \"给用户的深度反思建议（100字以内）\",\n");
        prompt.append("  \"thinkingDepthScore\": 思考深度评分1-10\n");
        prompt.append("}\n\n");
        prompt.append("请确保JSON格式正确。");

        return prompt.toString();
    }

    // ==================== 解析和转换方法 ====================

    private String extractQuestionFromResponse(String response) {
        if (response == null || response.isEmpty()) {
            return "";
        }
        // 移除可能的markdown标记
        String cleaned = response.replaceAll("```.*?\\n?", "").trim();
        // 移除引号
        cleaned = cleaned.replaceAll("^\"|\"$", "").trim();
        return cleaned;
    }

    private SocraticResponse parseSocraticResponse(String response) {
        SocraticResponse result = new SocraticResponse();
        result.question = "能进一步阐述你的想法吗？";
        result.thinkingHints = "从不同的角度思考";
        result.analysis = "";
        result.depthLevel = 2;
        result.estimatedTokens = 0;

        try {
            // 提取JSON部分
            String json = extractJsonFromResponse(response);
            if (json != null) {
                JsonNode node = objectMapper.readTree(json);
                if (node.has("question")) {
                    result.question = node.get("question").asText();
                }
                if (node.has("thinkingHints")) {
                    result.thinkingHints = node.get("thinkingHints").asText();
                }
                if (node.has("analysis")) {
                    result.analysis = node.get("analysis").asText();
                }
                if (node.has("depthLevel")) {
                    result.depthLevel = Math.max(1, Math.min(5, node.get("depthLevel").asInt()));
                }
            }
        } catch (Exception e) {
            log.error("解析AI响应失败", e);
            // 使用原始响应作为问题
            result.question = response.length() > 300 ? response.substring(0, 300) + "..." : response;
        }

        return result;
    }

    private String extractJsonFromResponse(String response) {
        if (response == null || response.isEmpty()) {
            return null;
        }

        // 移除markdown代码块
        String cleaned = response.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();

        // 查找JSON对象
        int startIndex = cleaned.indexOf("{");
        int endIndex = cleaned.lastIndexOf("}");

        if (startIndex >= 0 && endIndex > startIndex) {
            return cleaned.substring(startIndex, endIndex + 1);
        }

        return cleaned;
    }

    private String extractKeyPoints(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        // 简化的关键点提取逻辑
        String[] sentences = content.split("[。！？\\.]");
        StringBuilder keyPoints = new StringBuilder();
        for (String sentence : sentences) {
            if (sentence.length() > 10 && sentence.length() < 100) {
                keyPoints.append("• ").append(sentence.trim()).append("\n");
            }
        }
        return keyPoints.toString();
    }

    private Integer calculateTargetDepth(int currentRound) {
        // 随着轮次增加，深度逐渐增加
        if (currentRound <= 1) return 1;
        if (currentRound <= 2) return 2;
        if (currentRound <= 3) return 3;
        if (currentRound <= 4) return 4;
        return 5;
    }

    private SocraticMessage.MessageType determineMessageType(int round, int depthLevel) {
        if (round == 0) {
            return SocraticMessage.MessageType.INITIAL_QUESTION;
        }
        if (round >= 4) {
            return SocraticMessage.MessageType.REFLECTIVE_QUESTION;
        }
        if (depthLevel >= 4) {
            return SocraticMessage.MessageType.CHALLENGE_QUESTION;
        }
        if (depthLevel >= 3) {
            return SocraticMessage.MessageType.DEEPENING_QUESTION;
        }
        if (depthLevel >= 2) {
            return SocraticMessage.MessageType.CLARIFYING_QUESTION;
        }
        return SocraticMessage.MessageType.FOLLOW_UP_QUESTION;
    }

    private String generateInsight(SocraticDialogue dialogue, List<SocraticMessage> messages) {
        String prompt = buildInsightPrompt(dialogue, messages);

        try {
            String response = aiGenerationService.generateContent(prompt, "deepseek-chat");
            String json = extractJsonFromResponse(response);
            if (json != null) {
                JsonNode node = objectMapper.readTree(json);
                if (node.has("coreInsight")) {
                    return node.get("coreInsight").asText();
                }
            }
        } catch (Exception e) {
            log.error("生成洞察失败", e);
        }

        // 默认洞察
        return "在这次对话中，你通过苏格拉底式的追问逐步深入思考了问题。虽然没有产生突破性的认知变化，但思考过程本身就具有价值。建议你在今后的思考中继续保持质疑和反思的习惯。";
    }

    private SocraticInsightResponse buildInsightResponse(SocraticDialogue dialogue, List<SocraticMessage> messages) {
        SocraticInsightResponse response = new SocraticInsightResponse();
        response.setId(dialogue.getId());
        response.setDialogueId(dialogue.getId());
        response.setDiscussionId(dialogue.getDiscussionId());
        response.setCoreInsight(dialogue.getFinalInsight());
        response.setGeneratedAt(dialogue.getInsightGeneratedAt());

        // 构建思考演变
        List<SocraticInsightResponse.ThinkingEvolution> evolution = new ArrayList<>();
        int stage = 1;
        SocraticMessage lastUserMsg = null;
        SocraticMessage lastAiMsg = null;

        for (SocraticMessage msg : messages) {
            if (msg.isFromUser()) {
                lastUserMsg = msg;
            } else {
                lastAiMsg = msg;
            }

            if (lastUserMsg != null && lastAiMsg != null) {
                SocraticInsightResponse.ThinkingEvolution te = new SocraticInsightResponse.ThinkingEvolution();
                te.setStage(stage++);
                te.setDescription("第" + msg.getRound() + "轮思考");
                te.setUserThinking(lastUserMsg.getContent());
                te.setAiGuidance(lastAiMsg.getContent());
                evolution.add(te);
                lastUserMsg = null;
            }
        }
        response.setThinkingEvolution(evolution);

        // 构建统计
        SocraticInsightResponse.RoundStats stats = new SocraticInsightResponse.RoundStats();
        stats.setTotalRounds(dialogue.getCurrentRound());
        stats.setAvgResponseLength(150); // 简化计算
        stats.setThinkingDepthScore(Math.min(10, dialogue.getCurrentRound() * 2));
        response.setRoundStats(stats);

        return response;
    }

    private String buildInitialThinkingHints(String topic) {
        return "思考你对" + topic + "的真实看法，尝试表达你的核心观点";
    }

    // ==================== 响应转换方法 ====================

    private SocraticDialogueResponse convertToDialogueResponse(SocraticDialogue dialogue, SocraticMessage lastMessage) {
        SocraticDialogueResponse response = new SocraticDialogueResponse();
        response.setId(dialogue.getId());
        response.setDiscussionId(dialogue.getDiscussionId());
        response.setStatus(dialogue.getStatus().name());
        response.setCurrentRound(dialogue.getCurrentRound());
        response.setMaxRounds(dialogue.getMaxRounds());
        response.setInitialQuestion(dialogue.getInitialQuestion());
        response.setFinalInsight(dialogue.getFinalInsight());
        response.setTotalMessages(dialogue.getTotalMessages());
        response.setCanContinue(dialogue.canContinue());
        response.setRemainingRounds(dialogue.getMaxRounds() - dialogue.getCurrentRound());
        response.setLastMessageAt(dialogue.getLastMessageAt());
        response.setCreatedAt(dialogue.getCreatedAt());

        // 获取讨论标题
        discussionRepository.findById(dialogue.getDiscussionId())
                .ifPresent(d -> response.setDiscussionTitle(d.getTitle()));

        if (lastMessage != null) {
            response.setLastMessage(convertToMessageResponse(lastMessage));
        }

        return response;
    }

    @Override
    @Transactional
    public Long saveInsightAsCard(Long userId, Long dialogueId) {
        log.info("用户 {} 将对话 {} 的洞察保存到语料库", userId, dialogueId);

        SocraticDialogue dialogue = dialogueRepository.findById(dialogueId)
                .orElseThrow(() -> new ResourceNotFoundException("对话不存在"));

        if (!dialogue.getUserId().equals(userId)) {
            throw new BusinessException("无权访问此对话");
        }

        // 获取讨论信息
        Discussion discussion = discussionRepository.findById(dialogue.getDiscussionId())
                .orElse(null);

        // 获取所有消息
        List<SocraticMessage> messages = messageRepository.findByDialogueIdOrderBySequenceNumberAsc(dialogueId);

        // 构建对话过程摘要
        StringBuilder dialogueSummary = new StringBuilder();
        for (int i = 0; i < messages.size(); i += 2) {
            if (i < messages.size()) {
                SocraticMessage aiMsg = messages.get(i);
                if (aiMsg.isFromAi()) {
                    dialogueSummary.append("**AI追问：** ").append(aiMsg.getContent()).append("\n\n");
                }
            }
            if (i + 1 < messages.size()) {
                SocraticMessage userMsg = messages.get(i + 1);
                if (userMsg.isFromUser()) {
                    dialogueSummary.append("**我的思考：** ").append(userMsg.getContent()).append("\n\n");
                }
            }
        }
        dialogueSummary.append("\n---\n");
        dialogueSummary.append("*来自苏格拉底式对话，共").append(dialogue.getCurrentRound()).append("轮思考*");

        // 构建标题
        String title = discussion != null
                ? "思考：" + discussion.getTitle()
                : "苏格拉底式对话洞察 (" + dialogue.getCreatedAt().toLocalDate() + ")";

        // 保存到用户语料库（而非 Card）
        UserCorpus corpus = userCorpusService.createFromSocraticDialogue(
                userId,
                dialogueId,
                dialogue.getDiscussionId(),
                title,
                dialogue.getFinalInsight(),
                dialogueSummary.toString()
        );

        log.info("洞察已保存到语料库，corpusId: {}, userId: {}", corpus.getId(), userId);
        return corpus.getId();
    }

    private SocraticMessageResponse convertToMessageResponse(SocraticMessage message) {
        SocraticMessageResponse response = new SocraticMessageResponse();
        response.setId(message.getId());
        response.setDialogueId(message.getDialogueId());
        response.setRound(message.getRound());
        response.setRole(message.getRole().name());
        response.setContent(message.getContent());
        response.setType(message.getType().name());
        response.setTypeDescription(message.getType().getDescription());
        response.setDepthLevel(message.getDepthLevel());
        response.setIsFollowUp(message.getIsFollowUp());
        response.setThinkingHints(message.getThinkingHints());
        response.setAiAnalysis(message.getAiAnalysis());
        response.setKeyPointsExtracted(message.getKeyPointsExtracted());
        response.setIsFinalSummary(message.getIsFinalSummary());
        response.setSequenceNumber(message.getSequenceNumber());
        response.setCreatedAt(message.getCreatedAt());

        return response;
    }

    // ==================== 内部类 ====================

    private static class SocraticResponse {
        String question;
        String thinkingHints;
        String analysis;
        int depthLevel;
        int estimatedTokens;
    }
}
