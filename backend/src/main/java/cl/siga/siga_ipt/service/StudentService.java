package cl.siga.siga_ipt.service;


import cl.siga.siga_ipt.dto.StudentCreateDto;
import cl.siga.siga_ipt.dto.StudentResponseDto;
import cl.siga.siga_ipt.entity.CareerEntity;
import cl.siga.siga_ipt.entity.StudentEntity;
import cl.siga.siga_ipt.entity.StudyPlanEntity;
import cl.siga.siga_ipt.entity.UserEntity;
import cl.siga.siga_ipt.enums.CareerStatus;
import cl.siga.siga_ipt.enums.Role;
import cl.siga.siga_ipt.enums.StudentStatus;
import cl.siga.siga_ipt.enums.StudyPlanStatus;
import cl.siga.siga_ipt.repository.StudentRepository;
import cl.siga.siga_ipt.repository.UserRepository;
import cl.siga.siga_ipt.repository.CareerRepository;
import cl.siga.siga_ipt.repository.StudyPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final CareerRepository careerRepository;
    private final StudyPlanRepository studyPlanRepository;

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

    /* Funciones por implementar a futuro (planificando)
    @Override
    @Transactional
    public StudentResponseDto updateStudent(Long id, StudentUpdateDto dto) {}

    @Override
    @Transactional(readOnly = true)
    public StudentResponseDto getStudentById(Long id) {}

    @Override
    @Transactional(readOnly = true)
    public StudentResponseDto getStudentByRun(String run) {}

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponseDto> getAllStudents() {}
    */

    // Funciones auxiliares para pasar a dto
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