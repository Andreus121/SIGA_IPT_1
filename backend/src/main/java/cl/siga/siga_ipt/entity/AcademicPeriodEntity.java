package cl.siga.siga_ipt.entity;


import cl.siga.siga_ipt.enums.PeriodStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "academic_periods")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicPeriodEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String academicPeriod; // example: 2027-2 or 2025-1

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PeriodStatus status;
}
