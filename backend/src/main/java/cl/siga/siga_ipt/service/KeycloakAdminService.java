package cl.siga.siga_ipt.service;

/**
 * Servicio para administrar usuarios en Keycloak mediante la Admin REST API.
 * Se usa cuando el admin crea un estudiante o docente para darles acceso al sistema.
 */

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class KeycloakAdminService {

    // URL interna de Keycloak (dentro de Docker: http://keycloak:8080)
    @Value("${keycloak.internal.url}")
    private String keycloakInternalUrl;

    // Realm donde se crean los usuarios
    @Value("${keycloak.admin.realm:siga-realm}")
    private String realm;

    // Credenciales del admin de Keycloak (realm master)
    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    private final RestClient restClient = RestClient.create();

    /**
     * Crea un usuario en Keycloak con el email como username, le asigna la contraseña
     * dada por el admin y le asigna el rol de realm (STUDENT o TEACHER).
     *
     * @param email    Correo del nuevo usuario (también es el username en Keycloak)
     * @param fullName Nombre completo del usuario
     * @param password Contraseña inicial asignada por el admin
     * @param roleName Rol de realm a asignar: "STUDENT" o "TEACHER"
     */
    public void createUser(String email, String fullName, String password, String roleName) {
        try {
            // 1. Obtener token de admin del realm master
            String adminToken = obtenerTokenAdmin();

            // 2. Crear el usuario en Keycloak
            String userId = crearUsuarioEnKeycloak(adminToken, email, fullName, password);

            // 3. Asignar el rol de realm correspondiente
            asignarRol(adminToken, userId, roleName);

            log.info("Usuario Keycloak creado correctamente: {} con rol {}", email, roleName);

        } catch (Exception e) {
            log.error("Error al crear usuario en Keycloak para {}: {}", email, e.getMessage());
            // Se lanza la excepción para que el servicio que llama pueda revertir la transacción DB
            throw new RuntimeException("No se pudo crear el usuario en Keycloak: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene un token de acceso de admin desde el realm master usando las credenciales configuradas.
     */
    private String obtenerTokenAdmin() {
        String tokenUrl = keycloakInternalUrl + "/realms/master/protocol/openid-connect/token";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", "admin-cli");
        body.add("username", adminUsername);
        body.add("password", adminPassword);

        Map<?, ?> response = restClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(Map.class);

        if (response == null || !response.containsKey("access_token")) {
            throw new RuntimeException("No se pudo obtener el token de admin de Keycloak");
        }

        return (String) response.get("access_token");
    }

    /**
     * Crea el usuario en Keycloak y retorna el ID del usuario creado.
     */
    private String crearUsuarioEnKeycloak(String adminToken, String email, String fullName, String password) {
        String usersUrl = keycloakInternalUrl + "/admin/realms/" + realm + "/users";

        // Separar nombre y apellido del nombre completo (Keycloak usa firstName/lastName)
        String[] partes = fullName.trim().split(" ", 2);
        String firstName = partes[0];
        String lastName = partes.length > 1 ? partes[1] : "";

        // Cuerpo de la petición para crear usuario en Keycloak
        Map<String, Object> userBody = Map.of(
                "username", email,
                "email", email,
                "firstName", firstName,
                "lastName", lastName,
                "enabled", true,
                "credentials", List.of(Map.of(
                        "type", "password",
                        "value", password,
                        "temporary", false
                ))
        );

        // Keycloak retorna 201 Created con Location header que contiene el ID del usuario
        var response = restClient.post()
                .uri(usersUrl)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(userBody)
                .retrieve()
                .toBodilessEntity();

        // Extraer el ID del usuario desde el header Location: .../users/{id}
        var location = response.getHeaders().getLocation();
        if (location == null) {
            throw new RuntimeException("Keycloak no retornó la ubicación del usuario creado");
        }

        String path = location.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    /**
     * Asigna un rol de realm al usuario recién creado.
     */
    private void asignarRol(String adminToken, String userId, String roleName) {
        // Primero obtener la representación del rol para conseguir su ID
        String roleUrl = keycloakInternalUrl + "/admin/realms/" + realm + "/roles/" + roleName;

        Map<?, ?> role = restClient.get()
                .uri(roleUrl)
                .header("Authorization", "Bearer " + adminToken)
                .retrieve()
                .body(Map.class);

        if (role == null) {
            throw new RuntimeException("No se encontró el rol '" + roleName + "' en el realm " + realm);
        }

        // Asignar el rol al usuario
        String roleMappingUrl = keycloakInternalUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";

        restClient.post()
                .uri(roleMappingUrl)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(List.of(role))
                .retrieve()
                .toBodilessEntity();
    }
}
