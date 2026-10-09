package cl.siga.siga_ipt.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "courses", uniqueConstraints = {
        // Hacer que el código de la asignatura sea único por código de plan de estudio
        @UniqueConstraint(name = "uk_course_code_plan", columnNames = {"code", "study_plan_code"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String studyPlanCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer semester;

    @Column(nullable = false)
    private Integer theoryHours;

    @Column(nullable = false)
    private Integer exerciseHours;

    @Column(nullable = false)
    private Integer labHours;

    @Column(nullable = false)
    private Integer sct;
}