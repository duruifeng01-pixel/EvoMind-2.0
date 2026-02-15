package com.evomind.repository;

import com.evomind.entity.UserCognitiveProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户认知画像Repository
 */
@Repository
public interface UserCognitiveProfileRepository extends JpaRepository<UserCognitiveProfile, Long> {

    /**
     * 根据用户ID查找所有认知画像
     */
    List<UserCognitiveProfile> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * 根据用户ID和主题查找认知画像
     */
    Optional<UserCognitiveProfile> findByUserIdAndTopic(Long userId, String topic);

    /**
     * 检查用户是否已有某主题的认知画像
     */
    boolean existsByUserIdAndTopic(Long userId, String topic);

    /**
     * 删除用户的所有认知画像
     */
    void deleteByUserId(Long userId);
}
