package cl.siga.siga_ipt.entity;

import cl.siga.siga_ipt.enums.TeacherStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teachers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String run;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    private String professionalTitle;
    private String academicDegree;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeacherStatus status;
}