package cl.siga.siga_ipt.service;

import cl.siga.siga_ipt.dto.GradeEntryDto;
import cl.siga.siga_ipt.dto.RegistrationResponseDto;
import cl.siga.siga_ipt.entity.AcademicPeriodEntity;
import cl.siga.siga_ipt.entity.RegistrationEntity;
import cl.siga.siga_ipt.enums.PeriodStatus;
import cl.siga.siga_ipt.enums.RegistrationStatus;
import cl.siga.siga_ipt.repository.AcademicPeriodRepository;
import cl.siga.siga_ipt.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final RegistrationRepository registrationRepository;
    private final AcademicPeriodRepository periodRepository;

    // Guarda la nota final de una toma de ramos
    @Transactional
    public RegistrationResponseDto registerGrade(GradeEntryDto dto) {
        // Verifica que esa toma de ramos si existe
        RegistrationEntity registration = registrationRepository.findById(dto.getRegistrationId())
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada con ID: " + dto.getRegistrationId()));

        // Verifica que el registro está activo
        if (registration.getStatus() != RegistrationStatus.REGISTERED) {
            throw new IllegalStateException("Solo se pueden registrar calificaciones en inscripciones activas (REGISTERED).");
        }

        // Verificar el periodo académico y su estado open
        AcademicPeriodEntity period = periodRepository.findByPeriodCode(registration.getPeriodCode())
                .orElseThrow(() -> new IllegalArgumentException("El período académico '" + registration.getPeriodCode() + "' no existe."));

        if (period.getStatus() != PeriodStatus.OPEN) {
            throw new IllegalStateException("Las calificaciones solo pueden ser ingresadas o modificadas en períodos académicos con estado ABIERTO.");
        }

        // Verificar que la nota sea entre 1 y 7
        if (dto.getFinalGrade() < 1.0 || dto.getFinalGrade() > 7.0) {
            throw new IllegalArgumentException("La calificación final debe encontrarse entre 1.0 y 7.0.");
        }

        // guardar nota
        registration.setFinalGrade(dto.getFinalGrade());
        RegistrationEntity saved = registrationRepository.save(registration);

        return RegistrationResponseDto.builder()
                .id(saved.getId())
                .studentRun(saved.getStudentRun())
                .courseCode(saved.getCourseCode())
                .sectionCode(saved.getSectionCode())
                .periodCode(saved.getPeriodCode())
                .registrationDate(saved.getRegistrationDate())
                .finalGrade(saved.getFinalGrade())
                .status(saved.getStatus())
                .build();
    }

    // Obtener todas las notas de una sección
    @Transactional(readOnly = true)
    public List<RegistrationResponseDto> getGradesBySection(String sectionCode, String periodCode) {
        return registrationRepository.findBySectionCodeAndPeriodCode(sectionCode, periodCode).stream()
                .map(reg -> RegistrationResponseDto.builder()
                        .id(reg.getId())
                        .studentRun(reg.getStudentRun())
                        .courseCode(reg.getCourseCode())
                        .sectionCode(reg.getSectionCode())
                        .periodCode(reg.getPeriodCode())
                        .registrationDate(reg.getRegistrationDate())
                        .finalGrade(reg.getFinalGrade())
                        .status(reg.getStatus())
                        .build())
                .collect(Collectors.toList());
    }
}
