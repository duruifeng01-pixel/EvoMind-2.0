package com.evomind.service.impl;

import com.evomind.entity.SensitiveWord;
import com.evomind.repository.SensitiveWordRepository;
import com.evomind.service.SensitiveWordService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * 敏感词服务实现
 * 使用DFA算法实现高效敏感词检测
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveWordServiceImpl implements SensitiveWordService {

    private final SensitiveWordRepository sensitiveWordRepository;

    // DFA词库缓存：key=敏感词分类, value=DFA树
    private volatile Map<SensitiveWord.WordCategory, Map> dfaCache = new ConcurrentHashMap<>();
    
    // 所有敏感词缓存（用于快速查找）
    private volatile Set<String> wordCache = ConcurrentHashMap.newKeySet();
    
    // 缓存最后更新时间
    private volatile LocalDateTime cacheLastUpdate = LocalDateTime.MIN;

    /**
     * 初始化DFA缓存
     */
    @PostConstruct
    public void initCache() {
        refreshCache();
        // 初始化系统预设敏感词
        initSystemWords();
    }

    /**
     * 刷新缓存
     */
    public synchronized void refreshCache() {
        log.info("Refreshing sensitive word cache...");
        List<SensitiveWord> words = sensitiveWordRepository.findAllEnabled();
        
        Map<SensitiveWord.WordCategory, Map> newDfaCache = new ConcurrentHashMap<>();
        Set<String> newWordCache = ConcurrentHashMap.newKeySet();
        
        // 按分类分组构建DFA
        Map<SensitiveWord.WordCategory, List<SensitiveWord>> wordsByCategory = words.stream()
                .collect(Collectors.groupingBy(SensitiveWord::getCategory));
        
        for (Map.Entry<SensitiveWord.WordCategory, List<SensitiveWord>> entry : wordsByCategory.entrySet()) {
            Map dfa = buildDFA(entry.getValue());
            newDfaCache.put(entry.getKey(), dfa);
            entry.getValue().forEach(w -> newWordCache.add(w.getWord()));
        }
        
        this.dfaCache = newDfaCache;
        this.wordCache = newWordCache;
        this.cacheLastUpdate = LocalDateTime.now();
        
        log.info("Sensitive word cache refreshed. Total words: {}", words.size());
    }

    /**
     * 构建DFA（确定性有限自动机）
     */
    private Map buildDFA(List<SensitiveWord> words) {
        Map root = new HashMap();
        
        for (SensitiveWord word : words) {
            String text = word.getWord();
            if (text == null || text.isEmpty()) continue;
            
            Map current = root;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                Map node = (Map) current.get(c);
                if (node == null) {
                    node = new HashMap();
                    current.put(c, node);
                }
                current = node;
            }
            // 标记结束节点，存储敏感词ID和级别
            current.put("end", true);
            current.put("wordId", word.getId());
            current.put("word", word.getWord());
            current.put("category", word.getCategory());
            current.put("level", word.getLevel());
        }
        
        return root;
    }

    @Override
    @Transactional
    public SensitiveWord addWord(String word, SensitiveWord.WordCategory category,
                                  SensitiveWord.SensitiveLevel level, SensitiveWord.MatchMode matchMode,
                                  String description, Long createdBy) {
        // 检查是否已存在
        if (sensitiveWordRepository.existsByWord(word)) {
            throw new IllegalArgumentException("敏感词已存在: " + word);
        }
        
        SensitiveWord sensitiveWord = SensitiveWord.builder()
                .word(word)
                .category(category)
                .level(level)
                .matchMode(matchMode != null ? matchMode : SensitiveWord.MatchMode.CONTAINS)
                .description(description)
                .enabled(true)
                .source(createdBy == 0 ? SensitiveWord.WordSource.SYSTEM : SensitiveWord.WordSource.MANUAL)
                .createdBy(createdBy)
                .hitCount(0L)
                .build();
        
        SensitiveWord saved = sensitiveWordRepository.save(sensitiveWord);
        refreshCache();
        
        log.info("Added sensitive word: {} (category: {}, level: {})", word, category, level);
        return saved;
    }

    @Override
    @Transactional
    public SensitiveWord updateWord(Long id, Boolean enabled, 
                                     SensitiveWord.SensitiveLevel level, String description) {
        SensitiveWord word = sensitiveWordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("敏感词不存在: " + id));
        
        if (enabled != null) word.setEnabled(enabled);
        if (level != null) word.setLevel(level);
        if (description != null) word.setDescription(description);
        
        SensitiveWord saved = sensitiveWordRepository.save(word);
        refreshCache();
        
        return saved;
    }

    @Override
    @Transactional
    public void deleteWord(Long id) {
        SensitiveWord word = sensitiveWordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("敏感词不存在: " + id));
        
        // 系统预设敏感词不允许删除
        if (word.getSource() == SensitiveWord.WordSource.SYSTEM) {
            throw new IllegalStateException("系统预设敏感词不能删除");
        }
        
        sensitiveWordRepository.deleteById(id);
        refreshCache();
        
        log.info("Deleted sensitive word: {}", word.getWord());
    }

    @Override
    @Transactional
    public Map<String, Object> batchImport(List<String> words, SensitiveWord.WordCategory category,
                                           SensitiveWord.SensitiveLevel level, Long createdBy) {
        int added = 0;
        int skipped = 0;
        int failed = 0;
        
        for (String word : words) {
            if (word == null || word.trim().isEmpty()) continue;
            
            word = word.trim();
            
            if (sensitiveWordRepository.existsByWord(word)) {
                skipped++;
                continue;
            }
            
            try {
                addWord(word, category, level, SensitiveWord.MatchMode.CONTAINS, null, createdBy);
                added++;
            } catch (Exception e) {
                failed++;
                log.warn("Failed to add sensitive word: {}", word, e);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("added", added);
        result.put("skipped", skipped);
        result.put("failed", failed);
        result.put("total", words.size());
        
        return result;
    }

    @Override
    public boolean containsSensitiveWord(String text) {
        if (text == null || text.isEmpty()) return false;
        return !findSensitiveWords(text).isEmpty();
    }

    @Override
    public List<SensitiveWordHitResult> findSensitiveWords(String text) {
        List<SensitiveWordHitResult> results = new ArrayList<>();
        if (text == null || text.isEmpty()) return results;
        
        // 检查缓存是否需要刷新（每5分钟检查一次）
        if (LocalDateTime.now().minusMinutes(5).isAfter(cacheLastUpdate)) {
            refreshCache();
        }
        
        // 遍历所有分类的DFA
        for (Map.Entry<SensitiveWord.WordCategory, Map> entry : dfaCache.entrySet()) {
            List<SensitiveWordHitResult> categoryResults = findInDFA(text, entry.getValue(), entry.getKey());
            results.addAll(categoryResults);
        }
        
        // 更新命中次数（异步）
        results.forEach(r -> {
            if (r.wordId != null) {
                sensitiveWordRepository.incrementHitCount(r.wordId);
            }
        });
        
        return results;
    }

    /**
     * 在DFA中查找敏感词
     */
    private List<SensitiveWordHitResult> findInDFA(String text, Map dfaRoot, SensitiveWord.WordCategory category) {
        List<SensitiveWordHitResult> results = new ArrayList<>();
        
        for (int i = 0; i < text.length(); i++) {
            Map current = dfaRoot;
            int endIndex = -1;
            Long wordId = null;
            String wordText = null;
            SensitiveWord.SensitiveLevel level = null;
            
            for (int j = i; j < text.length(); j++) {
                char c = text.charAt(j);
                Map node = (Map) current.get(c);
                
                if (node == null) break;
                
                if (node.containsKey("end")) {
                    endIndex = j;
                    wordId = (Long) node.get("wordId");
                    wordText = (String) node.get("word");
                    level = (SensitiveWord.SensitiveLevel) node.get("level");
                }
                
                current = node;
            }
            
            if (endIndex != -1 && wordText != null) {
                List<Position> positions = new ArrayList<>();
                positions.add(new Position(i, endIndex + 1));
                
                SensitiveWordHitResult result = new SensitiveWordHitResult(
                        wordId, wordText, category, level, positions
                );
                results.add(result);
                
                // 跳过已匹配的字符，避免重复检测
                i = endIndex;
            }
        }
        
        return results;
    }

    @Override
    public String replaceSensitiveWords(String text, String replacement) {
        if (text == null || text.isEmpty()) return text;
        
        List<SensitiveWordHitResult> hits = findSensitiveWords(text);
        if (hits.isEmpty()) return text;
        
        StringBuilder result = new StringBuilder(text);
        
        // 按位置倒序替换，避免位置偏移
        hits.sort((a, b) -> {
            int aStart = a.positions.get(0).start;
            int bStart = b.positions.get(0).start;
            return Integer.compare(bStart, aStart);
        });
        
        for (SensitiveWordHitResult hit : hits) {
            for (Position pos : hit.positions) {
                String replaceStr = replacement.repeat(hit.word.length());
                result.replace(pos.start, pos.end, replaceStr);
            }
        }
        
        return result.toString();
    }

    @Override
    public List<SensitiveWord> getAllEnabledWords() {
        return sensitiveWordRepository.findAllEnabled();
    }

    @Override
    public Page<SensitiveWord> searchWords(String keyword, SensitiveWord.WordCategory category, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return sensitiveWordRepository.searchByKeyword(keyword, pageable);
        }
        if (category != null) {
            return sensitiveWordRepository.findByCategoryAndEnabledTrueOrderByLevelDesc(category, pageable);
        }
        return sensitiveWordRepository.findByEnabledTrueOrderByHitCountDesc(pageable);
    }

    @Override
    public SensitiveWord getWordById(Long id) {
        return sensitiveWordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("敏感词不存在: " + id));
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总数统计
        long totalEnabled = sensitiveWordRepository.countByEnabledTrue();
        stats.put("totalEnabled", totalEnabled);
        
        // 按分类统计
        List<Object[]> categoryCounts = sensitiveWordRepository.countByCategory();
        Map<String, Long> categoryStats = new HashMap<>();
        for (Object[] row : categoryCounts) {
            categoryStats.put(((SensitiveWord.WordCategory) row[0]).name(), (Long) row[1]);
        }
        stats.put("byCategory", categoryStats);
        
        // 按级别统计
        List<Object[]> levelCounts = sensitiveWordRepository.countByLevel();
        Map<String, Long> levelStats = new HashMap<>();
        for (Object[] row : levelCounts) {
            levelStats.put(((SensitiveWord.SensitiveLevel) row[0]).name(), (Long) row[1]);
        }
        stats.put("byLevel", levelStats);
        
        return stats;
    }

    @Override
    public List<SensitiveWord> getHotWords(int limit) {
        return sensitiveWordRepository.findTop20ByEnabledTrueOrderByHitCountDesc()
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SensitiveWord toggleEnabled(Long id, boolean enabled) {
        return updateWord(id, enabled, null, null);
    }

    @Override
    @Transactional
    public int toggleCategory(SensitiveWord.WordCategory category, boolean enabled) {
        int count = sensitiveWordRepository.updateEnabledByCategory(category, enabled);
        refreshCache();
        return count;
    }

    @Override
    @Transactional
    public void initSystemWords() {
        // 检查是否已初始化
        long systemWordCount = sensitiveWordRepository.count();
        if (systemWordCount > 0) {
            log.info("System sensitive words already initialized. Count: {}", systemWordCount);
            return;
        }
        
        log.info("Initializing system sensitive words...");
        
        // 政治敏感词
        String[] politicsWords = {
            "反动", "颠覆", "暴乱", "分裂", "独立", "台独", "港独", "藏独", "疆独"
        };
        
        // 色情词
        String[] pornWords = {
            "色情", "淫秽", "嫖娼", "卖淫", "裸聊", "裸照"
        };
        
        // 暴力词
        String[] violenceWords = {
            "杀人", "爆炸", "恐怖袭击", "暴力", "血腥"
        };
        
        // 诈骗词
        String[] fraudWords = {
            "诈骗", "传销", "非法集资", "洗钱", "套现"
        };
        
        // 赌博词
        String[] gamblingWords = {
            "赌博", "博彩", "赌球", "六合彩", "赌场"
        };
        
        // 添加系统预设敏感词
        for (String word : politicsWords) {
            try {
                addWord(word, SensitiveWord.WordCategory.POLITICS, 
                        SensitiveWord.SensitiveLevel.CRITICAL, 
                        SensitiveWord.MatchMode.CONTAINS,
                        "政治敏感词-系统自动添加", 0L);
            } catch (Exception e) {
                log.warn("Failed to add system word: {}", word);
            }
        }
        
        for (String word : pornWords) {
            try {
                addWord(word, SensitiveWord.WordCategory.PORNOGRAPHY, 
                        SensitiveWord.SensitiveLevel.HIGH, 
                        SensitiveWord.MatchMode.CONTAINS,
                        "色情词-系统自动添加", 0L);
            } catch (Exception e) {
                log.warn("Failed to add system word: {}", word);
            }
        }
        
        for (String word : violenceWords) {
            try {
                addWord(word, SensitiveWord.WordCategory.VIOLENCE, 
                        SensitiveWord.SensitiveLevel.HIGH, 
                        SensitiveWord.MatchMode.CONTAINS,
                        "暴力词-系统自动添加", 0L);
            } catch (Exception e) {
                log.warn("Failed to add system word: {}", word);
            }
        }
        
        for (String word : fraudWords) {
            try {
                addWord(word, SensitiveWord.WordCategory.FRAUD, 
                        SensitiveWord.SensitiveLevel.HIGH, 
                        SensitiveWord.MatchMode.CONTAINS,
                        "诈骗词-系统自动添加", 0L);
            } catch (Exception e) {
                log.warn("Failed to add system word: {}", word);
            }
        }
        
        for (String word : gamblingWords) {
            try {
                addWord(word, SensitiveWord.WordCategory.GAMBLING, 
                        SensitiveWord.SensitiveLevel.HIGH, 
                        SensitiveWord.MatchMode.CONTAINS,
                        "赌博词-系统自动添加", 0L);
            } catch (Exception e) {
                log.warn("Failed to add system word: {}", word);
            }
        }
        
        log.info("System sensitive words initialization completed");
    }
}