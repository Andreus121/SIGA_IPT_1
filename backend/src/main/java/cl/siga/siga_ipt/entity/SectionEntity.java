package cl.siga.siga_ipt.entity;

// PeriodStatus import removed (field no longer used)
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sections", uniqueConstraints = {@UniqueConstraint(name = "uk_section_course_period", columnNames = {"course_code", "period_code"})})
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

    @Column(nullable = false)
    private Long teacherId;

    @Column(nullable = false)
    private Integer capacity;
}