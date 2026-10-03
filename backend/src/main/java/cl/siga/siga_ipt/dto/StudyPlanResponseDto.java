package cl.siga.siga_ipt.dto;

import cl.siga.siga_ipt.enums.StudyPlanStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudyPlanResponseDto {
    private Long id;
    private String code;
    private String careerCode;
    private String name;
    private StudyPlanStatus status;
}