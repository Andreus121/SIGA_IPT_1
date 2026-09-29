package cl.siga.siga_ipt.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "prerequisites")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrerequisiteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String studyPlanCode;

    @Column(nullable = false)
    private String courseCode;

    @Column(nullable = false)
    private String prerequisiteCourseCode;
}
