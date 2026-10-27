package cl.siga.siga_ipt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectionResponseDto {

    private Long id;
    private String code;
    private String courseCode;
    private String periodCode;
    private Long teacherId;
    private Integer capacity;
    private List<SectionScheduleDto> schedules;
}
