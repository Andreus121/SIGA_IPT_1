package cl.siga.siga_ipt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchRegistrationCreateDto {

    @NotBlank(message = "El RUN del estudiante es obligatorio")
    private String studentRun;

    @NotBlank(message = "El código del período académico es obligatorio")
    private String periodCode;

    @NotEmpty(message = "Debe seleccionar al menos una sección")
    @Size(max = 6, message = "No puede seleccionar más de 6 asignaturas")
    private List<@NotBlank(message = "El código de sección es obligatorio") String> sectionCodes;
}
