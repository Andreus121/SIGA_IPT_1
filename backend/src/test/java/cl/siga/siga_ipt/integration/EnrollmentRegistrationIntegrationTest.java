package cl.siga.siga_ipt.integration;

import cl.siga.siga_ipt.dto.EnrollmentCreateDto;
import cl.siga.siga_ipt.dto.RegistrationCreateDto;
import cl.siga.siga_ipt.entity.*;
import cl.siga.siga_ipt.enums.*;
import cl.siga.siga_ipt.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EnrollmentRegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AcademicPeriodRepository periodRepository;

    @Autowired
    private CareerRepository careerRepository;

    @Autowired
    private StudyPlanRepository studyPlanRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PrerequisiteRepository prerequisiteRepository;

    @Autowired
    private AcademicHistoryRepository academicHistoryRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private SectionScheduleRepository scheduleRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    private StudentEntity regularStudent;
    private AcademicPeriodEntity openPeriod;
    private SectionEntity prog1Section;
    private SectionEntity prog2Section;

    @BeforeEach
    void setUp() {
        registrationRepository.deleteAll();
        enrollmentRepository.deleteAll();
        scheduleRepository.deleteAll();
        sectionRepository.deleteAll();
        academicHistoryRepository.deleteAll();
        prerequisiteRepository.deleteAll();
        courseRepository.deleteAll();
        studyPlanRepository.deleteAll();
        careerRepository.deleteAll();
        studentRepository.deleteAll();
        teacherRepository.deleteAll();
        periodRepository.deleteAll();

        // 1. Período OPEN
        openPeriod = periodRepository.save(AcademicPeriodEntity.builder()
                .periodCode("2027-1")
                .status(PeriodStatus.OPEN)
                .build());

        // 2. Plan y Carrera
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

        // 3. Asignaturas: PROG1 (semestre 1) y PROG2 (semestre 2, con prerrequisito PROG1)
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

        courseRepository.save(CourseEntity.builder()
                .code("PROG2")
                .studyPlanCode("TAP-2024")
                .name("Programacion 2")
                .semester(2)
                .sct(4)
                .theoryHours(2)
                .exerciseHours(0)
                .labHours(0)
                .build());

        prerequisiteRepository.save(PrerequisiteEntity.builder()
                .courseCode("PROG2")
                .studyPlanCode("TAP-2024")
                .prerequisiteCourseCode("PROG1")
                .build());

        // 4. Estudiante Regular
        regularStudent = studentRepository.save(StudentEntity.builder()
                .run("12345678-9")
                .fullName("Estudiante Regular")
                .email("estudiante@ipt.cl")
                .careerCode("TAP")
                .studyPlanCode("TAP-2024")
                .status(StudentStatus.REGULAR)
                .build());

        // 5. Docente
        TeacherEntity teacher = teacherRepository.save(TeacherEntity.builder()
                .run("98765432-1")
                .fullName("Docente Guia")
                .email("docente@ipt.cl")
                .professionalTitle("Ingeniero")
                .academicDegree("Magíster")
                .status(TeacherStatus.ACTIVE)
                .build());

        // 6. Secciones
        prog1Section = sectionRepository.save(SectionEntity.builder()
                .code("SEC-PROG1-2027-1")
                .courseCode("PROG1")
                .periodCode("2027-1")
                .teacherId(teacher.getId())
                .capacity(30)
                .build());

        scheduleRepository.save(SectionScheduleEntity.builder()
                .sectionCode("SEC-PROG1-2027-1")
                .periodCode("2027-1")
                .day("L")
                .moduleNumber(1)
                .build());

        prog2Section = sectionRepository.save(SectionEntity.builder()
                .code("SEC-PROG2-2027-1")
                .courseCode("PROG2")
                .periodCode("2027-1")
                .teacherId(teacher.getId())
                .capacity(30)
                .build());

        scheduleRepository.save(SectionScheduleEntity.builder()
                .sectionCode("SEC-PROG2-2027-1")
                .periodCode("2027-1")
                .day("M")
                .moduleNumber(2)
                .build());
    }

    // ==========================================
    // ESCENARIOS EXITOSOS (3)
    // ==========================================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Éxito 1: Matricular estudiante regular en período abierto")
    void testEnrollStudent_Success() throws Exception {
        EnrollmentCreateDto dto = EnrollmentCreateDto.builder()
                .studentRun(regularStudent.getRun())
                .periodCode("2027-1")
                .build();

        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentRun").value(regularStudent.getRun()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Éxito 2: Inscribir asignatura cumpliendo prerrequisitos, sin tope y con cupos")
    void testRegisterCourse_Success() throws Exception {
        // Matrícula previa activa
        enrollmentRepository.save(EnrollmentEntity.builder()
                .studentRun(regularStudent.getRun())
                .periodCode("2027-1")
                .enrollmentDate(LocalDate.now())
                .status(EnrollmentStatus.ACTIVE)
                .build());

        RegistrationCreateDto dto = RegistrationCreateDto.builder()
                .studentRun(regularStudent.getRun())
                .periodCode("2027-1")
                .sectionCode("SEC-PROG1-2027-1")
                .build();

        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseCode").value("PROG1"))
                .andExpect(jsonPath("$.status").value("REGISTERED"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Éxito 3: Anular inscripción de ramo en período abierto liberando cupo")
    void testDropCourse_Success() throws Exception {
        RegistrationEntity registration = registrationRepository.save(RegistrationEntity.builder()
                .studentRun(regularStudent.getRun())
                .courseCode("PROG1")
                .sectionCode("SEC-PROG1-2027-1")
                .periodCode("2027-1")
                .registrationDate(LocalDateTime.now())
                .status(RegistrationStatus.REGISTERED)
                .build());

        mockMvc.perform(delete("/api/registrations/" + registration.getId()))
                .andExpect(status().isNoContent());
    }

    // ==========================================
    // ESCENARIOS DE RECHAZO (3)
    // ==========================================

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Rechazo 1: Rechazar inscripción si el estudiante no tiene matrícula activa en el período")
    void testRegisterCourse_NotEnrolled_Fails() throws Exception {
        RegistrationCreateDto dto = RegistrationCreateDto.builder()
                .studentRun(regularStudent.getRun())
                .periodCode("2027-1")
                .sectionCode("SEC-PROG1-2027-1")
                .build();

        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsStringIgnoringCase("matriculado")));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Rechazo 2: Rechazar inscripción si no cumple con los prerrequisitos aprobados")
    void testRegisterCourse_MissingPrerequisite_Fails() throws Exception {
        // Matrícula activa
        enrollmentRepository.save(EnrollmentEntity.builder()
                .studentRun(regularStudent.getRun())
                .periodCode("2027-1")
                .enrollmentDate(LocalDate.now())
                .status(EnrollmentStatus.ACTIVE)
                .build());

        // Intentar inscribir PROG2 sin haber aprobado PROG1
        RegistrationCreateDto dto = RegistrationCreateDto.builder()
                .studentRun(regularStudent.getRun())
                .periodCode("2027-1")
                .sectionCode("SEC-PROG2-2027-1")
                .build();

        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Rechazo 3: Rechazar inscripción si se produce tope de horario con otra asignatura")
    void testRegisterCourse_ScheduleConflict_Fails() throws Exception {
        // Matrícula activa
        enrollmentRepository.save(EnrollmentEntity.builder()
                .studentRun(regularStudent.getRun())
                .periodCode("2027-1")
                .enrollmentDate(LocalDate.now())
                .status(EnrollmentStatus.ACTIVE)
                .build());

        // Ya inscrito en PROG1 (Lunes M1)
        registrationRepository.save(RegistrationEntity.builder()
                .studentRun(regularStudent.getRun())
                .courseCode("PROG1")
                .sectionCode("SEC-PROG1-2027-1")
                .periodCode("2027-1")
                .registrationDate(LocalDateTime.now())
                .status(RegistrationStatus.REGISTERED)
                .build());

        // Crear una nueva sección para otra asignatura pero también en Lunes M1
        courseRepository.save(CourseEntity.builder()
                .code("BD1")
                .studyPlanCode("TAP-2024")
                .name("Bases de Datos 1")
                .semester(1)
                .sct(4)
                .theoryHours(2)
                .exerciseHours(0)
                .labHours(0)
                .build());

        SectionEntity bdSection = sectionRepository.save(SectionEntity.builder()
                .code("SEC-BD1-2027-1")
                .courseCode("BD1")
                .periodCode("2027-1")
                .teacherId(1L)
                .capacity(30)
                .build());

        scheduleRepository.save(SectionScheduleEntity.builder()
                .sectionCode("SEC-BD1-2027-1")
                .periodCode("2027-1")
                .day("L")
                .moduleNumber(1) // Mismo horario: Lunes M1
                .build());

        RegistrationCreateDto dto = RegistrationCreateDto.builder()
                .studentRun(regularStudent.getRun())
                .periodCode("2027-1")
                .sectionCode("SEC-BD1-2027-1")
                .build();

        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsStringIgnoringCase("tope de horario")));
    }
}
