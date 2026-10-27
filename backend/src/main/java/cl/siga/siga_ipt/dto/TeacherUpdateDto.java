package cl.siga.siga_ipt.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class TeacherUpdateDto {

    private String run;

    private String fullName;

    @Email(message = "El formato del correo electrónico es inválido")
    private String email;

    private String professionalTitle;

    private String academicDegree;

}
