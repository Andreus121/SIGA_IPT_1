package cl.siga.siga_ipt.entity;

import cl.siga.siga_ipt.enums.AcademicStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "academic_histories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String studentRun;

    @Column(nullable = false)
    private String sectionCode;

    @Column(nullable = false)
    private String teacherCode;

    @Column(nullable = false)
    private String periodCode;

    private Double finalGrade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AcademicStatus status;
}
