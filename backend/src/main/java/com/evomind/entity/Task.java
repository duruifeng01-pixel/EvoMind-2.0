package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter
@Setter
public class Task extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(length = 20)
    private String stage;

    @Column(length = 10)
    private String difficulty;

    @Column(length = 20)
    private String status = "PENDING";

    @Column(name = "deadline_at")
    private LocalDateTime deadlineAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "reward_points")
    private Integer rewardPoints = 0;

    @Column(name = "related_card_id")
    private Long relatedCardId;

    @Column
    private Boolean reminderSent = false;
}
