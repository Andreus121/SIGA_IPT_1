package cl.siga.siga_ipt.service;

import cl.siga.siga_ipt.dto.RegistrationCreateDto;
import cl.siga.siga_ipt.dto.RegistrationResponseDto;
import cl.siga.siga_ipt.entity.*;
import cl.siga.siga_ipt.enums.*;
import cl.siga.siga_ipt.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private RegistrationRepository registrationRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private AcademicPeriodRepository periodRepository;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private SectionScheduleRepository scheduleRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private PrerequisiteRepository prerequisiteRepository;
    @Mock
    private AcademicHistoryRepository academicHistoryRepository;

    @InjectMocks
    private RegistrationService registrationService;

    private AcademicPeriodEntity testPeriod;
    private StudentEntity testStudent;
    private EnrollmentEntity testEnrollment;
    private SectionEntity testSection;
    private CourseEntity testCourse;
    private RegistrationCreateDto createDto;

    @BeforeEach
    void setUp() {
        testPeriod = AcademicPeriodEntity.builder()
                .id(1L)
                .periodCode("2027-1")
                .academicPeriod("2027-1")
                .status(PeriodStatus.OPEN)
                .build();

        testStudent = StudentEntity.builder()
                .run("12345678-9")
                .fullName("Juan Perez")
                .studyPlanCode("TAP-2024")
                .careerCode("TAP")
                .status(StudentStatus.REGULAR)
                .build();

        testEnrollment = EnrollmentEntity.builder()
                .id(1L)
                .studentRun("12345678-9")
                .periodCode("2027-1")
                .status(EnrollmentStatus.ACTIVE)
                .build();

        testSection = SectionEntity.builder()
                .code("SEC-PROG1-2027-1")
                .courseCode("PROG1")
                .periodCode("2027-1")
                .teacherId(1L)
                .capacity(30)
                .build();

        testCourse = CourseEntity.builder()
                .code("PROG1")
                .studyPlanCode("TAP-2024")
                .name("Programacion 1")
                .semester(1)
                .sct(4)
                .theoryHours(2)
                .exerciseHours(2)
                .labHours(0)
                .build();

        createDto = RegistrationCreateDto.builder()
                .studentRun("12345678-9")
                .periodCode("2027-1")
                .sectionCode("SEC-PROG1-2027-1")
                .build();
    }

    @Test
    @DisplayName("Inscripción exitosa cumpliendo todos los requisitos")
    void testRegisterCourse_Success() {
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(testPeriod));
        when(studentRepository.findByRun("12345678-9")).thenReturn(Optional.of(testStudent));
        when(enrollmentRepository.findByStudentRunAndPeriodCode("12345678-9", "2027-1")).thenReturn(Optional.of(testEnrollment));
        when(sectionRepository.findByCode("SEC-PROG1-2027-1")).thenReturn(Optional.of(testSection));
        when(courseRepository.findByCodeAndStudyPlanCode("PROG1", "TAP-2024")).thenReturn(Optional.of(testCourse));
        when(registrationRepository.existsByStudentRunAndCourseCodeAndPeriodCode("12345678-9", "PROG1", "2027-1")).thenReturn(false);
        when(academicHistoryRepository.existsByStudentRunAndCourseCodeAndStatus("12345678-9", "PROG1", AcademicStatus.PASSED)).thenReturn(false);
        when(prerequisiteRepository.findByCourseCodeAndStudyPlanCode("PROG1", "TAP-2024")).thenReturn(Collections.emptyList());
        when(registrationRepository.countByStudentRunAndPeriodCode("12345678-9", "2027-1")).thenReturn(2L);
        when(courseRepository.findByStudyPlanCode("TAP-2024")).thenReturn(List.of(testCourse));
        when(registrationRepository.countBySectionCodeAndPeriodCode("SEC-PROG1-2027-1", "2027-1")).thenReturn(10L);
        when(scheduleRepository.findBySectionCodeAndPeriodCode("SEC-PROG1-2027-1", "2027-1")).thenReturn(Collections.emptyList());
        when(registrationRepository.findByStudentRunAndPeriodCode("12345678-9", "2027-1")).thenReturn(Collections.emptyList());

        RegistrationEntity savedEntity = RegistrationEntity.builder()
                .id(100L)
                .studentRun("12345678-9")
                .courseCode("PROG1")
                .sectionCode("SEC-PROG1-2027-1")
                .periodCode("2027-1")
                .registrationDate(LocalDateTime.now())
                .status(RegistrationStatus.REGISTERED)
                .build();
        when(registrationRepository.save(any(RegistrationEntity.class))).thenReturn(savedEntity);

        RegistrationResponseDto result = registrationService.registerCourse(createDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getCourseCode()).isEqualTo("PROG1");
        assertThat(result.getStatus()).isEqualTo(RegistrationStatus.REGISTERED);
    }

    @Test
    @DisplayName("Falla si el período no está OPEN")
    void testRegisterCourse_PeriodClosed() {
        testPeriod.setStatus(PeriodStatus.CLOSED);
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(testPeriod));

        assertThatThrownBy(() -> registrationService.registerCourse(createDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("período académico ABIERTO");
    }

    @Test
    @DisplayName("Falla si el estudiante no está REGULAR")
    void testRegisterCourse_StudentNotRegular() {
        testStudent.setStatus(StudentStatus.POSTPONEMENT);
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(testPeriod));
        when(studentRepository.findByRun("12345678-9")).thenReturn(Optional.of(testStudent));

        assertThatThrownBy(() -> registrationService.registerCourse(createDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("estado REGULAR");
    }

    @Test
    @DisplayName("Falla si el estudiante no tiene matrícula activa")
    void testRegisterCourse_NotEnrolled() {
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(testPeriod));
        when(studentRepository.findByRun("12345678-9")).thenReturn(Optional.of(testStudent));
        when(enrollmentRepository.findByStudentRunAndPeriodCode("12345678-9", "2027-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationService.registerCourse(createDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("debe estar matriculado");
    }

    @Test
    @DisplayName("Falla si la sección no corresponde al período")
    void testRegisterCourse_SectionPeriodMismatch() {
        testSection.setPeriodCode("2026-2");
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(testPeriod));
        when(studentRepository.findByRun("12345678-9")).thenReturn(Optional.of(testStudent));
        when(enrollmentRepository.findByStudentRunAndPeriodCode("12345678-9", "2027-1")).thenReturn(Optional.of(testEnrollment));
        when(sectionRepository.findByCode("SEC-PROG1-2027-1")).thenReturn(Optional.of(testSection));

        assertThatThrownBy(() -> registrationService.registerCourse(createDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no corresponde al período académico indicado");
    }

    @Test
    @DisplayName("Falla si la asignatura ya está aprobada en historial académico")
    void testRegisterCourse_AlreadyPassed() {
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(testPeriod));
        when(studentRepository.findByRun("12345678-9")).thenReturn(Optional.of(testStudent));
        when(enrollmentRepository.findByStudentRunAndPeriodCode("12345678-9", "2027-1")).thenReturn(Optional.of(testEnrollment));
        when(sectionRepository.findByCode("SEC-PROG1-2027-1")).thenReturn(Optional.of(testSection));
        when(courseRepository.findByCodeAndStudyPlanCode("PROG1", "TAP-2024")).thenReturn(Optional.of(testCourse));
        when(registrationRepository.existsByStudentRunAndCourseCodeAndPeriodCode("12345678-9", "PROG1", "2027-1")).thenReturn(false);
        when(academicHistoryRepository.existsByStudentRunAndCourseCodeAndStatus("12345678-9", "PROG1", AcademicStatus.PASSED)).thenReturn(true);

        assertThatThrownBy(() -> registrationService.registerCourse(createDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya fue aprobada");
    }

    @Test
    @DisplayName("Falla si no cumple prerrequisito")
    void testRegisterCourse_MissingPrerequisite() {
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(testPeriod));
        when(studentRepository.findByRun("12345678-9")).thenReturn(Optional.of(testStudent));
        when(enrollmentRepository.findByStudentRunAndPeriodCode("12345678-9", "2027-1")).thenReturn(Optional.of(testEnrollment));
        when(sectionRepository.findByCode("SEC-PROG1-2027-1")).thenReturn(Optional.of(testSection));
        when(courseRepository.findByCodeAndStudyPlanCode("PROG1", "TAP-2024")).thenReturn(Optional.of(testCourse));
        when(registrationRepository.existsByStudentRunAndCourseCodeAndPeriodCode("12345678-9", "PROG1", "2027-1")).thenReturn(false);
        when(academicHistoryRepository.existsByStudentRunAndCourseCodeAndStatus("12345678-9", "PROG1", AcademicStatus.PASSED)).thenReturn(false);

        PrerequisiteEntity prereq = PrerequisiteEntity.builder()
                .courseCode("PROG1")
                .studyPlanCode("TAP-2024")
                .prerequisiteCourseCode("MAT01")
                .build();
        when(prerequisiteRepository.findByCourseCodeAndStudyPlanCode("PROG1", "TAP-2024")).thenReturn(List.of(prereq));
        when(academicHistoryRepository.existsByStudentRunAndCourseCodeAndStatus("12345678-9", "MAT01", AcademicStatus.PASSED)).thenReturn(false);

        assertThatThrownBy(() -> registrationService.registerCourse(createDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No cumple con el prerrequisito aprobado: MAT01");
    }

    @Test
    @DisplayName("Falla si supera el límite de 6 asignaturas")
    void testRegisterCourse_MaxCoursesExceeded() {
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(testPeriod));
        when(studentRepository.findByRun("12345678-9")).thenReturn(Optional.of(testStudent));
        when(enrollmentRepository.findByStudentRunAndPeriodCode("12345678-9", "2027-1")).thenReturn(Optional.of(testEnrollment));
        when(sectionRepository.findByCode("SEC-PROG1-2027-1")).thenReturn(Optional.of(testSection));
        when(courseRepository.findByCodeAndStudyPlanCode("PROG1", "TAP-2024")).thenReturn(Optional.of(testCourse));
        when(registrationRepository.existsByStudentRunAndCourseCodeAndPeriodCode("12345678-9", "PROG1", "2027-1")).thenReturn(false);
        when(academicHistoryRepository.existsByStudentRunAndCourseCodeAndStatus("12345678-9", "PROG1", AcademicStatus.PASSED)).thenReturn(false);
        when(prerequisiteRepository.findByCourseCodeAndStudyPlanCode("PROG1", "TAP-2024")).thenReturn(Collections.emptyList());
        when(registrationRepository.countByStudentRunAndPeriodCode("12345678-9", "2027-1")).thenReturn(6L);

        assertThatThrownBy(() -> registrationService.registerCourse(createDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("límite máximo de 6 asignaturas");
    }

    @Test
    @DisplayName("Falla si no respeta la prioridad de semestre inferior disponible y ofertado")
    void testRegisterCourse_LowerSemesterPriorityViolation() {
        testCourse.setSemester(2); // curso destino semestre 2
        CourseEntity lowerCourse = CourseEntity.builder()
                .code("MAT01")
                .studyPlanCode("TAP-2024")
                .semester(1)
                .build();

        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(testPeriod));
        when(studentRepository.findByRun("12345678-9")).thenReturn(Optional.of(testStudent));
        when(enrollmentRepository.findByStudentRunAndPeriodCode("12345678-9", "2027-1")).thenReturn(Optional.of(testEnrollment));
        when(sectionRepository.findByCode("SEC-PROG1-2027-1")).thenReturn(Optional.of(testSection));
        when(courseRepository.findByCodeAndStudyPlanCode("PROG1", "TAP-2024")).thenReturn(Optional.of(testCourse));
        when(registrationRepository.existsByStudentRunAndCourseCodeAndPeriodCode("12345678-9", "PROG1", "2027-1")).thenReturn(false);
        when(academicHistoryRepository.existsByStudentRunAndCourseCodeAndStatus("12345678-9", "PROG1", AcademicStatus.PASSED)).thenReturn(false);
        when(prerequisiteRepository.findByCourseCodeAndStudyPlanCode("PROG1", "TAP-2024")).thenReturn(Collections.emptyList());
        when(registrationRepository.countByStudentRunAndPeriodCode("12345678-9", "2027-1")).thenReturn(1L);

        when(courseRepository.findByStudyPlanCode("TAP-2024")).thenReturn(List.of(lowerCourse, testCourse));
        when(academicHistoryRepository.existsByStudentRunAndCourseCodeAndStatus("12345678-9", "MAT01", AcademicStatus.PASSED)).thenReturn(false);
        when(registrationRepository.existsByStudentRunAndCourseCodeAndPeriodCode("12345678-9", "MAT01", "2027-1")).thenReturn(false);
        when(sectionRepository.existsByCourseCodeAndPeriodCode("MAT01", "2027-1")).thenReturn(true);
        when(prerequisiteRepository.findByCourseCodeAndStudyPlanCode("MAT01", "TAP-2024")).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> registrationService.registerCourse(createDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No puede inscribir asignaturas del semestre 2 teniendo pendiente y disponible 'MAT01'");
    }

    @Test
    @DisplayName("Falla si no hay cupos en la sección")
    void testRegisterCourse_SectionFull() {
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(testPeriod));
        when(studentRepository.findByRun("12345678-9")).thenReturn(Optional.of(testStudent));
        when(enrollmentRepository.findByStudentRunAndPeriodCode("12345678-9", "2027-1")).thenReturn(Optional.of(testEnrollment));
        when(sectionRepository.findByCode("SEC-PROG1-2027-1")).thenReturn(Optional.of(testSection));
        when(courseRepository.findByCodeAndStudyPlanCode("PROG1", "TAP-2024")).thenReturn(Optional.of(testCourse));
        when(registrationRepository.existsByStudentRunAndCourseCodeAndPeriodCode("12345678-9", "PROG1", "2027-1")).thenReturn(false);
        when(academicHistoryRepository.existsByStudentRunAndCourseCodeAndStatus("12345678-9", "PROG1", AcademicStatus.PASSED)).thenReturn(false);
        when(prerequisiteRepository.findByCourseCodeAndStudyPlanCode("PROG1", "TAP-2024")).thenReturn(Collections.emptyList());
        when(registrationRepository.countByStudentRunAndPeriodCode("12345678-9", "2027-1")).thenReturn(1L);
        when(courseRepository.findByStudyPlanCode("TAP-2024")).thenReturn(List.of(testCourse));
        when(registrationRepository.countBySectionCodeAndPeriodCode("SEC-PROG1-2027-1", "2027-1")).thenReturn(30L); // Capacidad 30

        assertThatThrownBy(() -> registrationService.registerCourse(createDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no tiene cupos disponibles");
    }

    @Test
    @DisplayName("Falla si existe tope de horario con otra asignatura inscrita")
    void testRegisterCourse_ScheduleConflict() {
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(testPeriod));
        when(studentRepository.findByRun("12345678-9")).thenReturn(Optional.of(testStudent));
        when(enrollmentRepository.findByStudentRunAndPeriodCode("12345678-9", "2027-1")).thenReturn(Optional.of(testEnrollment));
        when(sectionRepository.findByCode("SEC-PROG1-2027-1")).thenReturn(Optional.of(testSection));
        when(courseRepository.findByCodeAndStudyPlanCode("PROG1", "TAP-2024")).thenReturn(Optional.of(testCourse));
        when(registrationRepository.existsByStudentRunAndCourseCodeAndPeriodCode("12345678-9", "PROG1", "2027-1")).thenReturn(false);
        when(academicHistoryRepository.existsByStudentRunAndCourseCodeAndStatus("12345678-9", "PROG1", AcademicStatus.PASSED)).thenReturn(false);
        when(prerequisiteRepository.findByCourseCodeAndStudyPlanCode("PROG1", "TAP-2024")).thenReturn(Collections.emptyList());
        when(registrationRepository.countByStudentRunAndPeriodCode("12345678-9", "2027-1")).thenReturn(1L);
        when(courseRepository.findByStudyPlanCode("TAP-2024")).thenReturn(List.of(testCourse));
        when(registrationRepository.countBySectionCodeAndPeriodCode("SEC-PROG1-2027-1", "2027-1")).thenReturn(5L);

        SectionScheduleEntity newSlot = SectionScheduleEntity.builder()
                .sectionCode("SEC-PROG1-2027-1")
                .periodCode("2027-1")
                .day("L")
                .moduleNumber(1)
                .build();
        when(scheduleRepository.findBySectionCodeAndPeriodCode("SEC-PROG1-2027-1", "2027-1")).thenReturn(List.of(newSlot));

        RegistrationEntity existingReg = RegistrationEntity.builder()
                .sectionCode("SEC-BD1-2027-1")
                .periodCode("2027-1")
                .build();
        when(registrationRepository.findByStudentRunAndPeriodCode("12345678-9", "2027-1")).thenReturn(List.of(existingReg));

        SectionScheduleEntity existingSlot = SectionScheduleEntity.builder()
                .sectionCode("SEC-BD1-2027-1")
                .periodCode("2027-1")
                .day("L")
                .moduleNumber(1)
                .build();
        when(scheduleRepository.findBySectionCodeAndPeriodCode("SEC-BD1-2027-1", "2027-1")).thenReturn(List.of(existingSlot));

        assertThatThrownBy(() -> registrationService.registerCourse(createDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tope de horario detectado");
    }

    @Test
    @DisplayName("Debe anular inscripción correctamente si período está OPEN")
    void testDropCourse_Success() {
        RegistrationEntity reg = RegistrationEntity.builder()
                .id(50L)
                .periodCode("2027-1")
                .build();
        when(registrationRepository.findById(50L)).thenReturn(Optional.of(reg));
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(testPeriod));

        registrationService.dropCourse(50L);

        verify(registrationRepository).delete(reg);
    }

    @Test
    @DisplayName("Falla al anular inscripción si el período está CLOSED")
    void testDropCourse_PeriodClosed() {
        testPeriod.setStatus(PeriodStatus.CLOSED);
        RegistrationEntity reg = RegistrationEntity.builder()
                .id(50L)
                .periodCode("2027-1")
                .build();
        when(registrationRepository.findById(50L)).thenReturn(Optional.of(reg));
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(testPeriod));

        assertThatThrownBy(() -> registrationService.dropCourse(50L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mientras el período académico se encuentre ABIERTO");
    }

    @Test
    @DisplayName("Debe listar inscripciones por estudiante y por sección")
    void testGetRegistrations() {
        RegistrationEntity reg = RegistrationEntity.builder()
                .id(1L)
                .studentRun("12345678-9")
                .sectionCode("SEC-PROG1-2027-1")
                .courseCode("PROG1")
                .periodCode("2027-1")
                .registrationDate(LocalDateTime.now())
                .status(RegistrationStatus.REGISTERED)
                .build();

        when(registrationRepository.findByStudentRunAndPeriodCode("12345678-9", "2027-1")).thenReturn(List.of(reg));
        when(registrationRepository.findBySectionCodeAndPeriodCode("SEC-PROG1-2027-1", "2027-1")).thenReturn(List.of(reg));

        List<RegistrationResponseDto> byStudent = registrationService.getRegistrationsByStudent("12345678-9", "2027-1");
        List<RegistrationResponseDto> bySection = registrationService.getRegistrationsBySection("SEC-PROG1-2027-1", "2027-1");

        assertThat(byStudent).hasSize(1);
        assertThat(bySection).hasSize(1);
    }
}
