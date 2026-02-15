package com.evomind.service;

import com.evomind.entity.SensitiveWord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 敏感词服务接口
 */
public interface SensitiveWordService {

    /**
     * 添加敏感词
     * 
     * @param word 敏感词内容
     * @param category 分类
     * @param level 敏感级别
     * @param matchMode 匹配模式
     * @param description 描述
     * @param createdBy 创建人ID
     * @return 创建的敏感词
     */
    SensitiveWord addWord(String word, SensitiveWord.WordCategory category,
                          SensitiveWord.SensitiveLevel level, SensitiveWord.MatchMode matchMode,
                          String description, Long createdBy);

    /**
     * 更新敏感词
     * 
     * @param id 敏感词ID
     * @param enabled 是否启用
     * @param level 敏感级别
     * @param description 描述
     * @return 更新后的敏感词
     */
    SensitiveWord updateWord(Long id, Boolean enabled, 
                             SensitiveWord.SensitiveLevel level, String description);

    /**
     * 删除敏感词
     * 
     * @param id 敏感词ID
     */
    void deleteWord(Long id);

    /**
     * 批量导入敏感词
     * 
     * @param words 敏感词列表
     * @param category 分类
     * @param level 敏感级别
     * @param createdBy 创建人ID
     * @return 导入结果统计
     */
    Map<String, Object> batchImport(List<String> words, SensitiveWord.WordCategory category,
                                    SensitiveWord.SensitiveLevel level, Long createdBy);

    /**
     * 检测文本是否包含敏感词
     * 
     * @param text 待检测文本
     * @return 是否包含
     */
    boolean containsSensitiveWord(String text);

    /**
     * 检测文本并返回命中的敏感词
     * 
     * @param text 待检测文本
     * @return 命中的敏感词列表
     */
    List<SensitiveWordHitResult> findSensitiveWords(String text);

    /**
     * 检测文本并替换敏感词
     * 
     * @param text 待检测文本
     * @param replacement 替换字符
     * @return 替换后的文本
     */
    String replaceSensitiveWords(String text, String replacement);

    /**
     * 获取所有启用的敏感词（用于构建DFA）
     * 
     * @return 敏感词列表
     */
    List<SensitiveWord> getAllEnabledWords();

    /**
     * 分页查询敏感词
     * 
     * @param keyword 搜索关键词
     * @param category 分类筛选
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<SensitiveWord> searchWords(String keyword, SensitiveWord.WordCategory category, Pageable pageable);

    /**
     * 根据ID获取敏感词
     * 
     * @param id 敏感词ID
     * @return 敏感词
     */
    SensitiveWord getWordById(Long id);

    /**
     * 获取敏感词统计信息
     * 
     * @return 统计信息
     */
    Map<String, Object> getStatistics();

    /**
     * 获取热点敏感词（命中次数最多）
     * 
     * @param limit 数量限制
     * @return 热点敏感词列表
     */
    List<SensitiveWord> getHotWords(int limit);

    /**
     * 启用/禁用敏感词
     * 
     * @param id 敏感词ID
     * @param enabled 是否启用
     * @return 更新后的敏感词
     */
    SensitiveWord toggleEnabled(Long id, boolean enabled);

    /**
     * 批量启用/禁用分类下的敏感词
     * 
     * @param category 分类
     * @param enabled 是否启用
     * @return 影响数量
     */
    int toggleCategory(SensitiveWord.WordCategory category, boolean enabled);

    /**
     * 初始化系统预设敏感词
     * 在应用启动时调用
     */
    void initSystemWords();

    /**
     * 敏感词命中结果
     */
    class SensitiveWordHitResult {
        public Long wordId;
        public String word;
        public SensitiveWord.WordCategory category;
        public SensitiveWord.SensitiveLevel level;
        public List<Position> positions;

        public SensitiveWordHitResult(Long wordId, String word, SensitiveWord.WordCategory category,
                                      SensitiveWord.SensitiveLevel level, List<Position> positions) {
            this.wordId = wordId;
            this.word = word;
            this.category = category;
            this.level = level;
            this.positions = positions;
        }
    }

    /**
     * 位置信息
     */
    class Position {
        public int start;
        public int end;

        public Position(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}