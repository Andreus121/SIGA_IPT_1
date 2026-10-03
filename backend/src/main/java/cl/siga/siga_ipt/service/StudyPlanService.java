package cl.siga.siga_ipt.service;

import cl.siga.siga_ipt.dto.StudyPlanCreateDto;
import cl.siga.siga_ipt.dto.StudyPlanResponseDto;
import cl.siga.siga_ipt.entity.CareerEntity;
import cl.siga.siga_ipt.entity.StudyPlanEntity;
import cl.siga.siga_ipt.enums.StudyPlanStatus;
import cl.siga.siga_ipt.repository.CareerRepository;
import cl.siga.siga_ipt.repository.CourseRepository;
import cl.siga.siga_ipt.repository.StudentRepository;
import cl.siga.siga_ipt.repository.StudyPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyPlanService {

    private final StudyPlanRepository studyPlanRepository;
    private final CareerRepository careerRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    // Crear un plan de estudio nuevo
    @Transactional
    public StudyPlanResponseDto createStudyPlan(StudyPlanCreateDto dto) {
        // Verificar que el código es único
        if (studyPlanRepository.findByCode(dto.getCode()).isPresent()) {
            throw new IllegalArgumentException("El código de plan de estudios ya existe.");
        }

        // Verificar que el código de carrera existe
        CareerEntity career = careerRepository.findByCode(dto.getCareerCode())
                .orElseThrow(() -> new IllegalArgumentException("La carrera asociada no existe."));

        // Estado inicial no vigente
        StudyPlanStatus initialStatus = StudyPlanStatus.NOT_CURRENT;

        // Regla: Si se marca como vigente o es el primer plan de la carrera, pasa a ser CURRENT
        boolean shouldBeCurrent = Boolean.TRUE.equals(dto.getSetCurrent())
                || studyPlanRepository.findAllByCareerCode(career.getCode()).isEmpty();

        // Si va a ser el plan de estudio vigente
        if (shouldBeCurrent) {
            // Pasar el plan vigente anterior a no vigente
            studyPlanRepository.findByCareerCodeAndStatus(career.getCode(), StudyPlanStatus.CURRENT)
                    // Si encuentra otro plan que estaba en vigente, se cambia a no vigente
                    .ifPresent(oldCurrent -> {
                        oldCurrent.setStatus(StudyPlanStatus.NOT_CURRENT);
                        // Guardar el cambio
                        studyPlanRepository.save(oldCurrent);
                    });
            // El estado inicial será vigente
            initialStatus = StudyPlanStatus.CURRENT;
        }

        // Crear entidad del plan de estudio
        StudyPlanEntity studyPlan = StudyPlanEntity.builder()
                .code(dto.getCode())
                .careerCode(career.getCode())
                .name(dto.getName())
                .status(initialStatus)
                .build();

        // Guardar plan de estudio y retornar
        studyPlanRepository.save(studyPlan);
        return mapStudyPlanToResponseDto(studyPlan);
    }

    // Cambiar un plan de estudio como el vigente
    @Transactional
    public StudyPlanResponseDto setPlanAsCurrent(String planCode) {
        // Verificar que el plan de estudio existe
        StudyPlanEntity studyPlan = studyPlanRepository.findByCode(planCode)
                .orElseThrow(() -> new IllegalArgumentException("El plan de estudios no existe."));

        // Si el plan de estudio ya es vigente, se queda igual y retorna
        if (studyPlan.getStatus() == StudyPlanStatus.CURRENT) {
            return mapStudyPlanToResponseDto(studyPlan);
        }

        // Regla: Desmarcar el plan vigente anterior para la misma carrera
        studyPlanRepository.findByCareerCodeAndStatus(studyPlan.getCareerCode(), StudyPlanStatus.CURRENT)
                .ifPresent(oldCurrent -> {
                    oldCurrent.setStatus(StudyPlanStatus.NOT_CURRENT);
                    studyPlanRepository.save(oldCurrent);
                });

        // Cambiar estado a vigente y guardar
        studyPlan.setStatus(StudyPlanStatus.CURRENT);
        studyPlanRepository.save(studyPlan);
        return mapStudyPlanToResponseDto(studyPlan);
    }

    // Borrar un plan de estudio
    @Transactional
    public void deleteStudyPlan(String planCode) {
        StudyPlanEntity plan = studyPlanRepository.findByCode(planCode)
                .orElseThrow(() -> new IllegalArgumentException("El plan de estudios no existe."));

        // Regla: Un plan asociado a estudiantes no puede ser eliminado físicamente
        if (studentRepository.existsByStudyPlanCode(planCode)) {
            throw new IllegalStateException("No se puede eliminar un plan de estudios que posee estudiantes asociados.");
        }

        // Regla mia: Un plan asociado a una asignatura/curso no puede ser eliminado físicamente
        if (courseRepository.existsByStudyPlanCode(planCode)) {
            throw new IllegalStateException("No se puede eliminar un plan de estudios que posee asignaturas asociadas.");
        }
        // Si no tiene alumnos ni asignaturas eliminar
        studyPlanRepository.delete(plan);
    }

    @Transactional(readOnly = true)
    public List<StudyPlanResponseDto> getPlansByCareer(String careerCode) {
        return studyPlanRepository.findAllByCareerCode(careerCode).stream()
                .map(this::mapStudyPlanToResponseDto)
                .toList();
    }

    // Función auxiliar
    private StudyPlanResponseDto mapStudyPlanToResponseDto(StudyPlanEntity studyPlan){
        return StudyPlanResponseDto.builder()
                .id(studyPlan.getId())
                .code(studyPlan.getCode())
                .careerCode(studyPlan.getCareerCode())
                .name(studyPlan.getName())
                .status(studyPlan.getStatus())
                .build();
    }
}
