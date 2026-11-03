package cl.siga.siga_ipt.service;

/**
 * Servicio para administrar usuarios en Keycloak mediante la Admin REST API.
 * Se usa cuando el admin crea un estudiante o docente para darles acceso al sistema.
 */

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

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

    @Value("${keycloak.admin.max-attempts:12}")
    private int maxAttempts;

    @Value("${keycloak.admin.retry-delay-ms:5000}")
    private long retryDelayMs;

    private final RestClient restClient = RestClient.create();

    // Crea un usuario en Keycloak con el email como username, le asigna la contraseña
    // dada por el admin y le asigna el rol de realm (STUDENT o TEACHER).
    public void createUser(String email, String fullName, String password, String roleName) {
        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                String adminToken = obtenerTokenAdmin();
                String userId = buscarUsuario(adminToken, email);
                if (userId == null) {
                    userId = crearUsuarioEnKeycloak(adminToken, email, fullName, password);
                }
                asignarRol(adminToken, userId, roleName);
                log.info("Usuario Keycloak listo: {} con rol {}", email, roleName);
                return;
            } catch (RestClientException | IllegalStateException e) {
                lastFailure = new RuntimeException(e.getMessage(), e);
                log.warn("Keycloak no está listo para {} (intento {}/{}): {}",
                        email, attempt, maxAttempts, e.getMessage());
                if (attempt < maxAttempts) {
                    esperarReintento();
                }
            } catch (RuntimeException e) {
                log.error("Error no recuperable al crear usuario Keycloak para {}: {}", email, e.getMessage());
                throw new RuntimeException("No se pudo crear el usuario en Keycloak: " + e.getMessage(), e);
            }
        }
        throw new RuntimeException("Keycloak no estuvo disponible después de " + maxAttempts
                + " intentos para crear/verificar " + email, lastFailure);
    }

    private void esperarReintento() {
        try {
            Thread.sleep(retryDelayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("El reintento contra Keycloak fue interrumpido", e);
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

    private String buscarUsuario(String adminToken, String email) {
        String usersUrl = UriComponentsBuilder
                .fromUriString(keycloakInternalUrl + "/admin/realms/" + realm + "/users")
                .queryParam("email", email)
                .queryParam("exact", true)
                .toUriString();

        List<?> users = restClient.get()
                .uri(usersUrl)
                .header("Authorization", "Bearer " + adminToken)
                .retrieve()
                .body(List.class);

        if (users == null || users.isEmpty()) {
            return null;
        }
        Object id = ((Map<?, ?>) users.get(0)).get("id");
        if (!(id instanceof String userId) || userId.isBlank()) {
            throw new IllegalStateException("Keycloak retornó un usuario sin ID para " + email);
        }
        return userId;
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
