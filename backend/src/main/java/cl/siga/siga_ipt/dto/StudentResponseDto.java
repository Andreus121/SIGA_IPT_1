package cl.siga.siga_ipt.dto;

import cl.siga.siga_ipt.enums.StudentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentResponseDto {
    private Long id;
    private String run;
    private String fullName;
    private String email;
    private String careerCode;
    private String studyPlanCode;
    private StudentStatus status;
}
