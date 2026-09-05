package com.swyp.FinQ.content.domain;

import com.swyp.FinQ.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "content")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Content extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "content_id")
    private Long id;

    @Column(name = "content_code", nullable = false, unique = true, length = 50)
    private String contentCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "source", length = 200)
    private String source;

    @Column(name = "reference_date")
    private LocalDate referenceDate;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    // JSON 구조: {title, description, additionalDescription, imageUrl, tableData}
    /* additionalDescription - SUMMARY
       imageUrl - CASE, COMPARISON
       tableData - COMPARISON */
    @Column(name = "body_data", columnDefinition = "json")
    private String bodyData;

    @Column(name = "summary_content", columnDefinition = "TEXT")
    private String summaryContent;

    @Column(name = "is_premium", nullable = false)
    private Boolean isPremium;
}