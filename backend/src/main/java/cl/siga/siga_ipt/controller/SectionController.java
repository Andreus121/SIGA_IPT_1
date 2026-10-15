package cl.siga.siga_ipt.controller;

import cl.siga.siga_ipt.dto.SectionCreateDto;
import cl.siga.siga_ipt.dto.SectionResponseDto;
import cl.siga.siga_ipt.dto.SectionUpdateDto;
import cl.siga.siga_ipt.service.SectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    // Crear una sección para una asignatura en un período académico abierto
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SectionResponseDto> createSection(@Valid @RequestBody SectionCreateDto dto) {
        return new ResponseEntity<>(sectionService.createSection(dto), HttpStatus.CREATED);
    }

    // Actualizar docente, capacidad o bloques horarios de una sección
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SectionResponseDto> updateSection(@PathVariable Long id, @Valid @RequestBody SectionUpdateDto dto) {
        return new ResponseEntity<>(sectionService.updateSection(id, dto), HttpStatus.OK);
    }

    // Obtener sección por ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<SectionResponseDto> getSectionById(@PathVariable Long id) {
        return new ResponseEntity<>(sectionService.getSectionById(id), HttpStatus.OK);
    }

    // Obtener sección por código de sección
    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<SectionResponseDto> getSectionByCode(@PathVariable String code) {
        return new ResponseEntity<>(sectionService.getSectionByCode(code), HttpStatus.OK);
    }

    // Obtener todas las secciones de un período académico
    @GetMapping("/period/{periodCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<SectionResponseDto>> getSectionsByPeriod(@PathVariable String periodCode) {
        return new ResponseEntity<>(sectionService.getSectionsByPeriod(periodCode), HttpStatus.OK);
    }

    // Eliminar sección
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSection(@PathVariable Long id) {
        sectionService.deleteSection(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
