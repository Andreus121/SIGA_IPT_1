package cl.siga.siga_ipt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentCreateDto {

    @NotBlank(message = "El RUN del estudiante es obligatorio")
    private String studentRun;

    @NotBlank(message = "El código del período académico es obligatorio")
    private String periodCode;
}
