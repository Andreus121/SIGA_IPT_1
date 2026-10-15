package cl.siga.siga_ipt.service;

import cl.siga.siga_ipt.dto.EnrollmentCreateDto;
import cl.siga.siga_ipt.dto.EnrollmentResponseDto;
import cl.siga.siga_ipt.entity.AcademicPeriodEntity;
import cl.siga.siga_ipt.entity.EnrollmentEntity;
import cl.siga.siga_ipt.entity.StudentEntity;
import cl.siga.siga_ipt.enums.EnrollmentStatus;
import cl.siga.siga_ipt.enums.PeriodStatus;
import cl.siga.siga_ipt.enums.StudentStatus;
import cl.siga.siga_ipt.repository.AcademicPeriodRepository;
import cl.siga.siga_ipt.repository.EnrollmentRepository;
import cl.siga.siga_ipt.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final AcademicPeriodRepository periodRepository;

    @Transactional
    public EnrollmentResponseDto enrollStudent(EnrollmentCreateDto dto) {
        // 1. Validar que el estudiante exista y esté REGULAR
        StudentEntity student = studentRepository.findByRun(dto.getStudentRun())
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado con RUN: " + dto.getStudentRun()));

        if (student.getStatus() != StudentStatus.REGULAR) {
            throw new IllegalStateException("Solo los estudiantes en estado REGULAR pueden matricularse en el período.");
        }

        // 2. Validar que el período exista y esté ABIERTO
        AcademicPeriodEntity period = periodRepository.findByPeriodCode(dto.getPeriodCode())
                .orElseThrow(() -> new IllegalArgumentException("El período académico '" + dto.getPeriodCode() + "' no existe."));

        if (period.getStatus() != PeriodStatus.OPEN) {
            throw new IllegalStateException("Solo se pueden realizar matrículas en períodos académicos con estado ABIERTO.");
        }

        // 3. Validar que no esté ya matriculado en el período
        if (enrollmentRepository.existsByStudentRunAndPeriodCode(dto.getStudentRun(), dto.getPeriodCode())) {
            throw new IllegalArgumentException("El estudiante ya se encuentra matriculado en el período académico " + dto.getPeriodCode());
        }

        EnrollmentEntity enrollment = EnrollmentEntity.builder()
                .studentRun(dto.getStudentRun())
                .periodCode(dto.getPeriodCode())
                .enrollmentDate(LocalDate.now())
                .status(EnrollmentStatus.ENROLLED)
                .build();

        EnrollmentEntity saved = enrollmentRepository.save(enrollment);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponseDto> getEnrollmentsByPeriod(String periodCode) {
        return enrollmentRepository.findByPeriodCode(periodCode).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponseDto> getEnrollmentsByStudent(String run) {
        return enrollmentRepository.findByStudentRun(run).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private EnrollmentResponseDto mapToDto(EnrollmentEntity entity) {
        return EnrollmentResponseDto.builder()
                .id(entity.getId())
                .studentRun(entity.getStudentRun())
                .periodCode(entity.getPeriodCode())
                .enrollmentDate(entity.getEnrollmentDate())
                .status(entity.getStatus())
                .build();
    }
}
