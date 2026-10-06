package cl.siga.siga_ipt.dto;

import cl.siga.siga_ipt.enums.StudentStatus;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class StudentUpdateDto {

    private String run;

    private String fullName;

    @Email(message = "Formato de correo no válido")
    private String email;

    private String careerCode;

    private StudentStatus status;
}
