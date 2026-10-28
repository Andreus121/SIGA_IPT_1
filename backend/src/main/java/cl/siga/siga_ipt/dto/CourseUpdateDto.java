package cl.siga.siga_ipt.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class CourseUpdateDto {

    private String name;

    @JsonProperty("semester")
    @Min(value = 1, message = "El semestre debe estar entre 1 y 4")
    @Max(value = 4, message = "El semestre debe estar entre 1 y 4")
    private Integer semester;

    @JsonProperty("theoryHours")
    @Min(value = 0, message = "Las horas de teoría no pueden ser negativas")
    private Integer theoryHours;

    @JsonProperty("exerciseHours")
    @Min(value = 0, message = "Las horas de ejercicios no pueden ser negativas")
    private Integer exerciseHours;

    @JsonProperty("labHours")
    @Min(value = 0, message = "Las horas de laboratorio no pueden ser negativas")
    private Integer labHours;

    @JsonProperty("sct")
    @Min(value = 1, message = "El valor SCT debe ser mayor que cero")
    @Max(value = 7, message = "El valor SCT debe ser menor o igual a 7")
    private Integer sct;

    @JsonProperty("prerequisitesCodes")
    private Set<String> prerequisitesCodes;
}
