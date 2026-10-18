package cl.siga.siga_ipt.controller;

import cl.siga.siga_ipt.dto.EnrollmentCreateDto;
import cl.siga.siga_ipt.dto.EnrollmentResponseDto;
import cl.siga.siga_ipt.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    // Solo el admin y el mismo estudiante puede hacer matrículas
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<EnrollmentResponseDto> enrollStudent(@Valid @RequestBody EnrollmentCreateDto dto) {
        return new ResponseEntity<>(enrollmentService.enrollStudent(dto), HttpStatus.CREATED);
    }

    // Solo el admin puede tener todas las matrículas de un periodo
    @GetMapping("/period/{periodCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentResponseDto>> getEnrollmentsByPeriod(@PathVariable String periodCode) {
        return new ResponseEntity<>(enrollmentService.getEnrollmentsByPeriod(periodCode), HttpStatus.OK);
    }

    // Solo el admin o el mismo estudiante puede obtener una matrícula por run
    @GetMapping("/student/{run}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<List<EnrollmentResponseDto>> getEnrollmentsByStudent(@PathVariable String run) {
        return new ResponseEntity<>(enrollmentService.getEnrollmentsByStudent(run), HttpStatus.OK);
    }
}
