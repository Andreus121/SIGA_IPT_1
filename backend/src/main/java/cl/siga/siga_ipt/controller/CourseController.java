package cl.siga.siga_ipt.controller;

import cl.siga.siga_ipt.dto.CourseCreateDto;
import cl.siga.siga_ipt.dto.CourseResponseDto;
import cl.siga.siga_ipt.dto.CourseUpdateDto;
import cl.siga.siga_ipt.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponseDto> createCourse(@Valid @RequestBody CourseCreateDto dto) {
        return new ResponseEntity<>(courseService.createCourse(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{studyPlanCode}/{code}")
    public ResponseEntity<CourseResponseDto> updateCourse(
            @PathVariable String studyPlanCode,
            @PathVariable String code,
            @Valid @RequestBody CourseUpdateDto dto) {
        return new ResponseEntity<>(courseService.updateCourse(studyPlanCode, code, dto), HttpStatus.OK);
    }

    @GetMapping("/{studyPlanCode}/{code}")
    public ResponseEntity<CourseResponseDto> getCourse(
            @PathVariable String studyPlanCode,
            @PathVariable String code) {
        return new ResponseEntity<>(courseService.getCourse(studyPlanCode, code), HttpStatus.OK);
    }

    @GetMapping("/plan/{studyPlanCode}")
    public ResponseEntity<List<CourseResponseDto>> getCoursesByPlan(@PathVariable String studyPlanCode) {
        return new ResponseEntity<>(courseService.getCoursesByPlan(studyPlanCode), HttpStatus.OK);
    }

    @DeleteMapping("/{studyPlanCode}/{code}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable String studyPlanCode,
            @PathVariable String code) {
        courseService.deleteCourse(studyPlanCode, code);
        return ResponseEntity.noContent().build();
    }
}