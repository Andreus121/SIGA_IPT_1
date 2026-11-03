package cl.siga.siga_ipt.controller;

import cl.siga.siga_ipt.dto.BatchRegistrationCreateDto;
import cl.siga.siga_ipt.dto.RegistrationCreateDto;
import cl.siga.siga_ipt.dto.RegistrationResponseDto;
import cl.siga.siga_ipt.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    // Solo el admin y el estudiante pueden hacer tomas de ramos
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<RegistrationResponseDto> registerCourse(@Valid @RequestBody RegistrationCreateDto dto) {
        return new ResponseEntity<>(registrationService.registerCourse(dto), HttpStatus.CREATED);
    }

    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<List<RegistrationResponseDto>> registerCourses(
            @Valid @RequestBody BatchRegistrationCreateDto dto) {
        return new ResponseEntity<>(registrationService.registerCourses(dto), HttpStatus.CREATED);
    }

    // Solo el admin y el estudiante pueden desinscribir ramos
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<Void> dropCourse(@PathVariable Long id) {
        registrationService.dropCourse(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Cualquiera puede ver que ramos tomó un estudiante
    @GetMapping("/student/{run}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'TEACHER')")
    public ResponseEntity<List<RegistrationResponseDto>> getRegistrationsByStudent(
            @PathVariable String run,
            @RequestParam String periodCode) {
        return new ResponseEntity<>(registrationService.getRegistrationsByStudent(run, periodCode), HttpStatus.OK);
    }

    // Solo el admin y los profesores pueden ver quienes están inscritos en una sección
    @GetMapping("/section/{sectionCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<RegistrationResponseDto>> getRegistrationsBySection(
            @PathVariable String sectionCode,
            @RequestParam String periodCode) {
        return new ResponseEntity<>(registrationService.getRegistrationsBySection(sectionCode, periodCode), HttpStatus.OK);
    }
}
