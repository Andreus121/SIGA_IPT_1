package cl.siga.siga_ipt.controller;

import cl.siga.siga_ipt.dto.AcademicPeriodCreateDto;
import cl.siga.siga_ipt.dto.AcademicPeriodResponseDto;
import cl.siga.siga_ipt.service.AcademicPeriodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/periods")
@RequiredArgsConstructor
public class AcademicPeriodController {

    private final AcademicPeriodService periodService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AcademicPeriodResponseDto> createPeriod(@Valid @RequestBody AcademicPeriodCreateDto dto) {
        AcademicPeriodResponseDto created = periodService.createPeriod(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{code}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<AcademicPeriodResponseDto> getPeriod(@PathVariable String code) {
        AcademicPeriodResponseDto dto = periodService.getByCode(code);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<List<AcademicPeriodResponseDto>> listPeriods() {
        List<AcademicPeriodResponseDto> dtos = periodService.getAllPeriods();
        return ResponseEntity.ok(dtos);
    }
}
