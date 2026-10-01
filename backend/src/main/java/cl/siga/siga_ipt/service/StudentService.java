package cl.siga.siga_ipt.service;


import cl.siga.siga_ipt.dto.StudentCreateDto;
import cl.siga.siga_ipt.dto.StudentResponseDto;
import cl.siga.siga_ipt.dto.StudentUpdateDto;
import cl.siga.siga_ipt.entity.CareerEntity;
import cl.siga.siga_ipt.entity.StudentEntity;
import cl.siga.siga_ipt.entity.StudyPlanEntity;
import cl.siga.siga_ipt.entity.UserEntity;
import cl.siga.siga_ipt.enums.CareerStatus;
import cl.siga.siga_ipt.enums.Role;
import cl.siga.siga_ipt.enums.StudentStatus;
import cl.siga.siga_ipt.enums.StudyPlanStatus;
import cl.siga.siga_ipt.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final CareerRepository careerRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final RegistrationRepository registrationRepository;
    private final AcademicHistoryRepository academicHistoryRepository;

    // Registrar un estudiante nuevo en la tabla de estudiantes
    @Transactional
    public StudentResponseDto registerStudent(StudentCreateDto dto) {

        // 1. Validar unicidad de RUN y Email
        if (studentRepository.existsByRun(dto.getRun())) {
            throw new IllegalArgumentException("El RUN ya se encuentra registrado.");
        }
        if (studentRepository.existsByEmail(dto.getEmail()) || userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("El correo electrónico ya se encuentra registrado.");
        }

        // 2. Validar que la carrera exista y esté ACTIVA
        CareerEntity career = careerRepository.findByCode(dto.getCareerCode())
                .orElseThrow(() -> new IllegalArgumentException("La carrera especificada no existe."));

        if (career.getStatus() != CareerStatus.ACTIVE) {
            throw new IllegalStateException("No se pueden registrar estudiantes en una carrera INACTIVA.");
        }

        // 3. Obtener el plan de estudio VIGENTE de la carrera
        StudyPlanEntity studyPlan = studyPlanRepository.findByCareerCodeAndStatus(career.getCode(), StudyPlanStatus.CURRENT)
                .orElseThrow(() -> new IllegalStateException("La carrera no tiene un plan de estudios vigente asignado."));

        // Crear el estudiante con sus datos
        StudentEntity student = StudentEntity.builder()
                .run(dto.getRun())
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .careerCode(career.getCode())
                .studyPlanCode(studyPlan.getCode())
                .status(StudentStatus.REGULAR)
                .build();

        // Guardar en la base de datos
        studentRepository.save(student);

        // 5. Crear cuenta de usuario asociada para autenticación
        UserEntity user = UserEntity.builder()
                .email(dto.getEmail())
                .password(dto.getPassword() != null ? dto.getPassword() : "DefaultPassword123!") // Usar encoder en prod
                .role(Role.STUDENT)
                .enabled(true)
                .build();

        userRepository.save(user);

        return mapStudentToResponseDto(student);
    }

    // Actualiza un estudiante ya existente
    @Transactional
    public StudentResponseDto updateStudent(Long id, StudentUpdateDto dto) {
        StudentEntity student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado."));

        // Actualización de Nombre Completo
        // Ver si está en blanco (no requiere cambio)
        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            // Guardar nombre nuevo
            student.setFullName(dto.getFullName());
        }

        // Actualización de Email
        // Ver si está en blanco (no requiere cambio)
        if (dto.getEmail() != null && !dto.getEmail().equalsIgnoreCase(student.getEmail())) {
            // Verificar si ya existe ese correo
            if (studentRepository.existsByEmail(dto.getEmail()) || userRepository.existsByEmail(dto.getEmail())) {
                // Indicar que el correo está ocupado
                throw new IllegalArgumentException("El nuevo correo electrónico ya está en uso.");
            }

            // Actualizar email en Student y en User
            userRepository.findByEmail(student.getEmail()).ifPresent(user -> {
                user.setEmail(dto.getEmail());
                userRepository.save(user);
            });
            student.setEmail(dto.getEmail());
        }

        // Cambio de Carrera y Plan de Estudio
        // Verificar que no esté en blanco o sea la misma que ya tenía
        if (dto.getCareerCode() != null && !dto.getCareerCode().equals(student.getCareerCode())) {
            // Regla: Solo se puede modificar mientras no tenga inscripciones ni historial académico
            // Revisar registrationRepository (periodo académico actual)
            boolean hasRegistrations = registrationRepository.existsByStudentRun(student.getRun());
            // Revisar historial académico (periodos académicos pasados)
            boolean hasHistory = academicHistoryRepository.existsByStudentRun(student.getRun());

            // Si ya tiene historial no se puede cambiar de carrera
            if (hasRegistrations || hasHistory) {
                throw new IllegalStateException("No se puede cambiar la carrera de un estudiante que posee inscripciones o historial académico.");
            }

            // Buscar la nueva carrera
            CareerEntity career = careerRepository.findByCode(dto.getCareerCode())
                    // Avisar si no existe
                    .orElseThrow(() -> new IllegalArgumentException("La nueva carrera no existe."));

            // Verificar que la nueva carrera esté activa
            if (career.getStatus() != CareerStatus.ACTIVE) {
                // Avisar si no está activa
                throw new IllegalStateException("No se puede cambiar a una carrera INACTIVA.");
            }

            // Buscar el plan de estudio elegido
            StudyPlanEntity currentPlan = studyPlanRepository.findByCareerCodeAndStatus(career.getCode(), StudyPlanStatus.CURRENT)
                    // Si el plan de estudio no está vigente, avisar
                    .orElseThrow(() -> new IllegalStateException("La nueva carrera no tiene un plan vigente."));

            // Guardar nueva carrera y plan de estudio
            student.setCareerCode(career.getCode());
            student.setStudyPlanCode(currentPlan.getCode());
        }

        // Cambio manual de Estado por el ADMIN
        if (dto.getStatus() != null) {
            // Regla: Estados EGRESADO y ELIMINADO no se pueden asignar manualmente
            if (dto.getStatus() == StudentStatus.GRADUATED || dto.getStatus() == StudentStatus.DROPPED) {
                throw new IllegalArgumentException("Los estados EGRESADO y ELIMINADO son gestionados automáticamente por el sistema.");
            }
            student.setStatus(dto.getStatus());
        }

        return mapStudentToResponseDto(studentRepository.save(student));
    }

    // Retorna los datos de un estudiante por Id
    @Transactional(readOnly = true)
    public StudentResponseDto getStudentById(Long id) {
        StudentEntity student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado."));
        return mapStudentToResponseDto(student);
    }

    // Retorna datos de un estudiante por run
    @Transactional(readOnly = true)
    public StudentResponseDto getStudentByRun(String run) {
        StudentEntity student = studentRepository.findByRun(run)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado."));
        return mapStudentToResponseDto(student);
    }

    // Retorna datos de un estudiante por su correo
    @Transactional(readOnly = true)
    public StudentResponseDto getStudentByEmail(String email) {
        StudentEntity student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado."));
        return mapStudentToResponseDto(student);
    }

    // Retorna toda la lista de estudiantes (echa por si sirve en algo)
    // CONSULTA PESADA
    @Transactional(readOnly = true)
    public List<StudentResponseDto> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::mapStudentToResponseDto)
                .collect(Collectors.toList());
    }

    // Funciones auxiliares para pasar a dto.
    private StudentResponseDto mapStudentToResponseDto(StudentEntity student) {
        return StudentResponseDto.builder()
                .id(student.getId())
                .run(student.getRun())
                .fullName(student.getFullName())
                .email(student.getEmail())
                .careerCode(student.getCareerCode())
                .studyPlanCode(student.getStudyPlanCode())
                .status(student.getStatus())
                .build();
    }
}