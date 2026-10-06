package cl.siga.siga_ipt.controller;

import cl.siga.siga_ipt.dto.CourseCreateDto;
import cl.siga.siga_ipt.dto.CourseResponseDto;
import cl.siga.siga_ipt.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<CourseResponseDto> createSubject(@Valid @RequestBody CourseCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.createCourse(dto));
    }
}