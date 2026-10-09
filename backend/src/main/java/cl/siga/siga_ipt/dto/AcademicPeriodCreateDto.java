package cl.siga.siga_ipt.dto;

import cl.siga.siga_ipt.enums.PeriodStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new AcademicPeriod.
 * Uses the natural business key {@code periodCode} and the period {@link PeriodStatus}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicPeriodCreateDto {

    @NotBlank(message = "El código del período académico es obligatorio")
    private String periodCode;

    @NotNull(message = "El estado del período es obligatorio")
    private PeriodStatus status;
}
