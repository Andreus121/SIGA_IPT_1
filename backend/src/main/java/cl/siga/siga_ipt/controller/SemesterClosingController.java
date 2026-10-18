package cl.siga.siga_ipt.controller;

import cl.siga.siga_ipt.dto.SemesterClosingResponseDto;
import cl.siga.siga_ipt.service.SemesterClosingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/semester-closing")
@RequiredArgsConstructor
public class SemesterClosingController {

    private final SemesterClosingService semesterClosingService;

    // Solo el admin puede cerrar un semestre
    @PostMapping("/{periodCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SemesterClosingResponseDto> closeSemester(@PathVariable String periodCode) {
        return ResponseEntity.ok(semesterClosingService.closeSemester(periodCode));
    }
}
