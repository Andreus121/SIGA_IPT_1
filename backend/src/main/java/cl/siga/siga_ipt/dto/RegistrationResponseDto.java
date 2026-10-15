package cl.siga.siga_ipt.dto;

import cl.siga.siga_ipt.enums.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationResponseDto {
    private Long id;
    private String studentRun;
    private String courseCode;
    private String sectionCode;
    private String periodCode;
    private LocalDateTime registrationDate;
    private RegistrationStatus status;
}
