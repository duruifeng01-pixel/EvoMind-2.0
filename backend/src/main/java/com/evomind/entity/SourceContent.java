package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "source_contents")
@Getter
@Setter
public class SourceContent extends BaseEntity {

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(name = "original_url", length = 512)
    private String originalUrl;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "author", length = 100)
    private String author;

    @Column(name = "content_text", columnDefinition = "longtext")
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String contentText;

    @Column(name = "content_html", columnDefinition = "longtext")
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String contentHtml;

    @Column(name = "paragraph_count")
    private Integer paragraphCount;

    @Column(name = "word_count")
    private Integer wordCount;

    @Column(name = "content_hash", length = 64)
    private String contentHash;

    @Column(name = "fetch_status", length = 20)
    private String fetchStatus = "PENDING";

    @Column(name = "fetch_error", length = 500)
    private String fetchError;

    @Column(name = "is_processed")
    private Boolean isProcessed = false;
}
