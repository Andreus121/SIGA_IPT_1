package cl.siga.siga_ipt.controller;

import cl.siga.siga_ipt.dto.GradeEntryDto;
import cl.siga.siga_ipt.dto.RegistrationResponseDto;
import cl.siga.siga_ipt.service.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    // Solo el admin y profesor pueden registrar notas
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<RegistrationResponseDto> registerGrade(@Valid @RequestBody GradeEntryDto dto) {
        return ResponseEntity.ok(gradeService.registerGrade(dto));
    }

    // Solo el admin y profesor pueden obtener todas las notas de una sección
    @GetMapping("/section/{sectionCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<RegistrationResponseDto>> getGradesBySection(
            @PathVariable String sectionCode,
            @RequestParam String periodCode) {
        return ResponseEntity.ok(gradeService.getGradesBySection(sectionCode, periodCode));
    }
}
