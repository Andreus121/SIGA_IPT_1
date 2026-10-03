package cl.siga.siga_ipt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StudyPlanCreateDto {

    @NotBlank(message = "El código del plan de estudios es obligatorio")
    private String code;

    @NotBlank(message = "El código de la carrera es obligatorio")
    private String careerCode;

    @NotBlank(message = "El nombre/año del plan es obligatorio")
    private String name;

    private Boolean setCurrent; // Si es true, pasa a ser el plan VIGENTE
}
