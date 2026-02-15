package com.evomind.service.impl;

import com.evomind.entity.*;
import com.evomind.repository.*;
import com.evomind.service.PrivacyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 隐私与数据权利服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PrivacyServiceImpl implements PrivacyService {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final UserCorpusRepository userCorpusRepository;
    private final SocraticDialogueRepository socraticDialogueRepository;
    private final VoiceNoteRepository voiceNoteRepository;
    private final InfoSourceRepository infoSourceRepository;
    private final UserCognitiveProfileRepository cognitiveProfileRepository;
    private final CognitiveConflictRepository cognitiveConflictRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String DELETION_TOKEN_PREFIX = "account:deletion:";
    private static final long DELETION_COOLDOWN_DAYS = 7;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> exportUserData(Long userId) {
        log.info("Exporting user data for userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Map<String, Object> userData = new LinkedHashMap<>();

        // 1. 用户基本信息（脱敏）
        userData.put("userInfo", Map.of(
            "phone", maskPhone(user.getPhone()),
            "nickname", user.getNickname() != null ? user.getNickname() : "",
            "avatar", user.getAvatar() != null ? user.getAvatar() : "",
            "createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : "",
            "lastLoginAt", user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : ""
        ));

        // 2. 认知卡片
        List<Card> cards = cardRepository.findByUserId(userId);
        userData.put("cards", cards.stream().map(this::maskCard).collect(Collectors.toList()));

        // 3. 语料库
        List<UserCorpus> corpusItems = userCorpusRepository.findByUserId(userId);
        userData.put("corpus", corpusItems.stream().map(this::maskCorpus).collect(Collectors.toList()));

        // 4. 苏格拉底对话
        List<SocraticDialogue> dialogues = socraticDialogueRepository.findByUserIdOrderByCreatedAtDesc(userId);
        userData.put("socraticDialogues", dialogues.stream().map(this::maskDialogue).collect(Collectors.toList()));

        // 5. 语音笔记
        List<VoiceNote> voiceNotes = voiceNoteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        userData.put("voiceNotes", voiceNotes.stream().map(this::maskVoiceNote).collect(Collectors.toList()));

        // 6. 信息源
        List<InfoSource> sources = infoSourceRepository.findByUserId(userId);
        userData.put("infoSources", sources.stream().map(this::maskSource).collect(Collectors.toList()));

        // 7. 认知画像
        List<UserCognitiveProfile> profiles = cognitiveProfileRepository.findByUserId(userId);
        userData.put("cognitiveProfiles", profiles.stream().map(this::maskProfile).collect(Collectors.toList()));

        // 8. 认知冲突
        List<CognitiveConflict> conflicts = cognitiveConflictRepository.findByUserId(userId);
        userData.put("cognitiveConflicts", conflicts.stream().map(this::maskConflict).collect(Collectors.toList()));

        // 9. 导出元数据
        userData.put("exportMetadata", Map.of(
            "exportedAt", LocalDateTime.now().toString(),
            "dataVersion", "1.0",
            "totalRecords", cards.size() + corpusItems.size() + dialogues.size() + 
                          voiceNotes.size() + sources.size() + profiles.size() + conflicts.size()
        ));

        log.info("User data exported successfully for userId={}, total records={}", 
                userId, userData.get("exportMetadata"));

        return userData;
    }

    @Override
    public String requestAccountDeletion(Long userId) {
        log.info("Account deletion requested for userId={}", userId);

        // 检查是否已存在注销申请
        String existingToken = redisTemplate.opsForValue().get(DELETION_TOKEN_PREFIX + userId);
        if (existingToken != null) {
            throw new RuntimeException("您已提交注销申请，请在冷静期结束后确认或取消");
        }

        // 生成注销令牌
        String deletionToken = UUID.randomUUID().toString();
        String key = DELETION_TOKEN_PREFIX + userId;
        
        // 存储到Redis，设置7天过期
        redisTemplate.opsForValue().set(key, deletionToken, DELETION_COOLDOWN_DAYS, TimeUnit.DAYS);

        log.info("Account deletion token generated for userId={}, token={}", userId, deletionToken);
        return deletionToken;
    }

    @Override
    @Transactional
    public void confirmAccountDeletion(Long userId, String deletionToken) {
        log.info("Confirming account deletion for userId={}", userId);

        // 验证令牌
        String key = DELETION_TOKEN_PREFIX + userId;
        String storedToken = redisTemplate.opsForValue().get(key);
        
        if (storedToken == null) {
            throw new RuntimeException("注销申请已过期或不存在，请重新申请");
        }
        
        if (!storedToken.equals(deletionToken)) {
            throw new RuntimeException("注销令牌无效");
        }

        // 执行软删除（将用户标记为已注销，保留数据一段时间）
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setEnabled(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // 删除Redis中的令牌
        redisTemplate.delete(key);

        log.info("Account deleted successfully for userId={}", userId);
    }

    @Override
    public void cancelAccountDeletion(Long userId) {
        log.info("Cancelling account deletion for userId={}", userId);

        String key = DELETION_TOKEN_PREFIX + userId;
        Boolean deleted = redisTemplate.delete(key);
        
        if (Boolean.FALSE.equals(deleted)) {
            throw new RuntimeException("没有待处理的注销申请");
        }

        log.info("Account deletion cancelled for userId={}", userId);
    }

    @Override
    public Map<String, Object> getExportHistory(Long userId) {
        // 简化实现：返回最近一次导出信息
        return Map.of(
            "exports", List.of(),
            "message", "数据导出历史功能待实现"
        );
    }

    // ============ 数据脱敏方法 ============

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private Map<String, Object> maskCard(Card card) {
        return Map.of(
            "id", card.getId(),
            "title", card.getTitle(),
            "content", truncateText(card.getContent(), 200),
            "tags", card.getTags(),
            "createdAt", card.getCreatedAt() != null ? card.getCreatedAt().toString() : ""
        );
    }

    private Map<String, Object> maskCorpus(UserCorpus corpus) {
        return Map.of(
            "id", corpus.getId(),
            "content", truncateText(corpus.getContent(), 200),
            "tags", corpus.getTags(),
            "sourceType", corpus.getSourceType(),
            "createdAt", corpus.getCreatedAt() != null ? corpus.getCreatedAt().toString() : ""
        );
    }

    private Map<String, Object> maskDialogue(SocraticDialogue dialogue) {
        return Map.of(
            "id", dialogue.getId(),
            "topic", dialogue.getTopic(),
            "status", dialogue.getStatus(),
            "messageCount", dialogue.getMessages() != null ? dialogue.getMessages().size() : 0,
            "createdAt", dialogue.getCreatedAt() != null ? dialogue.getCreatedAt().toString() : ""
        );
    }

    private Map<String, Object> maskVoiceNote(VoiceNote note) {
        return Map.of(
            "id", note.getId(),
            "transcription", truncateText(note.getTranscription(), 100),
            "durationSeconds", note.getDurationSeconds(),
            "createdAt", note.getCreatedAt() != null ? note.getCreatedAt().toString() : ""
        );
    }

    private Map<String, Object> maskSource(InfoSource source) {
        return Map.of(
            "id", source.getId(),
            "name", source.getName(),
            "platform", source.getPlatform(),
            "url", source.getUrl(),
            "createdAt", source.getCreatedAt() != null ? source.getCreatedAt().toString() : ""
        );
    }

    private Map<String, Object> maskProfile(UserCognitiveProfile profile) {
        return Map.of(
            "id", profile.getId(),
            "topic", profile.getTopic(),
            "beliefType", profile.getBeliefType(),
            "createdAt", profile.getCreatedAt() != null ? profile.getCreatedAt().toString() : ""
        );
    }

    private Map<String, Object> maskConflict(CognitiveConflict conflict) {
        return Map.of(
            "id", conflict.getId(),
            "conflictType", conflict.getConflictType(),
            "severity", conflict.getSeverity(),
            "status", conflict.getStatus(),
            "createdAt", conflict.getCreatedAt() != null ? conflict.getCreatedAt().toString() : ""
        );
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}
