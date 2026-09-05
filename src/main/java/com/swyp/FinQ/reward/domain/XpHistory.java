package com.swyp.FinQ.reward.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "xp_history", indexes = {
        @Index(name = "idx_xp_history_user", columnList = "user_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_xp_history_user_type_ref", columnNames = {"user_id", "xp_type", "reference_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class)
public class XpHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "xp_history_id")
    private Long id;

    // TODO: User 도메인 생성 후 ManyToOne 연결
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "xp_amount", nullable = false)
    private Integer xpAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "xp_type", nullable = false, length = 30)
    private XpType xpType;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}