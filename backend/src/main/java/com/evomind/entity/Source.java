package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sources")
@Getter
@Setter
public class Source extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String platform;

    @Column(name = "home_url", length = 512)
    private String homeUrl;

    @Column(length = 50)
    private String category;

    @Column(name = "is_pinned")
    private Boolean isPinned = false;

    @Column(name = "last_sync_at")
    private java.time.LocalDateTime lastSyncAt;

    @Column(name = "sync_status", length = 20)
    private String syncStatus;

    @Column(name = "article_count")
    private Integer articleCount = 0;

    @Column
    private Boolean enabled = true;
}
