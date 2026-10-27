package cl.siga.siga_ipt.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectionUpdateDto {

    private Long teacherId;

    @Min(value = 1, message = "La capacidad debe ser mayor que 0")
    @Max(value = 60, message = "La capacidad máxima es de 60 estudiantes")
    private Integer capacity;

    @Valid
    private List<SectionScheduleDto> schedules;
}
