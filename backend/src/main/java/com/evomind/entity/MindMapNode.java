package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "mindmap_nodes")
@Getter
@Setter
public class MindMapNode extends BaseEntity {

    @Column(name = "card_id", nullable = false)
    private Long cardId;

    @Column(name = "node_id", nullable = false, length = 64)
    private String nodeId;

    @Column(name = "parent_node_id", length = 64)
    private String parentNodeId;

    @Column(nullable = false, length = 200)
    private String text;

    @Column(length = 500)
    private String description;

    @Column(name = "node_type", length = 20)
    private String nodeType = "TOPIC";

    @Column(name = "level")
    private Integer level = 0;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "has_original_reference")
    private Boolean hasOriginalReference = false;

    @Column(name = "original_content_id")
    private Long originalContentId;

    @Column(name = "original_paragraph_index")
    private Integer originalParagraphIndex;

    @Column(name = "is_expanded")
    private Boolean isExpanded = true;

    @Column(name = "style_json", length = 500)
    private String styleJson;
}
