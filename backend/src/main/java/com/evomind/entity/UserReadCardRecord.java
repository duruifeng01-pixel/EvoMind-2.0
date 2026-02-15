package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户已读内容防重复记录实体
 * 记录用户已读内容，在冷却期内不重复推荐
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_read_card_records")
public class UserReadCardRecord extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "card_id", nullable = false)
    private Long cardId;

    @Column(name = "first_read_at", nullable = false)
    private LocalDateTime firstReadAt;

    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    @Column(name = "read_count")
    private Integer readCount = 1;

    @Column(name = "cool_down_until", nullable = false)
    private LocalDateTime coolDownUntil;

    public UserReadCardRecord() {
        this.firstReadAt = LocalDateTime.now();
    }

    /**
     * 记录阅读
     */
    public void recordRead(int coolDownDays) {
        this.lastReadAt = LocalDateTime.now();
        this.readCount++;
        this.coolDownUntil = LocalDateTime.now().plusDays(coolDownDays);
    }

    /**
     * 检查是否在冷却期内
     */
    public boolean isInCoolDownPeriod() {
        return LocalDateTime.now().isBefore(coolDownUntil);
    }
}
