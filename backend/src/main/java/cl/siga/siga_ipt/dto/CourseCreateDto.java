package cl.siga.siga_ipt.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class CourseCreateDto {

    @NotBlank(message = "El código de la asignatura es obligatorio")
    private String code;

    @JsonProperty("studyPlanCode")
    @NotNull(message = "El código del plan de estudios es obligatorio")
    private String studyPlanCode;

    @NotBlank(message = "El nombre de la asignatura es obligatorio")
    private String name;

    @NotNull(message = "El semestre es obligatorio")
    @Min(value = 1, message = "El semestre debe ser como mínimo 1")
    @Max(value = 4, message = "El semestre debe ser como máximo 4")
    private Integer semester;

    @JsonProperty("theoryHours")
    @NotNull(message = "Las horas de teoría son obligatorias")
    @Min(value = 0, message = "Las horas de teoría no pueden ser negativas")
    private Integer theoryHours;

    @JsonProperty("exerciseHours")
    @NotNull(message = "Las horas de ejercicios son obligatorias")
    @Min(value = 0, message = "Las horas de ejercicios no pueden ser negativas")
    private Integer exerciseHours;

    @JsonProperty("labHours")
    @NotNull(message = "Las horas de laboratorio son obligatorias")
    @Min(value = 0, message = "Las horas de laboratorio no pueden ser negativas")
    private Integer labHours;

    @NotNull(message = "El SCT es obligatorio")
    @Min(value = 1, message = "El SCT debe ser mayor que 0")
    @Max(value = 7, message = "El SCT debe ser menor o igual a 7")
    private Integer sct;

    // Se usa Set<> para que no tenga cosas duplicadas
    @JsonProperty ("prerequisitesCodes")
    private Set<String> prerequisitesCodes = new HashSet<>();
}
