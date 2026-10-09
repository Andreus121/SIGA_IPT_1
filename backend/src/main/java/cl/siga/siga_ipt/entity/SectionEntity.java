package cl.siga.siga_ipt.entity;

import cl.siga.siga_ipt.enums.PeriodStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sections")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String courseCode;

    @Column(name = "period_code", nullable = false)
    private String periodCode;   // references AcademicPeriodEntity.periodCode

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PeriodStatus periodStatus;

    @Column(nullable = false)
    private Long teacherId;

    @Column(nullable = false)
    private Integer capacity;
}