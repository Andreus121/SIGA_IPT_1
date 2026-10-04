package cl.siga.siga_ipt.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class CourseResponseDto {
    private Long id;
    private String code;
    private String name;
    private Integer semester;
    private Integer theoryHours;
    private Integer exerciseHours;
    private Integer labHours;
    private Integer sct;
    private String studyPlanCode;
    private Set<String> prerequisites;
}
