package cl.siga.siga_ipt.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    // Contraseña inicial asignada por el admin para que el docente pueda iniciar sesión en Keycloak
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

}
