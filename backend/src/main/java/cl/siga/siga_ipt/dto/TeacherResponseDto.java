package cl.siga.siga_ipt.dto;

import cl.siga.siga_ipt.enums.TeacherStatus;
import lombok.*;

@Builder
@Data
public class TeacherResponseDto {

    private Long id;
    private String run;
    private String fullName;
    private String email;
    private String professionalTitle;
    private String academicDegree;
    private TeacherStatus status;
}