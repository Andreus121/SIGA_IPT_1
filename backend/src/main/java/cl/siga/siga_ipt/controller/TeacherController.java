package cl.siga.siga_ipt.controller;

import cl.siga.siga_ipt.dto.TeacherCreateDto;
import cl.siga.siga_ipt.dto.TeacherResponseDto;
import cl.siga.siga_ipt.dto.TeacherUpdateDto;
import cl.siga.siga_ipt.enums.TeacherStatus;
import cl.siga.siga_ipt.service.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de docentes.
 * Las operaciones están protegidas mediante anotaciones @PreAuthorize que extraen los roles del JWT de Keycloak.
 */
@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherResponseDto> createTeacher(@Valid @RequestBody TeacherCreateDto dto) {
        return new ResponseEntity<>(teacherService.createTeacher(dto), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TeacherResponseDto>> getAllTeachers(@RequestParam(required = false) TeacherStatus status) {
        return new ResponseEntity<>(teacherService.getAllTeachers(status), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<TeacherResponseDto> getTeacherById(@PathVariable Long id) {
        return new ResponseEntity<>(teacherService.getTeacherById(id), HttpStatus.OK);
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<TeacherResponseDto> getTeacherByEmail(@PathVariable String email) {
        return new ResponseEntity<>(teacherService.getTeacherByEmail(email), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherResponseDto> updateTeacher(@PathVariable Long id, @Valid @RequestBody TeacherUpdateDto dto) {
        return new ResponseEntity<>(teacherService.updateTeacher(id, dto), HttpStatus.OK);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherResponseDto> changeStatus(@PathVariable Long id, @RequestParam TeacherStatus status) {
        return new ResponseEntity<>(teacherService.changeTeacherStatus(id, status), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return ResponseEntity.noContent().build();
    }
}
