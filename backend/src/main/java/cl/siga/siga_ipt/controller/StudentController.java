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


@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    // Post para registrar un nuevo estudiante en la tabla de estudiantes
    @PostMapping
    public ResponseEntity<StudentResponseDto> createStudent(@Valid @RequestBody StudentCreateDto dto) {
        return new ResponseEntity<>(studentService.registerStudent(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponseDto> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentUpdateDto dto){
        return new ResponseEntity<>(studentService.updateStudent(id, dto), HttpStatus.OK);
    }

    /* Funciones por implementar
    @GetMapping
    public ResponseEntity<List<StudentResponseDto>> getAllStudents() {
    }


    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDto> getStudentById(@PathVariable Long id) {
    }

    @GetMapping("/run/{run}")
    public ResponseEntity<StudentResponseDto> getStudentByRun(@PathVariable String run) {
    }


    */
}