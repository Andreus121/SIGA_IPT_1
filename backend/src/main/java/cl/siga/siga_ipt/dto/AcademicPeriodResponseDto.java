package cl.siga.siga_ipt.dto;

import cl.siga.siga_ipt.enums.PeriodStatus;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for returning AcademicPeriod information.
 */
@Data
@Builder
public class AcademicPeriodResponseDto {
    private String periodCode;
    private PeriodStatus status;
}
