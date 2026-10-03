package cl.siga.siga_ipt.controller;

import cl.siga.siga_ipt.dto.StudyPlanCreateDto;
import cl.siga.siga_ipt.dto.StudyPlanResponseDto;
import cl.siga.siga_ipt.service.StudyPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/study-plans")
@RequiredArgsConstructor
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    @PostMapping
    public ResponseEntity<StudyPlanResponseDto> createStudyPlan(@Valid @RequestBody StudyPlanCreateDto dto) {
        return new ResponseEntity<>(studyPlanService.createStudyPlan(dto), HttpStatus.CREATED);
    }

    @GetMapping("/career/{careerCode}")
    public ResponseEntity<List<StudyPlanResponseDto>> getPlansByCareer(@PathVariable String careerCode) {
        return ResponseEntity.ok(studyPlanService.getPlansByCareer(careerCode));
    }

    @PutMapping("/{planCode}/set-current")
    public ResponseEntity<StudyPlanResponseDto> setPlanAsCurrent(@PathVariable String planCode) {
        return ResponseEntity.ok(studyPlanService.setPlanAsCurrent(planCode));
    }

    @DeleteMapping("/{planCode}")
    public ResponseEntity<Void> deleteStudyPlan(@PathVariable String planCode) {
        studyPlanService.deleteStudyPlan(planCode);
        return ResponseEntity.noContent().build();
    }
}