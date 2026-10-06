package cl.siga.siga_ipt.controller;


import cl.siga.siga_ipt.dto.CareerCreateDto;
import cl.siga.siga_ipt.dto.CareerResponseDto;
import cl.siga.siga_ipt.dto.CareerUpdateDto;
import cl.siga.siga_ipt.service.CareerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/careers")
@RequiredArgsConstructor
public class CareerController {

    private final CareerService careerService;

    @PostMapping
    public ResponseEntity<CareerResponseDto> createCareer(@Valid @RequestBody CareerCreateDto dto) {
        return new ResponseEntity<>(careerService.createCareer(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CareerResponseDto>> getAllCareers() {
        return new ResponseEntity<>(careerService.getAllCareers(), HttpStatus.OK);
    }

    @GetMapping("/{code}")
    public ResponseEntity<CareerResponseDto> getCareerByCode(@PathVariable String code) {
        return new ResponseEntity<>(careerService.getCareerByCode(code), HttpStatus.OK);
    }

    @PutMapping("/{code}")
    public ResponseEntity<CareerResponseDto> updateCareer(
            @PathVariable String code,
            @RequestBody CareerUpdateDto dto) {
        return new ResponseEntity<>(careerService.updateCareer(code, dto), HttpStatus.OK);
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteCareer(@PathVariable String code) {
        careerService.deleteCareer(code);
        return ResponseEntity.noContent().build();
    }
}