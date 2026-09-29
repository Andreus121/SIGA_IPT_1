package cl.siga.siga_ipt.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sections")
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

    @Column(nullable = false)
    private String academicPeriodCode;

    @Column(nullable = false)
    private String teacherCode;

    @Column(nullable = false)
    private Integer capacity;
}