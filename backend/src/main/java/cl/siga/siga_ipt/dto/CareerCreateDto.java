package cl.siga.siga_ipt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CareerCreateDto {

    @NotBlank(message = "El código de la carrera es obligatorio")
    private String code;

    @NotBlank(message = "El nombre de la carrera es obligatorio")
    private String name;

    @NotBlank(message = "La descripción de la carrera es obligatoria")
    private String description;
}