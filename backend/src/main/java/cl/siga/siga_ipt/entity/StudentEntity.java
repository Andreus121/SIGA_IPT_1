package cl.siga.siga_ipt.entity;

import cl.siga.siga_ipt.enums.StudentStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false, unique = true)
    private String run;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String careerCode;

    @Column(nullable = false)
    private String studyPlanCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudentStatus status;
}