package cl.siga.siga_ipt.controller;

import cl.siga.siga_ipt.dto.StudyPlanCreateDto;
import cl.siga.siga_ipt.dto.StudyPlanResponseDto;
import cl.siga.siga_ipt.service.StudyPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/study-plans")
@RequiredArgsConstructor
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudyPlanResponseDto> createStudyPlan(@Valid @RequestBody StudyPlanCreateDto dto) {
        return new ResponseEntity<>(studyPlanService.createStudyPlan(dto), HttpStatus.CREATED);
    }

    // obtener todos los planes de estudio
    @GetMapping
    @PreAuthorize ("hasAnyRole('ADMIN')")
    public ResponseEntity<List<StudyPlanResponseDto>> getAllStudyPlans() {
        return new ResponseEntity<>(studyPlanService.getAllStudyPlans(), HttpStatus.OK);
    }


    @GetMapping("/career/{careerCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StudyPlanResponseDto>> getPlansByCareer(@PathVariable String careerCode) {
        return new ResponseEntity<>(studyPlanService.getPlansByCareer(careerCode), HttpStatus.OK);
    }

    @PutMapping("/{planCode}/set-current")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudyPlanResponseDto> setPlanAsCurrent(@PathVariable String planCode) {
        return new ResponseEntity<>(studyPlanService.setPlanAsCurrent(planCode), HttpStatus.OK);
    }

    @DeleteMapping("/{planCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStudyPlan(@PathVariable String planCode) {
        studyPlanService.deleteStudyPlan(planCode);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{code}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<StudyPlanResponseDto> getPlanByCode(@PathVariable String code) {
        return new ResponseEntity<>(studyPlanService.getPlanByCode(code), HttpStatus.OK);
    }
}
