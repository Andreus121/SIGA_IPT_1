package cl.siga.siga_ipt.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectionCreateDto {

    @NotBlank(message = "El código de la sección es obligatorio")
    private String code;

    @NotBlank(message = "El código de la asignatura es obligatorio")
    private String courseCode;

    @NotBlank(message = "El código del período académico es obligatorio")
    private String periodCode;

    @NotNull(message = "El ID del docente es obligatorio")
    private Long teacherId;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser mayor que 0")
    @Max(value = 60, message = "La capacidad máxima es de 60 estudiantes")
    private Integer capacity;

    @NotNull(message = "Los bloques horarios son obligatorios")
    @NotEmpty(message = "Debe asignar al menos un bloque de horario")
    @Valid
    @Builder.Default
    private List<SectionScheduleDto> schedules = new ArrayList<>();
}
