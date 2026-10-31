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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private AcademicPeriodRepository periodRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private StudentEntity testStudent;
    private AcademicPeriodEntity testPeriod;
    private EnrollmentCreateDto createDto;

    @BeforeEach
    void setUp() {
        testStudent = StudentEntity.builder()
                .run("12345678-9")
                .fullName("Juan Perez")
                .email("juan.perez@ipt.cl")
                .careerCode("TAP")
                .studyPlanCode("TAP-2024")
                .status(StudentStatus.REGULAR)
                .build();

        testPeriod = AcademicPeriodEntity.builder()
                .id(1L)
                .periodCode("2027-1")
                .academicPeriod("2027-1")
                .status(PeriodStatus.OPEN)
                .build();

        createDto = EnrollmentCreateDto.builder()
                .studentRun("12345678-9")
                .periodCode("2027-1")
                .build();
    }

    @Test
    @DisplayName("Debe matricular exitosamente a un estudiante en estado REGULAR y período OPEN")
    void testEnrollStudent_Success() {
        when(studentRepository.findByRun("12345678-9")).thenReturn(Optional.of(testStudent));
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(testPeriod));
        when(enrollmentRepository.existsByStudentRunAndPeriodCode("12345678-9", "2027-1")).thenReturn(false);

        EnrollmentEntity savedEntity = EnrollmentEntity.builder()
                .id(10L)
                .studentRun("12345678-9")
                .periodCode("2027-1")
                .enrollmentDate(LocalDate.now())
                .status(EnrollmentStatus.ACTIVE)
                .build();
        when(enrollmentRepository.save(any(EnrollmentEntity.class))).thenReturn(savedEntity);

        EnrollmentResponseDto result = enrollmentService.enrollStudent(createDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getStudentRun()).isEqualTo("12345678-9");
        assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        verify(enrollmentRepository).save(any(EnrollmentEntity.class));
    }

    @Test
    @DisplayName("Debe fallar al matricular si el estudiante no existe")
    void testEnrollStudent_StudentNotFound() {
        when(studentRepository.findByRun("12345678-9")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.enrollStudent(createDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Estudiante no encontrado");
    }

    @Test
    @DisplayName("Debe fallar si el estudiante no está en estado REGULAR")
    void testEnrollStudent_StudentNotRegular() {
        testStudent.setStatus(StudentStatus.TEMPORARY_WITHDRAWAL);
        when(studentRepository.findByRun("12345678-9")).thenReturn(Optional.of(testStudent));

        assertThatThrownBy(() -> enrollmentService.enrollStudent(createDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solo los estudiantes en estado REGULAR");
    }

    @Test
    @DisplayName("Debe fallar si el período académico no existe")
    void testEnrollStudent_PeriodNotFound() {
        when(studentRepository.findByRun("12345678-9")).thenReturn(Optional.of(testStudent));
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.enrollStudent(createDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El período académico '2027-1' no existe");
    }

    @Test
    @DisplayName("Debe fallar si el período académico está CLOSED")
    void testEnrollStudent_PeriodClosed() {
        testPeriod.setStatus(PeriodStatus.CLOSED);
        when(studentRepository.findByRun("12345678-9")).thenReturn(Optional.of(testStudent));
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(testPeriod));

        assertThatThrownBy(() -> enrollmentService.enrollStudent(createDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solo se pueden realizar matrículas en períodos académicos con estado ABIERTO");
    }

    @Test
    @DisplayName("Debe fallar si el estudiante ya está matriculado en el período")
    void testEnrollStudent_AlreadyEnrolled() {
        when(studentRepository.findByRun("12345678-9")).thenReturn(Optional.of(testStudent));
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(testPeriod));
        when(enrollmentRepository.existsByStudentRunAndPeriodCode("12345678-9", "2027-1")).thenReturn(true);

        assertThatThrownBy(() -> enrollmentService.enrollStudent(createDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El estudiante ya se encuentra matriculado");
    }

    @Test
    @DisplayName("Debe obtener matrículas por período")
    void testGetEnrollmentsByPeriod() {
        EnrollmentEntity e = EnrollmentEntity.builder()
                .id(1L)
                .studentRun("12345678-9")
                .periodCode("2027-1")
                .enrollmentDate(LocalDate.now())
                .status(EnrollmentStatus.ACTIVE)
                .build();
        when(enrollmentRepository.findByPeriodCode("2027-1")).thenReturn(List.of(e));

        List<EnrollmentResponseDto> list = enrollmentService.getEnrollmentsByPeriod("2027-1");
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getStudentRun()).isEqualTo("12345678-9");
    }

    @Test
    @DisplayName("Debe obtener matrículas por RUN de estudiante")
    void testGetEnrollmentsByStudent() {
        EnrollmentEntity e = EnrollmentEntity.builder()
                .id(1L)
                .studentRun("12345678-9")
                .periodCode("2027-1")
                .enrollmentDate(LocalDate.now())
                .status(EnrollmentStatus.ACTIVE)
                .build();
        when(enrollmentRepository.findByStudentRun("12345678-9")).thenReturn(List.of(e));

        List<EnrollmentResponseDto> list = enrollmentService.getEnrollmentsByStudent("12345678-9");
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getPeriodCode()).isEqualTo("2027-1");
    }

    @Test
    @DisplayName("Debe eliminar una matrícula existente")
    void testDeleteEnrollment_Success() {
        when(enrollmentRepository.existsById(10L)).thenReturn(true);

        enrollmentService.deleteEnrollment(10L);

        verify(enrollmentRepository).deleteById(10L);
    }

    @Test
    @DisplayName("Debe fallar al eliminar una matrícula inexistente")
    void testDeleteEnrollment_NotFound() {
        when(enrollmentRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> enrollmentService.deleteEnrollment(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Matrícula no encontrada");

        verify(enrollmentRepository, never()).deleteById(anyLong());
    }
}
