package cl.siga.siga_ipt.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StudentCreateDto {

    @NotBlank(message = "El RUN es obligatorio")
    private String run;

    @NotBlank(message = "El nombre completo es obligatorio")
    private String fullName;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "Formato de correo no válido")
    private String email;

    @NotBlank(message = "El código de carrera es obligatorio")
    private String careerCode;

    private String password; // Contraseña inicial para su cuenta de usuario
}