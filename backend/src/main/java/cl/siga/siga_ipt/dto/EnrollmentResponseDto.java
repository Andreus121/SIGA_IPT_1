package cl.siga.siga_ipt.dto;

import cl.siga.siga_ipt.enums.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentResponseDto {
    private Long id;
    private String studentRun;
    private String periodCode;
    private LocalDate enrollmentDate;
    private EnrollmentStatus status;
}
