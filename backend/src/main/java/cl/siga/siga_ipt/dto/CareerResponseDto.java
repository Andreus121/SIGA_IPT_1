package cl.siga.siga_ipt.dto;

import cl.siga.siga_ipt.enums.CareerStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CareerResponseDto {
    private Long id;
    private String code;
    private String name;
    private String description;
    private CareerStatus status;
}
