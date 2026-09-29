package cl.siga.siga_ipt.entity;

import cl.siga.siga_ipt.enums.StudyPlanStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "study_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyPlanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String careerCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudyPlanStatus status;
}