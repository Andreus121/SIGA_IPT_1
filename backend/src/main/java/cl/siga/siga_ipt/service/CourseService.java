package cl.siga.siga_ipt.service;

import cl.siga.siga_ipt.dto.CourseCreateDto;
import cl.siga.siga_ipt.dto.CourseResponseDto;
import cl.siga.siga_ipt.dto.CourseUpdateDto;
import cl.siga.siga_ipt.entity.CourseEntity;
import cl.siga.siga_ipt.entity.PrerequisiteEntity;
import cl.siga.siga_ipt.repository.CourseRepository;
import cl.siga.siga_ipt.repository.PrerequisiteRepository;
import cl.siga.siga_ipt.repository.StudyPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final PrerequisiteRepository prerequisiteRepository;

    @Transactional
    public CourseResponseDto createCourse(CourseCreateDto dto){
        // Verificar que el plan de estudio si existe
        if (!studyPlanRepository.existsByCode(dto.getStudyPlanCode())){
            throw new IllegalArgumentException("El plan de estudio con el código: "+ dto.getStudyPlanCode()+" no existe");
        }
        // Verificar que no existe otra asignatura en el plan de estudio que no tenga ese código
        if (courseRepository.existsByCodeAndStudyPlanCode(dto.getCode(), dto.getStudyPlanCode())) {
            throw new IllegalArgumentException("El código de asignatura '" + dto.getCode() + "' ya existe en el plan de estudios.");
        }

        // Verificar semestre
        if (dto.getSemester() < 1 || dto.getSemester() >4){
            throw new IllegalArgumentException("El semestre debe ser un número del 1 al 4");
        }

        // Verificar suma total de horas
        if (dto.getExerciseHours() + dto.getLabHours() + dto.getTheoryHours() >= 8){
            throw new IllegalArgumentException("La cantidad de horas semanales no puede ser mayor de 7");
        }

        // Verificar cantidad de sct
        if (dto.getSct()<=0 || dto.getSct() >7){
            throw new IllegalArgumentException("La cantidad de Sct debe ser valores del 1 al 7");
        }

        // Verificar límite de prerequisites
        if (dto.getPrerequisiteCodes().size() > 3){
            throw new IllegalArgumentException("Una asignatura no puede tener más de 3 pre-requisitos");
        }

        // Verificar que cada pre-requisito existe
        if (!dto.getPrerequisiteCodes().isEmpty()){
            String studyPlanCode = dto.getStudyPlanCode();
            // Verificar cada pre-requisito
            for (String code : dto.getPrerequisiteCodes()) {
                // Cuando no encuentre la asignatura error, no existe
                if (!courseRepository.existsByCodeAndStudyPlanCode(code, studyPlanCode)) {
                    throw new IllegalArgumentException("La asignatura prerrequisito con el código: " + code + " no existe en el plan de estudio.");
                }
            }
        }

        // Crear entidad de la asignatura
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

        // Guardar asignatura en la base de datos
        courseRepository.save(course);
        //retornar respuesta
        return mapCourseToResponseDto(course);
    }

    @Transactional
    public CourseResponseDto updateCourse(CourseUpdateDto dto){
        // Verificar que la asignatura a actualizar si existe
        CourseEntity course = courseRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("La asignatura a editar no existe."));

        // Si va a hacer un cambio en el plan de estudio
        if(dto.getStudyPlanCode()!=null&& !dto.getStudyPlanCode().isBlank()){
            // Verificar que el plan de estudio si existe
            if (!studyPlanRepository.existsByCode(dto.getStudyPlanCode())){
                throw new IllegalArgumentException("El plan de estudio con el código: "+ dto.getStudyPlanCode()+" no existe");
            }
            // Guardar cambio
            course.setStudyPlanCode(dto.getStudyPlanCode());
        }

        // Si va a cambiar el código de la asignatura
        if(dto.getCode()!=null && !dto.getCode().isBlank()){
            // Verificar que no existe otra asignatura en el plan de estudio que no tenga ese código
            if (courseRepository.existsByCodeAndStudyPlanCode(dto.getCode(), dto.getStudyPlanCode())) {
                throw new IllegalArgumentException("El código de asignatura '" + dto.getCode() + "' ya existe en el plan de estudios.");
            }
            course.setCode(dto.getCode());
        }

        // Si va a cambiar el semestre en el que se cursa
        if(dto.getSemester()!=null){
            // Verificar semestre
            if (dto.getSemester() < 1 || dto.getSemester() > 4) {
                throw new IllegalArgumentException("El semestre debe ser un número del 1 al 4");
            }
            course.setSemester(dto.getSemester());
        }

        // Si va a cambiar las horas de ejercicios
        if(dto.getExerciseHours()!=null){
            // Guardar cambio
            course.setExerciseHours(dto.getExerciseHours());
        }

        // Si va a cambiar las horas de laboratorio
        if(dto.getLabHours()!=null){
            // Guardar cambio
            course.setLabHours(dto.getLabHours());
        }

        // Si va a cambiar las horas de teoría
        if(dto.getTheoryHours()!=null){
            // Guardar cambio
            course.setTheoryHours(dto.getTheoryHours());
        }

        // Verificar suma total de horas
        if (course.getExerciseHours() + course.getLabHours() + course.getTheoryHours() >= 8) {
            throw new IllegalArgumentException("La cantidad de horas semanales no puede ser mayor de 7");
        }

        // Si va a cambiar los sct del ramo
        if(dto.getSct()!=null){
            // Verificar cantidad de sct
            if (dto.getSct() <= 0 || dto.getSct() > 7) {
                throw new IllegalArgumentException("La cantidad de Sct debe ser valores del 1 al 7");
            }
            course.setSct(dto.getSct());
        }

        // Si va a cambiar los prerrequisitos
        if(dto.getPrerequisiteCodes()!=null){
            // Verificar límite de prerequisites
            if (dto.getPrerequisiteCodes().size() > 3) {
                throw new IllegalArgumentException("Una asignatura no puede tener más de 3 pre-requisitos");
            }

            // Eliminar los prerequisitos que ya tenía si es que tenía

        }

        courseRepository.save(course);
        return mapCourseToResponseDto(course);
    }

    // Función auxiliar
    private CourseResponseDto mapCourseToResponseDto(CourseEntity course){
        // Extraer todos los prerequisites de la asignatura
        List<PrerequisiteEntity> prerequisites = prerequisiteRepository.findByCourseCodeAndStudyPlanCode(course.getCode(),course.getStudyPlanCode());
        // Extraer solo sus códigos
        Set<String> prerequisitesCodes = new HashSet<>();
        for(PrerequisiteEntity prerequisite : prerequisites){
            prerequisitesCodes.add(prerequisite.getPrerequisiteCourseCode());
        }

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
                .prerequisites(prerequisitesCodes)
                .build();
    }

}
