package cl.siga.siga_ipt.service;

import cl.siga.siga_ipt.dto.StudentCreateDto;
import cl.siga.siga_ipt.dto.StudentResponseDto;
import cl.siga.siga_ipt.dto.StudentUpdateDto;
import cl.siga.siga_ipt.entity.CareerEntity;
import cl.siga.siga_ipt.entity.StudentEntity;
import cl.siga.siga_ipt.entity.StudyPlanEntity;
import cl.siga.siga_ipt.enums.CareerStatus;
import cl.siga.siga_ipt.enums.StudentStatus;
import cl.siga.siga_ipt.enums.StudyPlanStatus;
import cl.siga.siga_ipt.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CareerRepository careerRepository;

    @Mock
    private StudyPlanRepository studyPlanRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private AcademicHistoryRepository academicHistoryRepository;

    @InjectMocks
    private StudentService studentService;

    private StudentCreateDto buildValidStudentCreateDto() {
        StudentCreateDto dto = new StudentCreateDto();
        dto.setRun("12345678-9");
        dto.setFullName("Juan Pérez");
        dto.setEmail("juan.perez@alumnos.ipt.cl");
        dto.setCareerCode("TAP");
        return dto;
    }

    @Test
    @DisplayName("Should create student successfully with REGULAR status and CURRENT study plan")
    void createStudent_Success() {
        StudentCreateDto dto = buildValidStudentCreateDto();

        CareerEntity career = CareerEntity.builder()
                .code("TAP")
                .status(CareerStatus.ACTIVE)
                .build();

        StudyPlanEntity plan = StudyPlanEntity.builder()
                .code("PLAN-TAP-2024")
                .careerCode("TAP")
                .status(StudyPlanStatus.CURRENT)
                .build();

        when(studentRepository.existsByRun("12345678-9")).thenReturn(false);
        when(studentRepository.existsByEmail("juan.perez@alumnos.ipt.cl")).thenReturn(false);
        when(careerRepository.findByCode("TAP")).thenReturn(Optional.of(career));
        when(studyPlanRepository.findByCareerCodeAndStatus("TAP", StudyPlanStatus.CURRENT)).thenReturn(Optional.of(plan));
        when(studentRepository.save(any(StudentEntity.class))).thenAnswer(i -> i.getArgument(0));

        StudentResponseDto response = studentService.createStudent(dto);

        assertThat(response).isNotNull();
        assertThat(response.getRun()).isEqualTo("12345678-9");
        assertThat(response.getStatus()).isEqualTo(StudentStatus.REGULAR);
        assertThat(response.getStudyPlanCode()).isEqualTo("PLAN-TAP-2024");
        verify(studentRepository).save(any(StudentEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when RUN already exists")
    void createStudent_DuplicateRun_ThrowsException() {
        StudentCreateDto dto = buildValidStudentCreateDto();
        when(studentRepository.existsByRun("12345678-9")).thenReturn(true);

        assertThatThrownBy(() -> studentService.createStudent(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El RUN ya se encuentra registrado");
    }

    @Test
    @DisplayName("Should throw exception when Email already exists")
    void createStudent_DuplicateEmail_ThrowsException() {
        StudentCreateDto dto = buildValidStudentCreateDto();
        when(studentRepository.existsByRun("12345678-9")).thenReturn(false);
        when(studentRepository.existsByEmail("juan.perez@alumnos.ipt.cl")).thenReturn(true);

        assertThatThrownBy(() -> studentService.createStudent(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El correo electrónico ya se encuentra registrado");
    }

    @Test
    @DisplayName("Should throw exception when career does not exist")
    void createStudent_CareerNotFound_ThrowsException() {
        StudentCreateDto dto = buildValidStudentCreateDto();
        when(studentRepository.existsByRun("12345678-9")).thenReturn(false);
        when(studentRepository.existsByEmail("juan.perez@alumnos.ipt.cl")).thenReturn(false);
        when(careerRepository.findByCode("TAP")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.createStudent(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La carrera especificada no existe");
    }

    @Test
    @DisplayName("Should throw exception when career is INACTIVE")
    void createStudent_CareerInactive_ThrowsException() {
        StudentCreateDto dto = buildValidStudentCreateDto();
        CareerEntity inactiveCareer = CareerEntity.builder().code("TAP").status(CareerStatus.INACTIVE).build();

        when(studentRepository.existsByRun("12345678-9")).thenReturn(false);
        when(studentRepository.existsByEmail("juan.perez@alumnos.ipt.cl")).thenReturn(false);
        when(careerRepository.findByCode("TAP")).thenReturn(Optional.of(inactiveCareer));

        assertThatThrownBy(() -> studentService.createStudent(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se pueden registrar estudiantes en una carrera INACTIVA");
    }

    @Test
    @DisplayName("Should throw exception when career has no CURRENT study plan")
    void createStudent_NoCurrentStudyPlan_ThrowsException() {
        StudentCreateDto dto = buildValidStudentCreateDto();
        CareerEntity career = CareerEntity.builder().code("TAP").status(CareerStatus.ACTIVE).build();

        when(studentRepository.existsByRun("12345678-9")).thenReturn(false);
        when(studentRepository.existsByEmail("juan.perez@alumnos.ipt.cl")).thenReturn(false);
        when(careerRepository.findByCode("TAP")).thenReturn(Optional.of(career));
        when(studyPlanRepository.findByCareerCodeAndStatus("TAP", StudyPlanStatus.CURRENT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.createStudent(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("La carrera no tiene un plan de estudios vigente");
    }

    @Test
    @DisplayName("Should update student basic info successfully")
    void updateStudent_BasicInfo_Success() {
        StudentEntity student = StudentEntity.builder()
                .id(1L)
                .run("12345678-9")
                .fullName("Juan Pérez")
                .email("juan@alumnos.ipt.cl")
                .careerCode("TAP")
                .studyPlanCode("PLAN-1")
                .status(StudentStatus.REGULAR)
                .build();

        StudentUpdateDto updateDto = new StudentUpdateDto();
        updateDto.setFullName("Juan Alberto Pérez");
        updateDto.setEmail("juan.nuevo@alumnos.ipt.cl");
        updateDto.setStatus(StudentStatus.POSTPONEMENT);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.existsByEmail("juan.nuevo@alumnos.ipt.cl")).thenReturn(false);
        when(studentRepository.save(any(StudentEntity.class))).thenAnswer(i -> i.getArgument(0));

        StudentResponseDto response = studentService.updateStudent(1L, updateDto);

        assertThat(response.getFullName()).isEqualTo("Juan Alberto Pérez");
        assertThat(response.getEmail()).isEqualTo("juan.nuevo@alumnos.ipt.cl");
        assertThat(response.getStatus()).isEqualTo(StudentStatus.POSTPONEMENT);
    }

    @Test
    @DisplayName("Should switch career if student has NO registrations and NO academic history")
    void updateStudent_ChangeCareer_Success() {
        StudentEntity student = StudentEntity.builder()
                .id(1L)
                .run("12345678-9")
                .careerCode("TAP")
                .studyPlanCode("PLAN-TAP")
                .build();

        StudentUpdateDto updateDto = new StudentUpdateDto();
        updateDto.setCareerCode("TCS");

        CareerEntity newCareer = CareerEntity.builder().code("TCS").status(CareerStatus.ACTIVE).build();
        StudyPlanEntity newPlan = StudyPlanEntity.builder().code("PLAN-TCS-2024").status(StudyPlanStatus.CURRENT).build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(registrationRepository.existsByStudentRun("12345678-9")).thenReturn(false);
        when(academicHistoryRepository.existsByStudentRun("12345678-9")).thenReturn(false);
        when(careerRepository.findByCode("TCS")).thenReturn(Optional.of(newCareer));
        when(studyPlanRepository.findByCareerCodeAndStatus("TCS", StudyPlanStatus.CURRENT)).thenReturn(Optional.of(newPlan));
        when(studentRepository.save(any(StudentEntity.class))).thenAnswer(i -> i.getArgument(0));

        StudentResponseDto response = studentService.updateStudent(1L, updateDto);

        assertThat(response.getCareerCode()).isEqualTo("TCS");
        assertThat(response.getStudyPlanCode()).isEqualTo("PLAN-TCS-2024");
    }

    @Test
    @DisplayName("Should throw exception when changing career with existing registrations")
    void updateStudent_ChangeCareer_WithRegistrations_ThrowsException() {
        StudentEntity student = StudentEntity.builder().id(1L).run("12345678-9").careerCode("TAP").build();

        StudentUpdateDto updateDto = new StudentUpdateDto();
        updateDto.setCareerCode("TCS");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(registrationRepository.existsByStudentRun("12345678-9")).thenReturn(true);

        assertThatThrownBy(() -> studentService.updateStudent(1L, updateDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se puede cambiar la carrera de un estudiante que posee inscripciones");
    }

    @Test
    @DisplayName("Should throw exception when assigning GRADUATED or DROPPED status manually")
    void updateStudent_ManualGraduatedOrDropped_ThrowsException() {
        StudentEntity student = StudentEntity.builder().id(1L).run("12345678-9").status(StudentStatus.REGULAR).build();

        StudentUpdateDto updateDto = new StudentUpdateDto();
        updateDto.setStatus(StudentStatus.GRADUATED);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> studentService.updateStudent(1L, updateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("EGRESADO y ELIMINADO son gestionados automáticamente");
    }

    @Test
    @DisplayName("Should delete student when no registrations and no history exist")
    void deleteStudent_Success() {
        StudentEntity student = StudentEntity.builder().id(1L).run("12345678-9").build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(registrationRepository.existsByStudentRun("12345678-9")).thenReturn(false);
        when(academicHistoryRepository.existsByStudentRun("12345678-9")).thenReturn(false);

        studentService.deleteStudent(1L);

        verify(studentRepository).delete(student);
    }

    @Test
    @DisplayName("Should throw exception when deleting student with academic history")
    void deleteStudent_WithAcademicHistory_ThrowsException() {
        StudentEntity student = StudentEntity.builder().id(1L).run("12345678-9").build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(registrationRepository.existsByStudentRun("12345678-9")).thenReturn(false);
        when(academicHistoryRepository.existsByStudentRun("12345678-9")).thenReturn(true);

        assertThatThrownBy(() -> studentService.deleteStudent(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("historial académico");

        verify(studentRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should get student by id, run and email")
    void getStudentQueries_Success() {
        StudentEntity student = StudentEntity.builder()
                .id(1L)
                .run("12345678-9")
                .email("test@ipt.cl")
                .build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.findByRun("12345678-9")).thenReturn(Optional.of(student));
        when(studentRepository.findByEmail("test@ipt.cl")).thenReturn(Optional.of(student));

        assertThat(studentService.getStudentById(1L)).isNotNull();
        assertThat(studentService.getStudentByRun("12345678-9")).isNotNull();
        assertThat(studentService.getStudentByEmail("test@ipt.cl")).isNotNull();
    }

    @Test
    @DisplayName("Should return all students")
    void getAllStudents_Success() {
        StudentEntity student = StudentEntity.builder().id(1L).build();
        when(studentRepository.findAll()).thenReturn(List.of(student));

        List<StudentResponseDto> all = studentService.getAllStudents();
        assertThat(all).hasSize(1);
    }

    @Test
    @DisplayName("Should throw exception when student not found by id, run or email")
    void getStudentQueries_NotFound_ThrowsException() {
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());
        when(studentRepository.findByRun("999")).thenReturn(Optional.empty());
        when(studentRepository.findByEmail("non@ipt.cl")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getStudentById(999L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> studentService.getStudentByRun("999"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> studentService.getStudentByEmail("non@ipt.cl"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should throw exception when deleting student with registrations")
    void deleteStudent_WithRegistrations_ThrowsException() {
        StudentEntity student = StudentEntity.builder().id(1L).run("12345678-9").build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(registrationRepository.existsByStudentRun("12345678-9")).thenReturn(true);

        assertThatThrownBy(() -> studentService.deleteStudent(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tiene inscripciones en el sistema");
    }

    @Test
    @DisplayName("Should throw exception when changing career to inactive career")
    void updateStudent_ChangeCareerToInactive_ThrowsException() {
        StudentEntity student = StudentEntity.builder().id(1L).run("12345678-9").careerCode("TAP").build();
        StudentUpdateDto updateDto = new StudentUpdateDto();
        updateDto.setCareerCode("TCS");

        CareerEntity inactiveCareer = CareerEntity.builder().code("TCS").status(CareerStatus.INACTIVE).build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(registrationRepository.existsByStudentRun("12345678-9")).thenReturn(false);
        when(academicHistoryRepository.existsByStudentRun("12345678-9")).thenReturn(false);
        when(careerRepository.findByCode("TCS")).thenReturn(Optional.of(inactiveCareer));

        assertThatThrownBy(() -> studentService.updateStudent(1L, updateDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se puede cambiar a una carrera INACTIVA");
    }

    @Test
    @DisplayName("Should throw exception when changing career and new career has no current plan")
    void updateStudent_ChangeCareerNoCurrentPlan_ThrowsException() {
        StudentEntity student = StudentEntity.builder().id(1L).run("12345678-9").careerCode("TAP").build();
        StudentUpdateDto updateDto = new StudentUpdateDto();
        updateDto.setCareerCode("TCS");

        CareerEntity activeCareer = CareerEntity.builder().code("TCS").status(CareerStatus.ACTIVE).build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(registrationRepository.existsByStudentRun("12345678-9")).thenReturn(false);
        when(academicHistoryRepository.existsByStudentRun("12345678-9")).thenReturn(false);
        when(careerRepository.findByCode("TCS")).thenReturn(Optional.of(activeCareer));
        when(studyPlanRepository.findByCareerCodeAndStatus("TCS", StudyPlanStatus.CURRENT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.updateStudent(1L, updateDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("La nueva carrera no tiene un plan vigente");
    }

    @Test
    @DisplayName("Should throw exception when updating email with duplicate")
    void updateStudent_DuplicateEmail_ThrowsException() {
        StudentEntity student = StudentEntity.builder().id(1L).run("12345678-9").email("old@ipt.cl").build();
        StudentUpdateDto updateDto = new StudentUpdateDto();
        updateDto.setEmail("taken@ipt.cl");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.existsByEmail("taken@ipt.cl")).thenReturn(true);

        assertThatThrownBy(() -> studentService.updateStudent(1L, updateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya está en uso");
    }
}
