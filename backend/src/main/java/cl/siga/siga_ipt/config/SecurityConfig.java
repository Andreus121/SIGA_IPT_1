package cl.siga.siga_ipt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
// Configuración de seguridad que delega la autenticación a Keycloak mediante JWT
/**
 * Configuración de Spring Security.
 *
 * • Se desactiva CSRF porque la API se consume mediante JSON.
 * • Se protege cada endpoint mediante los roles que provienen del JWT de Keycloak.
 * • La aplicación actúa como Resource Server OAuth2 y valida los tokens JWT emitidos por Keycloak.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Endpoints administrativos solo para ADMIN
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Operaciones de docentes accesibles para ADMIN y TEACHER
                .requestMatchers("/api/teachers/**").hasAnyRole("ADMIN", "TEACHER")
                // Operaciones de estudiantes accesibles para todos los roles
                .requestMatchers("/api/students/**").hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                // Cualquier otro endpoint requiere autenticación
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }
}
