package cl.siga.siga_ipt.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
public class TeacherCreateDto {

    @NotBlank(message = "El RUN es obligatorio")
    private String run;

    @NotBlank(message = "El nombre completo es obligatorio")
    private String fullName;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico es inválido")
    private String email;

    @NotBlank(message = "El título profesional es obligatorio")
    private String professionalTitle;

    @NotBlank(message = "El grado académico es obligatorio")
    private String academicDegree;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
