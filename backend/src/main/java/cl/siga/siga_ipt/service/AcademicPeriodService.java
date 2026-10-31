package cl.siga.siga_ipt.service;

import cl.siga.siga_ipt.dto.AcademicPeriodCreateDto;
import cl.siga.siga_ipt.dto.AcademicPeriodResponseDto;
import cl.siga.siga_ipt.entity.AcademicPeriodEntity;
import cl.siga.siga_ipt.repository.AcademicPeriodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AcademicPeriodService {

    private final AcademicPeriodRepository periodRepository;

    @Transactional
    public AcademicPeriodResponseDto createPeriod(AcademicPeriodCreateDto dto) {
        if (periodRepository.existsByPeriodCode(dto.getPeriodCode())
                || periodRepository.existsByAcademicPeriod(dto.getPeriodCode())) {
            throw new IllegalArgumentException(
                    "Ya existe un período académico con el código: " + dto.getPeriodCode());
        }

        // Business logic: build entity here, not in controller
        AcademicPeriodEntity entity = AcademicPeriodEntity.builder()
                .periodCode(dto.getPeriodCode())
                .academicPeriod(dto.getPeriodCode())
                .status(dto.getStatus())
                .build();
        AcademicPeriodEntity saved = periodRepository.save(entity);
        return toResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public AcademicPeriodResponseDto getByCode(String periodCode) {
        AcademicPeriodEntity entity = periodRepository.findByPeriodCode(periodCode)
                .orElseThrow(() -> new IllegalArgumentException("Periodo no encontrado con el código: " + periodCode));
        return toResponseDto(entity);
    }

    @Transactional(readOnly = true)
    public List<AcademicPeriodResponseDto> getAllPeriods() {
        return periodRepository.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    private AcademicPeriodResponseDto toResponseDto(AcademicPeriodEntity entity) {
        return AcademicPeriodResponseDto.builder()
                .periodCode(entity.getPeriodCode())
                .status(entity.getStatus())
                .build();
    }
}
