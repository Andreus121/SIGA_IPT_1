package cl.siga.siga_ipt.service;

/**
 * Servicio para la gestión de docentes.
 * Contiene la lógica de negocio y validaciones requeridas.
 */
import cl.siga.siga_ipt.dto.TeacherCreateDto;
import cl.siga.siga_ipt.dto.TeacherResponseDto;
import cl.siga.siga_ipt.dto.TeacherUpdateDto;
import cl.siga.siga_ipt.entity.TeacherEntity;

import cl.siga.siga_ipt.enums.PeriodStatus;

import cl.siga.siga_ipt.enums.TeacherStatus;
import cl.siga.siga_ipt.repository.AcademicHistoryRepository;
import cl.siga.siga_ipt.repository.AcademicPeriodRepository;
import cl.siga.siga_ipt.repository.SectionRepository;
import cl.siga.siga_ipt.repository.TeacherRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final SectionRepository sectionRepository;
    private final AcademicPeriodRepository periodRepository;
    private final AcademicHistoryRepository academicHistoryRepository;

    // Crear un profesor
    @Transactional
    public TeacherResponseDto createTeacher(TeacherCreateDto dto) {
        // Verificar RUN único
        if (teacherRepository.existsByRun(dto.getRun())) {
            throw new IllegalArgumentException("Ya existe un docente registrado con el RUN: " + dto.getRun());
        }
        // Verificar email único
        if (teacherRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Ya existe un docente registrado con el correo: " + dto.getEmail());
        }

        // Crear profesor
        TeacherEntity teacher = TeacherEntity.builder()
                .run(dto.getRun())
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .professionalTitle(dto.getProfessionalTitle())
                .academicDegree(dto.getAcademicDegree())
                .status(TeacherStatus.ACTIVE) // Regla
                .build();



        // Guardar y retornar
        teacherRepository.save(teacher);
        return mapTeacherToDto(teacher);
    }

    // Obtener todos los profesores
    @Transactional(readOnly = true)
    public List<TeacherResponseDto> getAllTeachers(TeacherStatus status) {
        List<TeacherEntity> teachers = (status != null)
                ? teacherRepository.findByStatus(status)
                : teacherRepository.findAll();

        return teachers.stream()
                .map(this::mapTeacherToDto)
                .toList();
    }

    // Obtener un profesor por su ID
    @Transactional(readOnly = true)
    public TeacherResponseDto getTeacherById(Long id) {
        TeacherEntity entity = teacherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Docente no encontrado con el ID: " + id));
        return mapTeacherToDto(entity);
    }

    @Transactional(readOnly = true)
    public TeacherResponseDto getTeacherByRun(String run) {
        TeacherEntity teacher = teacherRepository.findByRun(run)
                .orElseThrow(() -> new IllegalArgumentException("Docente no encontrado con RUN: " + run));
        return mapTeacherToDto(teacher);
    }

    // Actualizar datos de un profesor
    @Transactional
    public TeacherResponseDto updateTeacher(Long id, TeacherUpdateDto dto) {
        // Encontrar al profesor a editar
        TeacherEntity teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Docente no encontrado con el ID: " + id));

        // Si quiere cambiar el run
        if(dto.getRun()!=null && !dto.getRun().isBlank()){
            // Verificar que el run no pertenece a otro docente
            if(teacherRepository.existsByRunAndIdNot(dto.getRun(), id)){
                throw new IllegalArgumentException("El run proporcionado es de otro docente");
            }
            teacher.setRun(dto.getRun());
        }

        // Si quiere cambiar el nombre del profesor
        if(dto.getFullName()!=null && !dto.getFullName().isBlank()){
            teacher.setFullName(dto.getFullName());
        }

        // Si quiere cambiar el email
        if(dto.getEmail()!=null && !dto.getEmail().isBlank()){
            // Verificar que no sea de otro profesor
            if (teacherRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
                throw new IllegalArgumentException("El correo proporcionado ya pertenece a otro docente: " + dto.getEmail());
            }
            // Guardar email
            teacher.setEmail(dto.getEmail());
        }

        // Si quiere cambiar el título profesional
        if(dto.getProfessionalTitle()!=null && !dto.getProfessionalTitle().isBlank()){
            teacher.setProfessionalTitle(dto.getProfessionalTitle());
        }

        // Si quiere cambiar el grado académico
        if(dto.getAcademicDegree()!= null && !dto.getAcademicDegree().isBlank()){
            teacher.setAcademicDegree(dto.getAcademicDegree());
        }

        teacherRepository.save(teacher);
        return mapTeacherToDto(teacher);
    }

    // Cambiar el estado de un profesor
    @Transactional
    public TeacherResponseDto changeTeacherStatus(Long id, TeacherStatus newStatus) {
        // Verificar que existe el profesor a cambiar estado
        TeacherEntity teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Docente no encontrado con el ID: " + id));

        // Si el nuevo estado es INACTIVO
        if (newStatus == TeacherStatus.INACTIVE) {
            // Regla de negocio, no cambiar a INACTIVE si tiene secciones asignadas en un período académico ABIERTO
            boolean hasOpenPeriodSections = sectionRepository.findAll().stream()
                    .filter(s -> s.getTeacherId().equals(id))
                    .anyMatch(s -> periodRepository.findByPeriodCode(s.getPeriodCode())
                            .map(p -> p.getStatus() == PeriodStatus.OPEN)
                            .orElse(false));

            if (hasOpenPeriodSections) {
                throw new IllegalStateException("No se puede inactivar al docente porque tiene secciones asignadas en un período académico ABIERTO.");
            }
        }

        // Si el nuevo estado es Activo solo se cambia y ya
        teacher.setStatus(newStatus);
        teacherRepository.save(teacher);
        return mapTeacherToDto(teacher);
    }

    // Borrar un profesor
    @Transactional
    public void deleteTeacher(Long id) {
        // Encontrar profesor y lanzar error si no se encuentra
        TeacherEntity teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Docente no encontrado con el ID: " + id));

        // Verificar si tiene secciones asociadas
        if (sectionRepository.existsByTeacherId(id)) {
            throw new IllegalStateException("No se puede eliminar el docente porque tiene secciones asociadas en el sistema.");
        }

        // Verificar si tiene historial académico
        if (academicHistoryRepository.existsByTeacherRun(teacher.getRun())) {
            throw new IllegalStateException("No se puede eliminar el docente porque registra historial académico en el sistema.");
        }

        teacherRepository.delete(teacher);
    }

    private TeacherResponseDto mapTeacherToDto(TeacherEntity teacher) {
        return TeacherResponseDto.builder()
                .id(teacher.getId())
                .run(teacher.getRun())
                .fullName(teacher.getFullName())
                .email(teacher.getEmail())
                .professionalTitle(teacher.getProfessionalTitle())
                .academicDegree(teacher.getAcademicDegree())
                .status(teacher.getStatus())
                .build();
    }
}