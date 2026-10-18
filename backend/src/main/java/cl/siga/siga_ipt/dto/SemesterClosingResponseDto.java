package cl.siga.siga_ipt.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SemesterClosingResponseDto {
    private String periodCode;
    private int totalRegistrationsProcessed;
    private int studentsGraduatedCount;
    private int studentsDroppedCount;
    private String message;
}
