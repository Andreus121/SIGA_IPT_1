package cl.siga.siga_ipt.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeEntryDto {

    @NotNull(message = "El ID de la inscripción es obligatorio.")
    private Long registrationId;

    @NotNull(message = "La calificación final es obligatoria.")
    @DecimalMin(value = "1.0", message = "La calificación mínima es 1.0.")
    @DecimalMax(value = "7.0", message = "La calificación máxima es 7.0.")
    private Double finalGrade;
}
