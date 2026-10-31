package cl.siga.siga_ipt.integration;

import cl.siga.siga_ipt.dto.GradeEntryDto;
import cl.siga.siga_ipt.entity.*;
import cl.siga.siga_ipt.enums.*;
import cl.siga.siga_ipt.repository.*;
import cl.siga.siga_ipt.service.GradeService;
import cl.siga.siga_ipt.service.SemesterClosingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GradeClosingIntegrationTest {

    @Autowired
    private GradeService gradeService;

    @Autowired
    private SemesterClosingService semesterClosingService;

    @Autowired
    private AcademicPeriodRepository periodRepository;

    @Autowired
    private CareerRepository careerRepository;

    @Autowired
    private StudyPlanRepository studyPlanRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private AcademicHistoryRepository academicHistoryRepository;

    private AcademicPeriodEntity openPeriod;
    private StudentEntity testStudent;
    private RegistrationEntity registration;

    @BeforeEach
    void setUp() {
        academicHistoryRepository.deleteAll();
        registrationRepository.deleteAll();
        sectionRepository.deleteAll();
        courseRepository.deleteAll();
        studyPlanRepository.deleteAll();
        careerRepository.deleteAll();
        studentRepository.deleteAll();
        teacherRepository.deleteAll();
        periodRepository.deleteAll();

        // 1. Periodo OPEN
        openPeriod = periodRepository.save(AcademicPeriodEntity.builder()
                .periodCode("2027-1")
                .academicPeriod("2027-1")
                .status(PeriodStatus.OPEN)
                .build());

        // 2. Carrera y Plan
        careerRepository.save(CareerEntity.builder()
                .code("TAP")
                .name("Analista Programador")
                .description("Carrera tecnica")
                .status(CareerStatus.ACTIVE)
                .build());

        studyPlanRepository.save(StudyPlanEntity.builder()
                .code("TAP-2024")
                .careerCode("TAP")
                .name("Plan 2024")
                .status(StudyPlanStatus.CURRENT)
                .build());

        // Asignatura única del plan para prueba de graduación
        courseRepository.save(CourseEntity.builder()
                .code("PROG1")
                .studyPlanCode("TAP-2024")
                .name("Programacion 1")
                .semester(1)
                .sct(4)
                .theoryHours(2)
                .exerciseHours(0)
                .labHours(0)
                .build());

        // 3. Estudiante Regular
        testStudent = studentRepository.save(StudentEntity.builder()
                .run("12345678-9")
                .fullName("Estudiante Notas")
                .email("notas@ipt.cl")
                .careerCode("TAP")
                .studyPlanCode("TAP-2024")
                .status(StudentStatus.REGULAR)
                .build());

        // 4. Docente y Sección
        TeacherEntity teacher = teacherRepository.save(TeacherEntity.builder()
                .run("98765432-1")
                .fullName("Profesor Notas")
                .email("profe@ipt.cl")
                .professionalTitle("Ingeniero")
                .academicDegree("Magíster")
                .status(TeacherStatus.ACTIVE)
                .build());

        SectionEntity section = sectionRepository.save(SectionEntity.builder()
                .code("SEC-PROG1-2027-1")
                .courseCode("PROG1")
                .periodCode("2027-1")
                .teacherId(teacher.getId())
                .capacity(30)
                .build());

        // 5. Inscripción activa
        registration = registrationRepository.save(RegistrationEntity.builder()
                .studentRun(testStudent.getRun())
                .courseCode("PROG1")
                .sectionCode(section.getCode())
                .periodCode("2027-1")
                .registrationDate(LocalDateTime.now())
                .status(RegistrationStatus.REGISTERED)
                .build());
    }

    // ==========================================
    // ESCENARIOS EXITOSOS (3)
    // ==========================================

    @Test
    @DisplayName("Éxito 1: Docente ingresa nota final válida (5.5)")
    void testRegisterGrade_Success() throws Exception {
        GradeEntryDto dto = GradeEntryDto.builder()
                .registrationId(registration.getId())
                .finalGrade(5.5)
                .build();

        var response = gradeService.registerGrade(dto);
        assertThat(response.getFinalGrade()).isEqualTo(5.5);
    }

    @Test
    @DisplayName("Éxito 2: Cierre de semestre: estudiante aprueba su plan completo y pasa a GRADUATED")
    void testCloseSemester_StudentGraduates_Success() throws Exception {
        // Asignar nota aprobatoria final (6.0)
        registration.setFinalGrade(6.0);
        registrationRepository.save(registration);

        var response = semesterClosingService.closeSemester("2027-1");
        assertThat(response.getTotalRegistrationsProcessed()).isEqualTo(1);
        assertThat(response.getStudentsGraduatedCount()).isEqualTo(1);

        StudentEntity updated = studentRepository.findByRun(testStudent.getRun()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(StudentStatus.GRADUATED);
    }

    @Test
    @DisplayName("Éxito 3: Período académico pasa a CLOSED al finalizar el cierre de semestre")
    void testCloseSemester_PeriodClosed_Success() throws Exception {
        registration.setFinalGrade(4.5);
        registrationRepository.save(registration);

        semesterClosingService.closeSemester("2027-1");

        AcademicPeriodEntity updatedPeriod = periodRepository.findByPeriodCode("2027-1").orElseThrow();
        assertThat(updatedPeriod.getStatus()).isEqualTo(PeriodStatus.CLOSED);
    }

    // ==========================================
    // ESCENARIOS DE RECHAZO (3)
    // ==========================================

    @Test
    @DisplayName("Rechazo 1: Rechazar ingreso de calificación fuera de rango (7.5)")
    void testRegisterGrade_OutOfRange_Fails() throws Exception {
        GradeEntryDto dto = GradeEntryDto.builder()
                .registrationId(registration.getId())
                .finalGrade(7.5) // Calificación inválida > 7.0
                .build();

        assertThatThrownBy(() -> gradeService.registerGrade(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("7.0");
    }

    @Test
    @DisplayName("Rechazo 2: Rechazar ingreso o modificación de notas si el período está CLOSED")
    void testRegisterGrade_PeriodClosed_Fails() throws Exception {
        // Cerrar período
        openPeriod.setStatus(PeriodStatus.CLOSED);
        periodRepository.save(openPeriod);

        GradeEntryDto dto = GradeEntryDto.builder()
                .registrationId(registration.getId())
                .finalGrade(5.0)
                .build();

        assertThatThrownBy(() -> gradeService.registerGrade(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ABIERTO");
    }

    @Test
    @DisplayName("Rechazo 3: Rechazar cierre de semestre si existen inscripciones sin nota registrada")
    void testCloseSemester_PendingGrades_Fails() throws Exception {
        // registration.finalGrade es null
        assertThatThrownBy(() -> semesterClosingService.closeSemester("2027-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sin calificación final");
    }
}
