package cl.siga.siga_ipt.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectionScheduleDto {

    @NotBlank(message = "El día del módulo es obligatorio")
    @Pattern(regexp = "^[LMWJV]$", message = "El día debe ser uno de los siguientes: L, M, W, J, V")
    private String day;

    @NotNull(message = "El número de módulo es obligatorio")
    @Min(value = 1, message = "El módulo debe ser como mínimo 1 (M1)")
    @Max(value = 6, message = "El módulo debe ser como máximo 6 (M6)")
    private Integer moduleNumber;
}
