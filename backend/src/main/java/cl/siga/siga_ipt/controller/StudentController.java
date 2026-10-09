package cl.siga.siga_ipt.controller;

import cl.siga.siga_ipt.dto.StudentCreateDto;
import cl.siga.siga_ipt.dto.StudentResponseDto;
import cl.siga.siga_ipt.dto.StudentUpdateDto;
import cl.siga.siga_ipt.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;


@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    // Post para registrar un nuevo estudiante en la tabla de estudiantes
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentResponseDto> createStudent(@Valid @RequestBody StudentCreateDto dto) {
        return new ResponseEntity<>(studentService.createStudent(dto), HttpStatus.CREATED);
    }

    // Put para actualizar un estudiante ya existente
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentResponseDto> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentUpdateDto dto){
        return new ResponseEntity<>(studentService.updateStudent(id, dto), HttpStatus.OK);
    }

    // Get para traer datos de un estudiante por su id
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<StudentResponseDto> getStudentById(@PathVariable Long id) {
        return new ResponseEntity<>(studentService.getStudentById(id), HttpStatus.OK);
    }

    // Get para traer datos de un estudiante por su run
    @GetMapping("/run/{run}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<StudentResponseDto> getStudentByRun(@PathVariable String run) {
        return new ResponseEntity<>(studentService.getStudentByRun(run), HttpStatus.OK);
    }

    // Get para traer datos de un estudiante por su email
    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<StudentResponseDto> getStudentByEmail(@PathVariable String email) {
        return new ResponseEntity<>(studentService.getStudentByEmail(email), HttpStatus.OK);
    }

    // Get para TODOS los estudiantes (consulta pesada)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StudentResponseDto>> getAllStudents() {
        return new ResponseEntity<>(studentService.getAllStudents(), HttpStatus.OK);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
}