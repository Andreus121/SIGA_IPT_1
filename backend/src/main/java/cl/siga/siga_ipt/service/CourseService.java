package cl.siga.siga_ipt.service;

import cl.siga.siga_ipt.dto.CourseCreateDto;
import cl.siga.siga_ipt.dto.CourseResponseDto;
import cl.siga.siga_ipt.dto.CourseUpdateDto;
import cl.siga.siga_ipt.entity.CourseEntity;
import cl.siga.siga_ipt.entity.PrerequisiteEntity;
import cl.siga.siga_ipt.repository.AcademicHistoryRepository;
import cl.siga.siga_ipt.repository.CourseRepository;
import cl.siga.siga_ipt.repository.PrerequisiteRepository;
import cl.siga.siga_ipt.repository.StudyPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final PrerequisiteRepository prerequisiteRepository;
    private final AcademicHistoryRepository academicHistoryRepository;

    // Crear una asignatura nueva
    @Transactional
    public CourseResponseDto createCourse(CourseCreateDto dto) {
        // 1. Validar existencia del plan de estudios
        if (!studyPlanRepository.existsByCode(dto.getStudyPlanCode())) {
            throw new IllegalArgumentException("El plan de estudio con el código '" + dto.getStudyPlanCode() + "' no existe.");
        }

        // 2. Unicidad del código en el plan de estudios
        if (courseRepository.existsByCodeAndStudyPlanCode(dto.getCode(), dto.getStudyPlanCode())) {
            throw new IllegalArgumentException("El código de asignatura '" + dto.getCode() + "' ya existe en el plan de estudios.");
        }

        // 3. Validar semestre
        validateSemester(dto.getSemester());

        // 4. Validar TEL
        validateTELHours(dto.getTheoryHours(), dto.getExerciseHours(), dto.getLabHours());

        // 5. Validar SCT
        validateSCT(dto.getSct());

        // 6. Validar prerrequisitos
        validatePrerequisites(dto.getPrerequisitesCodes(), dto.getStudyPlanCode(), dto.getSemester());

        // Guardar la entidad Asignatura
        CourseEntity course = CourseEntity.builder()
                .code(dto.getCode())
                .studyPlanCode(dto.getStudyPlanCode())
                .name(dto.getName())
                .semester(dto.getSemester())
                .theoryHours(dto.getTheoryHours())
                .exerciseHours(dto.getExerciseHours())
                .labHours(dto.getLabHours())
                .sct(dto.getSct())
                .build();

        courseRepository.save(course);

        // Guardar los prerrequisitos en la base de datos
        savePrerequisites(dto.getCode(), dto.getStudyPlanCode(), dto.getPrerequisitesCodes());

        return mapCourseToResponseDto(course);
    }

    // Actualizar una asignatura ya existente (menos su código)
    @Transactional
    public CourseResponseDto updateCourse(String studyPlanCode, String code, CourseUpdateDto dto) {
        CourseEntity course = courseRepository.findByCodeAndStudyPlanCode(code, studyPlanCode)
                .orElseThrow(() -> new IllegalArgumentException("La asignatura con código '" + code + "' en el plan '" + studyPlanCode + "' no existe."));

        // Verificar si va a cambiar el nombre
        if (dto.getName() != null && !dto.getName().isBlank()) {
            course.setName(dto.getName());
        }

        // Verificar si va a cambiar el semestre
        if (dto.getSemester() != null) {
            validateSemester(dto.getSemester());

            // Verificar que el nuevo semestre sea posterior a sus prerrequisitos existentes
            // Obtiene los prerrequisitos actuales
            List<PrerequisiteEntity> currentPrereqs = prerequisiteRepository.findByCourseCodeAndStudyPlanCode(code, studyPlanCode);
            // Revisa cada prerrequisito
            for (PrerequisiteEntity prereq : currentPrereqs) {
                CourseEntity prereqCourse = courseRepository.findByCodeAndStudyPlanCode(prereq.getPrerequisiteCourseCode(), studyPlanCode).orElse(null);
                // Verificar que el curso prerrequisito existe y es de un semestre inferior
                if (prereqCourse == null || prereqCourse.getSemester() >= dto.getSemester()) {
                    throw new IllegalArgumentException("El semestre de la asignatura debe ser estrictamente posterior al de sus prerrequisitos.");
                }
            }

            // Verificar que asignaturas que dependen de esta no se vean afectadas
            List<PrerequisiteEntity> dependentPrereqs = prerequisiteRepository.findByPrerequisiteCourseCodeAndStudyPlanCode(code, studyPlanCode);
            for (PrerequisiteEntity dep : dependentPrereqs) {
                CourseEntity dependentCourse = courseRepository.findByCodeAndStudyPlanCode(dep.getCourseCode(), studyPlanCode).orElse(null);
                // Verificar que estas asignaturas sean de un semestre superior
                if (dependentCourse != null && dto.getSemester() >= dependentCourse.getSemester()) {
                    // Indicar error porque el curso a modificar es el prerrequisito de otro superior
                    throw new IllegalArgumentException("No se puede mover la asignatura al semestre " + dto.getSemester() +
                            " porque es prerrequisito de '" + dependentCourse.getCode() + "' en el semestre " + dependentCourse.getSemester() + ".");
                }
            }

            course.setSemester(dto.getSemester());
        }

        // Actualización de horas TEL
        int t = dto.getTheoryHours() != null ? dto.getTheoryHours() : course.getTheoryHours();
        int e = dto.getExerciseHours() != null ? dto.getExerciseHours() : course.getExerciseHours();
        int l = dto.getLabHours() != null ? dto.getLabHours() : course.getLabHours();
        validateTELHours(t, e, l);

        if (dto.getTheoryHours() != null) course.setTheoryHours(dto.getTheoryHours());
        if (dto.getExerciseHours() != null) course.setExerciseHours(dto.getExerciseHours());
        if (dto.getLabHours() != null) course.setLabHours(dto.getLabHours());

        // Actualización de SCT
        if (dto.getSct() != null) {
            validateSCT(dto.getSct());
            course.setSct(dto.getSct());
        }

        // Actualización de Prerrequisitos
        if (dto.getPrerequisiteCodes() != null) {
            validatePrerequisites(dto.getPrerequisiteCodes(), studyPlanCode, course.getSemester());
            prerequisiteRepository.deleteByCourseCodeAndStudyPlanCode(code, studyPlanCode);
            savePrerequisites(code, studyPlanCode, dto.getPrerequisiteCodes());
        }

        courseRepository.save(course);
        return mapCourseToResponseDto(course);
    }

    // Obtener una asignatura por su código y plan de estudio
    @Transactional(readOnly = true)
    public CourseResponseDto getCourse(String studyPlanCode, String code) {
        CourseEntity course = courseRepository.findByCodeAndStudyPlanCode(code, studyPlanCode)
                .orElseThrow(() -> new IllegalArgumentException("La asignatura especificada no existe."));
        return mapCourseToResponseDto(course);
    }

    // obtener todas las asignaturas
    @Transactional(readOnly = true)
    public List<CourseResponseDto> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::mapCourseToResponseDto)
                .collect(Collectors.toList());
    }

    // Obtener las asignaturas de un plan de estudio
    @Transactional(readOnly = true)
    public List<CourseResponseDto> getCoursesByPlan(String studyPlanCode) {
        if (!studyPlanRepository.existsByCode(studyPlanCode)) {
            throw new IllegalArgumentException("El plan de estudios '" + studyPlanCode + "' no existe.");
        }
        return courseRepository.findByStudyPlanCode(studyPlanCode)
                .stream()
                .map(this::mapCourseToResponseDto)
                .collect(Collectors.toList());
    }

    // Borrar una asignatura
    @Transactional
    public void deleteCourse(String studyPlanCode, String code) {
        CourseEntity course = courseRepository.findByCodeAndStudyPlanCode(code, studyPlanCode)
                .orElseThrow(() -> new IllegalArgumentException("La asignatura especificada no existe."));

        // Regla: No eliminar físicamente si pertenece al historial académico
        if (academicHistoryRepository.existsByCourseCodeAndStudyPlanCode(code, studyPlanCode)) {
            throw new IllegalStateException("La asignatura forma parte del historial académico de uno o más estudiantes y no se puede eliminar.");
        }

        // Validar si es prerrequisito de alguna otra asignatura
        if (prerequisiteRepository.existsByPrerequisiteCourseCodeAndStudyPlanCode(code, studyPlanCode)) {
            throw new IllegalStateException("No se puede eliminar la asignatura porque es prerrequisito de otra asignatura en el plan de estudio.");
        }

        // Eliminar registros de prerrequisitos propios
        prerequisiteRepository.deleteByCourseCodeAndStudyPlanCode(code, studyPlanCode);

        // Eliminar asignatura
        courseRepository.delete(course);
    }

    // Funciones auxiliares
    private void validateSemester(Integer semester) {
        // El semestre debe ser entre 1 y 4
        if (semester == null || semester < 1 || semester > 4) {
            throw new IllegalArgumentException("El semestre debe ser un número entero entre 1 y 4.");
        }
    }

    private void validateTELHours(Integer t, Integer e, Integer l) {
        // Ninguna puede ser negativas
        if (t == null || e == null || l == null || t < 0 || e < 0 || l < 0) {
            throw new IllegalArgumentException("Las horas de Teoría, Ejercicios y Laboratorio no pueden ser negativas.");
        }
        // La suma total no puede ser mayor de 8 horas
        if (t + e + l >= 8) {
            throw new IllegalArgumentException("La suma de horas semanales TEL debe ser inferior a 8 horas (máximo 7 horas).");
        }
    }

    private void validateSCT(Integer sct) {
        // Una asignatura tiene de 0 a 6 sct
        if (sct == null || sct <= 0 || sct > 7) {
            throw new IllegalArgumentException("El valor SCT debe ser mayor que cero y menor o igual a 7.");
        }
    }

    private void validatePrerequisites(Set<String> prerequisiteCodes, String studyPlanCode, Integer courseSemester) {
        // Si no tiene prerrequisitos se acepta
        if (prerequisiteCodes == null || prerequisiteCodes.isEmpty()) {
            return;
        }

        // Máximo 3 prerrequisitos
        if (prerequisiteCodes.size() > 3) {
            throw new IllegalArgumentException("Una asignatura no puede tener más de 3 prerrequisitos.");
        }

        // Revisar que cada prerrequisito existe
        for (String prereqCode : prerequisiteCodes) {
            CourseEntity prereqCourse = courseRepository.findByCodeAndStudyPlanCode(prereqCode, studyPlanCode)
                    .orElseThrow(() -> new IllegalArgumentException("El prerrequisito con código '" + prereqCode + "' no existe en el plan de estudios."));

            // Ver que la asignatura de prerrequisito es de un semestre inferior
            if (prereqCourse.getSemester() >= courseSemester) {
                throw new IllegalArgumentException("El prerrequisito '" + prereqCode + "' (Semestre " + prereqCourse.getSemester() +
                        ") debe pertenecer a un semestre anterior al de la asignatura (Semestre " + courseSemester + ").");
            }
        }
    }

    // guardar todos los prerrequisitos de una asignatura
    private void savePrerequisites(String courseCode, String studyPlanCode, Set<String> prerequisiteCodes) {
        if (prerequisiteCodes == null) return;

        for (String prereqCode : prerequisiteCodes) {
            PrerequisiteEntity entity = PrerequisiteEntity.builder()
                    .courseCode(courseCode)
                    .studyPlanCode(studyPlanCode)
                    .prerequisiteCourseCode(prereqCode)
                    .build();
            prerequisiteRepository.save(entity);
        }
    }

    // Pasar un entity a un response
    private CourseResponseDto mapCourseToResponseDto(CourseEntity course) {
        List<PrerequisiteEntity> prerequisites = prerequisiteRepository.findByCourseCodeAndStudyPlanCode(course.getCode(), course.getStudyPlanCode());
        Set<String> prerequisitesCodes = prerequisites.stream()
                .map(PrerequisiteEntity::getPrerequisiteCourseCode)
                .collect(Collectors.toSet());

        return CourseResponseDto.builder()
                .id(course.getId())
                .code(course.getCode())
                .name(course.getName())
                .semester(course.getSemester())
                .theoryHours(course.getTheoryHours())
                .exerciseHours(course.getExerciseHours())
                .labHours(course.getLabHours())
                .sct(course.getSct())
                .studyPlanCode(course.getStudyPlanCode())
                .prerequisitesCodes(prerequisitesCodes)
                .build();
    }
}