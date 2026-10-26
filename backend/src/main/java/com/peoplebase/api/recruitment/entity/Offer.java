package com.peoplebase.api.recruitment.entity;

import com.peoplebase.api.auth.entity.UserAccount;
import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.employee.entity.Employee;
import com.peoplebase.api.recruitment.enums.OfferStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Đề nghị tuyển và phản hồi của ứng viên, chuyển tiếp nhận một lần. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "offers",
        uniqueConstraints = {@UniqueConstraint(name = "uk_offers_1", columnNames = {"offer_code"}), @UniqueConstraint(name = "uk_offers_2", columnNames = {"converted_employee_id"})},
        indexes = {@Index(name = "idx_offers_application_id", columnList = "application_id"), @Index(name = "idx_offers_issued_by_id", columnList = "issued_by_id"), @Index(name = "idx_offers_converted_employee_id", columnList = "converted_employee_id")})
public class Offer extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "offer_code", nullable = false, length = 50)
    private String offerCode;

    @Column(name = "offered_salary", nullable = true, precision = 18, scale = 2)
    private BigDecimal offeredSalary;

    @Column(name = "currency", nullable = true, length = 3)
    private String currency;

    @Column(name = "proposed_start_date", nullable = true)
    private LocalDate proposedStartDate;

    @Column(name = "expires_on", nullable = true)
    private LocalDate expiresOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private OfferStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "issued_by_id", nullable = true)
    private UserAccount issuedBy;

    @Column(name = "issued_at", nullable = true)
    private LocalDateTime issuedAt;

    @Column(name = "responded_at", nullable = true)
    private LocalDateTime respondedAt;

    @Column(name = "response_note", nullable = true, length = 2000)
    private String responseNote;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "converted_employee_id", nullable = true, unique = true)
    private Employee convertedEmployee;

    @Column(name = "converted_at", nullable = true)
    private LocalDateTime convertedAt;
}
