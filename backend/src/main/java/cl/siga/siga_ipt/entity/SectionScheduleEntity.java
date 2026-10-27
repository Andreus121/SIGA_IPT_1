package cl.siga.siga_ipt.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "section_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectionScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sectionCode;

    @Column(nullable = false)
    private String periodCode;

    @Column(name = "schedule_day", nullable = false)
    private String day; // L, M, W, J, V

    @Column(nullable = false)
    private Integer moduleNumber; // 1 to 6
}