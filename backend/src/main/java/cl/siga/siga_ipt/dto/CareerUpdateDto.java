package cl.siga.siga_ipt.dto;

import cl.siga.siga_ipt.enums.CareerStatus;
import lombok.Data;

@Data
public class CareerUpdateDto {
    private String name;
    private String description;
    private CareerStatus status;
}
