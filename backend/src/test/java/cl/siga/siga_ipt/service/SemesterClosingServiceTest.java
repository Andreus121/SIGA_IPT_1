package cl.siga.siga_ipt.service;

import cl.siga.siga_ipt.dto.SemesterClosingResponseDto;
import cl.siga.siga_ipt.entity.*;
import cl.siga.siga_ipt.enums.AcademicStatus;
import cl.siga.siga_ipt.enums.PeriodStatus;
import cl.siga.siga_ipt.enums.RegistrationStatus;
import cl.siga.siga_ipt.enums.StudentStatus;
import cl.siga.siga_ipt.repository.*;
import org.junit.jupiter.api.BeforeEach;
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
class SemesterClosingServiceTest {

    @Mock
    private AcademicPeriodRepository periodRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private AcademicHistoryRepository academicHistoryRepository;

    @InjectMocks
    private SemesterClosingService semesterClosingService;

    private AcademicPeriodEntity period;
    private StudentEntity student;
    private SectionEntity section;
    private TeacherEntity teacher;
    private RegistrationEntity registration;

    @BeforeEach
    void setUp() {
        period = AcademicPeriodEntity.builder()
                .id(1L)
                .periodCode("2027-1")
                .status(PeriodStatus.OPEN)
                .build();

        student = StudentEntity.builder()
                .run("12345678-9")
                .fullName("Juan Perez")
                .studyPlanCode("TAP-2024")
                .careerCode("TAP")
                .status(StudentStatus.REGULAR)
                .build();

        section = SectionEntity.builder()
                .code("SEC-01")
                .courseCode("PROG1")
                .periodCode("2027-1")
                .teacherId(1L)
                .capacity(30)
                .build();

        teacher = TeacherEntity.builder()
                .id(1L)
                .run("98765432-1")
                .build();

        registration = RegistrationEntity.builder()
                .id(10L)
                .studentRun("12345678-9")
                .courseCode("PROG1")
                .sectionCode("SEC-01")
                .periodCode("2027-1")
                .finalGrade(5.0)
                .status(RegistrationStatus.REGISTERED)
                .build();
    }

    @Test
    @DisplayName("Cierre exitoso: consolida en historial, evalúa graduación y cierra período")
    void testCloseSemester_Success_WithGraduation() {
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(period));
        when(registrationRepository.findByPeriodCode("2027-1")).thenReturn(List.of(registration));
        when(sectionRepository.findByCodeAndPeriodCode("SEC-01", "2027-1")).thenReturn(Optional.of(section));
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(studentRepository.findByRun("12345678-9")).thenReturn(Optional.of(student));

        AcademicHistoryEntity passedHistory = AcademicHistoryEntity.builder()
                .studentRun("12345678-9")
                .courseCode("PROG1")
                .status(AcademicStatus.PASSED)
                .build();
        when(academicHistoryRepository.findByStudentRun("12345678-9")).thenReturn(List.of(passedHistory));

        CourseEntity singleCourse = CourseEntity.builder().code("PROG1").build();
        when(courseRepository.findByStudyPlanCode("TAP-2024")).thenReturn(List.of(singleCourse));

        SemesterClosingResponseDto response = semesterClosingService.closeSemester("2027-1");

        assertThat(response).isNotNull();
        assertThat(response.getTotalRegistrationsProcessed()).isEqualTo(1);
        assertThat(response.getStudentsGraduatedCount()).isEqualTo(1);
        assertThat(response.getStudentsDroppedCount()).isEqualTo(0);
        assertThat(period.getStatus()).isEqualTo(PeriodStatus.CLOSED);

        verify(academicHistoryRepository).save(any(AcademicHistoryEntity.class));
        verify(studentRepository).save(student);
        verify(periodRepository).save(period);
        assertThat(student.getStatus()).isEqualTo(StudentStatus.GRADUATED);
    }

    @Test
    @DisplayName("Cierre exitoso: estudiante pasa a DROPPED si reprueba la misma asignatura por segunda vez")
    void testCloseSemester_Success_WithDroppedStudent() {
        registration.setFinalGrade(3.0); // reprobado en este período

        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(period));
        when(registrationRepository.findByPeriodCode("2027-1")).thenReturn(List.of(registration));
        when(sectionRepository.findByCodeAndPeriodCode("SEC-01", "2027-1")).thenReturn(Optional.of(section));
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(studentRepository.findByRun("12345678-9")).thenReturn(Optional.of(student));

        // En el historial figuran 2 reprobaciones en la misma asignatura
        AcademicHistoryEntity failed1 = AcademicHistoryEntity.builder()
                .studentRun("12345678-9")
                .courseCode("PROG1")
                .status(AcademicStatus.FAILED)
                .build();
        AcademicHistoryEntity failed2 = AcademicHistoryEntity.builder()
                .studentRun("12345678-9")
                .courseCode("PROG1")
                .status(AcademicStatus.FAILED)
                .build();
        when(academicHistoryRepository.findByStudentRun("12345678-9")).thenReturn(List.of(failed1, failed2));

        SemesterClosingResponseDto response = semesterClosingService.closeSemester("2027-1");

        assertThat(response).isNotNull();
        assertThat(response.getStudentsDroppedCount()).isEqualTo(1);
        assertThat(response.getStudentsGraduatedCount()).isEqualTo(0);
        assertThat(student.getStatus()).isEqualTo(StudentStatus.DROPPED);
    }

    @Test
    @DisplayName("Falla el cierre si el período no existe")
    void testCloseSemester_PeriodNotFound() {
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> semesterClosingService.closeSemester("2027-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no existe");
    }

    @Test
    @DisplayName("Falla el cierre si el período no se encuentra OPEN")
    void testCloseSemester_PeriodNotOpen() {
        period.setStatus(PeriodStatus.CLOSED);
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(period));

        assertThatThrownBy(() -> semesterClosingService.closeSemester("2027-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no se encuentra ABIERTO");
    }

    @Test
    @DisplayName("Falla el cierre si existen inscripciones sin nota registrada")
    void testCloseSemester_MissingFinalGrades() {
        registration.setFinalGrade(null); // Sin nota asignada
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(period));
        when(registrationRepository.findByPeriodCode("2027-1")).thenReturn(List.of(registration));

        assertThatThrownBy(() -> semesterClosingService.closeSemester("2027-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("existen asignaturas inscritas sin calificación final");
    }
}
