package cl.siga.siga_ipt.entity;

import cl.siga.siga_ipt.enums.RegistrationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "registrations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String studentRun;

    @Column(nullable = false)
    private String courseCode;

    @Column(nullable = false)
    private String sectionCode;

    @Column(nullable = false)
    private String periodCode;

    private LocalDateTime registrationDate;

    private Double finalGrade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status;
}