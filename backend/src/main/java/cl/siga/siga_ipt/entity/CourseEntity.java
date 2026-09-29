package cl.siga.siga_ipt.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String studyPlanCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer semester;

    private Integer theoryHours;
    private Integer exerciseHours;
    private Integer labHours;

    @Column(nullable = false)
    private Integer sct;
}