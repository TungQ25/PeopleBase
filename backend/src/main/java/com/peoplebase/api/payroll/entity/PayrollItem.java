package com.peoplebase.api.payroll.entity;

import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.common.snapshot.Snapshots.CalculationDetail;
import com.peoplebase.api.payroll.enums.PayComponentType;
import com.peoplebase.api.payroll.enums.PayrollItemOrigin;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Từng dòng lương với công thức và nguồn giải trình. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payroll_items",
        uniqueConstraints = {@UniqueConstraint(name = "uk_payroll_items_1", columnNames = {"payslip_id", "line_no"}),
                @UniqueConstraint(name = "uk_payroll_items_adjustment_code", columnNames = "adjustment_code")},
        indexes = {@Index(name = "idx_payroll_items_payslip_id", columnList = "payslip_id"), @Index(name = "idx_payroll_items_component_id", columnList = "component_id")})
public class PayrollItem extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payslip_id", nullable = false)
    private Payslip payslip;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "component_id", nullable = false)
    private PayComponent component;

    @Column(name = "line_no", nullable = false)
    private Integer lineNo;

    @Column(name = "component_code_snapshot", nullable = false, length = 255)
    private String componentCodeSnapshot;

    @Column(name = "component_name_snapshot", nullable = false, length = 150)
    private String componentNameSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type_snapshot", nullable = false, length = 40)
    private PayComponentType componentTypeSnapshot;

    @Column(name = "quantity", nullable = true, precision = 12, scale = 4)
    private BigDecimal quantity;

    @Column(name = "unit_rate", nullable = true, precision = 18, scale = 2)
    private BigDecimal unitRate;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "calculation_detail", nullable = false, columnDefinition = "jsonb")
    private CalculationDetail calculationDetail;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "original_payslip_id")
    private Payslip originalPayslip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_item_id")
    private PayrollItem originalItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "origin", nullable = false, length = 40)
    private PayrollItemOrigin origin = PayrollItemOrigin.CALCULATED;

    @Column(name = "adjustment_code", length = 100)
    private String adjustmentCode;

    @Column(name = "reason", length = 2000)
    private String reason;
}
